package bzh.stack.apiavtrans.config;

import bzh.stack.apiavtrans.entity.AbsenceType;
import bzh.stack.apiavtrans.entity.Role;
import bzh.stack.apiavtrans.entity.TodoCategory;
import bzh.stack.apiavtrans.entity.TypeCarte;
import bzh.stack.apiavtrans.repository.AbsenceTypeRepository;
import bzh.stack.apiavtrans.repository.RoleRepository;
import bzh.stack.apiavtrans.repository.TodoCategoryRepository;
import bzh.stack.apiavtrans.repository.TypeCarteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(RoleRepository roleRepository, AbsenceTypeRepository absenceTypeRepository, TodoCategoryRepository todoCategoryRepository, TypeCarteRepository typeCarteRepository) {
        return args -> {
            if (roleRepository.count() == 0) {
                Role administrateur = new Role();
                administrateur.setNom("Administrateur");
                administrateur.setColor("#FF5733");
                roleRepository.save(administrateur);

                Role mecanicien = new Role();
                mecanicien.setNom("Mécanicien");
                mecanicien.setColor("#33FF57");
                roleRepository.save(mecanicien);

                Role utilisateur = new Role();
                utilisateur.setNom("Utilisateur");
                utilisateur.setColor("#3357FF");
                roleRepository.save(utilisateur);

                System.out.println("Rôles initialisés : Administrateur, Mécanicien, Utilisateur");
            }

            if (absenceTypeRepository.count() == 0) {
                AbsenceType congesPayes = new AbsenceType();
                congesPayes.setName("Congés payés");
                congesPayes.setColor("#4CAF50");
                absenceTypeRepository.save(congesPayes);

                AbsenceType rtt = new AbsenceType();
                rtt.setName("RTT");
                rtt.setColor("#2196F3");
                absenceTypeRepository.save(rtt);

                AbsenceType maladie = new AbsenceType();
                maladie.setName("Maladie");
                maladie.setColor("#F44336");
                absenceTypeRepository.save(maladie);

                AbsenceType sanssolde = new AbsenceType();
                sanssolde.setName("Sans solde");
                sanssolde.setColor("#9E9E9E");
                absenceTypeRepository.save(sanssolde);

                AbsenceType autre = new AbsenceType();
                autre.setName("Autre");
                autre.setColor("#FF9800");
                absenceTypeRepository.save(autre);

                System.out.println("Types d'absence initialisés : Congés payés, RTT, Maladie, Sans solde, Autre");
            }

            if (todoCategoryRepository.count() == 0) {
                TodoCategory general = new TodoCategory();
                general.setName("General");
                general.setColor("#607D8B");
                todoCategoryRepository.save(general);

                TodoCategory urgent = new TodoCategory();
                urgent.setName("Urgent");
                urgent.setColor("#F44336");
                todoCategoryRepository.save(urgent);

                TodoCategory maintenance = new TodoCategory();
                maintenance.setName("Maintenance");
                maintenance.setColor("#FF9800");
                todoCategoryRepository.save(maintenance);

                TodoCategory admin = new TodoCategory();
                admin.setName("Administration");
                admin.setColor("#2196F3");
                todoCategoryRepository.save(admin);

                System.out.println("Todo categories initialized: General, Urgent, Maintenance, Administration");
            }

            if (typeCarteRepository.count() == 0) {
                TypeCarte bancaire = new TypeCarte();
                bancaire.setNom("Bancaire");
                bancaire.setDescription("Carte bancaire");
                typeCarteRepository.save(bancaire);

                TypeCarte carburant = new TypeCarte();
                carburant.setNom("Carburant");
                carburant.setDescription("Carte carburant");
                typeCarteRepository.save(carburant);

                System.out.println("Types de cartes initialises : Bancaire, Carburant");
            }
        };
    }
}
