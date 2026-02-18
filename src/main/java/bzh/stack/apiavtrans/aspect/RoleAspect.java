package bzh.stack.apiavtrans.aspect;

import bzh.stack.apiavtrans.annotation.RequireRole;
import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.exception.AuthenticationException;
import bzh.stack.apiavtrans.repository.UserRepository;
import bzh.stack.apiavtrans.service.RoleHierarchyService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleAspect {

    private final UserRepository userRepository;
    private final RoleHierarchyService roleHierarchyService;

    @Around("@annotation(bzh.stack.apiavtrans.annotation.RequireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole requireRole = method.getAnnotation(RequireRole.class);

        String requiredRole = requireRole.value();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RuntimeException("Access denied");
        }

        HttpServletRequest request = attributes.getRequest();
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new AuthenticationException("Authentication required: Missing or invalid Authorization header");
        }

        String token = authorization.substring(7);
        User user = userRepository.findByToken(token)
                .orElseThrow(() -> new AuthenticationException("Authentication required: Invalid token"));

        if (!user.getIsActive()) {
            throw new AuthenticationException("Access denied: Account is not active");
        }

        if (user.getRole() == null) {
            throw new AuthenticationException("Access denied: No role assigned");
        }

        String userRole = user.getRole().getNom();

        if (!roleHierarchyService.hasAccess(userRole, requiredRole)) {
            throw new AuthenticationException("Access denied: Required role is " + requiredRole);
        }

        request.setAttribute("user", user);
        request.setAttribute("userEmail", user.getEmail());

        return joinPoint.proceed();
    }
}
