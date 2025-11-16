package backend.academy.bot.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebUserService {
    private final WebUserRepository webUserRepository;
    private final PasswordEncoder passwordEncoder;

    public WebUserService(WebUserRepository webUserRepository, PasswordEncoder passwordEncoder) {
        this.webUserRepository = webUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public WebUser registerUser(String username, String rawPassword) {
        if (webUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }
        WebUser user = new WebUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        return webUserRepository.save(user);
    }

    public WebUserRepository repository() {
        return webUserRepository;
    }
}
