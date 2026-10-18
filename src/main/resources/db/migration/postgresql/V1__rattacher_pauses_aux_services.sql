-- Rattache chaque pause (services.is_break = true) au service de travail auquel elle appartient.
--
-- Avant cette migration, une pause n'était liée à aucun service : les heures d'une période étaient
-- « services débutés dans la période − pauses débutées dans la période ». Une pause prise après minuit
-- pendant un service de nuit était donc déduite du jour suivant (ex. -1 h).
--
-- Flyway s'exécute avant Hibernate (ddl-auto=update) : la colonne est créée ici si besoin, avec la
-- même contrainte que celle déclarée sur l'entité Service. Sur une base vide (installation neuve),
-- la table n'existe pas encore : Hibernate la créera avec la colonne, il n'y a rien à rattacher.
DO $$
BEGIN
    IF to_regclass('services') IS NULL THEN
        RETURN;
    END IF;

    ALTER TABLE services ADD COLUMN IF NOT EXISTS service_parent_uuid UUID;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_services_service_parent') THEN
        ALTER TABLE services
            ADD CONSTRAINT fk_services_service_parent
            FOREIGN KEY (service_parent_uuid) REFERENCES services (uuid);
    END IF;

    CREATE INDEX IF NOT EXISTS idx_services_service_parent ON services (service_parent_uuid);
    CREATE INDEX IF NOT EXISTS idx_services_user_debut ON services (user_uuid, debut);

    -- Service de travail du même salarié le plus proche de la pause (à 24 h près) :
    -- distance nulle quand la pause commence pendant le service, sinon écart avec ses bornes.
    -- À distance égale (pause contenue dans le service), le service le plus récent l'emporte.
    UPDATE services p
    SET service_parent_uuid = (
        SELECT s.uuid
        FROM services s
        WHERE s.user_uuid = p.user_uuid
          AND s.is_break = false
          AND s.debut <= p.debut + INTERVAL '24 hours'
          AND (s.fin IS NULL OR s.fin >= p.debut - INTERVAL '24 hours')
        ORDER BY GREATEST(
                     s.debut - p.debut,
                     CASE WHEN s.fin IS NULL THEN INTERVAL '0' ELSE p.debut - s.fin END,
                     INTERVAL '0'
                 ),
                 s.debut DESC
        LIMIT 1
    )
    WHERE p.is_break = true
      AND p.service_parent_uuid IS NULL;
END $$;
