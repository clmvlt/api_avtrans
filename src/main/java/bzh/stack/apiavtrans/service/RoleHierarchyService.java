package bzh.stack.apiavtrans.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class RoleHierarchyService {

    private static final List<String> ROLE_HIERARCHY = Arrays.asList(
        "Utilisateur",
        "Mécanicien",
        "Administrateur"
    );

    public boolean hasAccess(String userRole, String requiredRole) {
        if (userRole == null || requiredRole == null) {
            return false;
        }

        int userRoleIndex = ROLE_HIERARCHY.indexOf(userRole);
        int requiredRoleIndex = ROLE_HIERARCHY.indexOf(requiredRole);

        if (userRoleIndex == -1 || requiredRoleIndex == -1) {
            return false;
        }

        return userRoleIndex >= requiredRoleIndex;
    }
}
