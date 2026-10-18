package bzh.stack.apiavtrans.repository;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Migration V1 (rattachement des pauses à leur service) sur un vrai PostgreSQL, à partir de l'ancien schéma.
 */
class RattachementPausesMigrationTest {

    private static EmbeddedPostgres postgres;

    private DataSource dataSource;
    private final UUID chauffeur = UUID.randomUUID();
    private final UUID collegue = UUID.randomUUID();

    @BeforeAll
    static void demarrerPostgres() throws Exception {
        postgres = EmbeddedPostgres.start();
    }

    @AfterAll
    static void arreterPostgres() throws Exception {
        postgres.close();
    }

    @BeforeEach
    void setUp() throws Exception {
        String base = "m" + UUID.randomUUID().toString().replace("-", "");
        try (Connection c = postgres.getPostgresDatabase().getConnection(); Statement st = c.createStatement()) {
            st.execute("CREATE DATABASE " + base);
        }
        dataSource = postgres.getDatabase("postgres", base);
    }

    @Test
    void should_attach_each_existing_break_to_its_work_service() throws Exception {
        ancienSchema();
        UUID nuit = service(chauffeur, false, "2026-10-12 21:00", "2026-10-13 05:00");
        UUID pauseApresMinuit = service(chauffeur, true, "2026-10-13 01:00", "2026-10-13 02:00");
        // Service d'un collègue couvrant la même nuit : ne doit pas « voler » la pause
        service(collegue, false, "2026-10-12 20:00", "2026-10-13 06:00");

        UUID matin = service(chauffeur, false, "2026-10-14 08:00", "2026-10-14 12:00");
        UUID apresMidi = service(chauffeur, false, "2026-10-14 14:00", "2026-10-14 18:00");
        UUID pauseMatin = service(chauffeur, true, "2026-10-14 10:00", "2026-10-14 10:15");
        UUID pauseApresMidi = service(chauffeur, true, "2026-10-14 15:00", "2026-10-14 15:15");
        // Pause saisie hors service (service raccourci après coup) : rattachée au plus proche
        UUID pauseHorsService = service(chauffeur, true, "2026-10-14 12:30", "2026-10-14 12:45");
        // Pause isolée sans service à moins de 24 h : reste non rattachée
        UUID pauseIsolee = service(chauffeur, true, "2026-09-01 12:00", "2026-09-01 12:30");

        UUID enCours = service(chauffeur, false, "2026-10-16 21:00", null);
        UUID pauseEnCours = service(chauffeur, true, "2026-10-17 01:00", null);

        migrer();

        assertThat(parent(pauseApresMinuit)).isEqualTo(nuit);
        assertThat(parent(pauseMatin)).isEqualTo(matin);
        assertThat(parent(pauseApresMidi)).isEqualTo(apresMidi);
        assertThat(parent(pauseHorsService)).isEqualTo(matin);
        assertThat(parent(pauseIsolee)).isNull();
        assertThat(parent(pauseEnCours)).isEqualTo(enCours);
        assertThat(parent(nuit)).isNull();
        assertThat(existe("SELECT 1 FROM pg_constraint WHERE conname = 'fk_services_service_parent'")).isTrue();
        assertThat(existe("SELECT 1 FROM pg_indexes WHERE indexname = 'idx_services_service_parent'")).isTrue();
    }

    @Test
    void should_do_nothing_and_succeed_when_database_is_empty() throws Exception {
        migrer();

        assertThat(existe("SELECT 1 FROM information_schema.tables WHERE table_name = 'services'")).isFalse();
        assertThat(existe("SELECT 1 FROM flyway_schema_history WHERE version = '1' AND success")).isTrue();
    }

    @Test
    void should_keep_existing_links_when_column_already_exists() throws Exception {
        ancienSchema();
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            st.execute("ALTER TABLE services ADD COLUMN service_parent_uuid UUID");
        }
        UUID service = service(chauffeur, false, "2026-10-14 08:00", "2026-10-14 12:00");
        UUID autre = service(chauffeur, false, "2026-10-14 13:00", "2026-10-14 18:00");
        UUID pause = service(chauffeur, true, "2026-10-14 10:00", "2026-10-14 10:15");
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE services SET service_parent_uuid = ? WHERE uuid = ?")) {
            ps.setObject(1, autre);
            ps.setObject(2, pause);
            ps.executeUpdate();
        }

        migrer();

        assertThat(parent(pause)).isEqualTo(autre).isNotEqualTo(service);
    }

    private void migrer() {
        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/postgresql")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load()
                .migrate();
    }

    /** Schéma tel que créé par Hibernate avant la migration (colonnes utiles uniquement). */
    private void ancienSchema() throws Exception {
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            st.execute("CREATE TABLE users (uuid UUID PRIMARY KEY)");
            st.execute("CREATE TABLE services (uuid UUID PRIMARY KEY, debut TIMESTAMP WITH TIME ZONE NOT NULL, "
                    + "fin TIMESTAMP WITH TIME ZONE, duree BIGINT, is_break BOOLEAN NOT NULL, is_admin BOOLEAN NOT NULL, "
                    + "user_uuid UUID NOT NULL REFERENCES users (uuid))");
            st.execute("INSERT INTO users (uuid) VALUES ('" + chauffeur + "'), ('" + collegue + "')");
        }
    }

    private UUID service(UUID user, boolean isBreak, String debut, String fin) throws Exception {
        UUID uuid = UUID.randomUUID();
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO services (uuid, debut, fin, is_break, is_admin, user_uuid) "
                     + "VALUES (?, ?::timestamp AT TIME ZONE 'Europe/Paris', ?::timestamp AT TIME ZONE 'Europe/Paris', ?, false, ?)")) {
            ps.setObject(1, uuid);
            ps.setString(2, debut);
            ps.setString(3, fin);
            ps.setBoolean(4, isBreak);
            ps.setObject(5, user);
            ps.executeUpdate();
        }
        return uuid;
    }

    private UUID parent(UUID service) throws Exception {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT service_parent_uuid FROM services WHERE uuid = ?")) {
            ps.setObject(1, service);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getObject(1, UUID.class);
            }
        }
    }

    private boolean existe(String sql) throws Exception {
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next();
        }
    }
}
