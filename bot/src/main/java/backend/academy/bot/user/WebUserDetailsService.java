package backend.academy.bot.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class WebUserDetailsService implements UserDetailsService {
    private final WebUserRepository webUserRepository;

    public WebUserDetailsService(WebUserRepository webUserRepository) {
        this.webUserRepository = webUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        WebUser user = webUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return new WebUserDetails(user);
    }
}
