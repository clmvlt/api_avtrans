package bzh.stack.apiavtrans.interceptor;

import bzh.stack.apiavtrans.entity.User;
import bzh.stack.apiavtrans.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class UserEmailInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            User user = userRepository.findByToken(token).orElse(null);

            if (user != null) {
                request.setAttribute("userEmail", user.getEmail());
            }
        }

        return true;
    }
}
