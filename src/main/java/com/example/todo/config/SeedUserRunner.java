package com.example.todo.config;

import com.example.todo.entity.AppUser;
import com.example.todo.repository.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeedUserRunner {

    @Bean
    public ApplicationRunner seedUser(UserMapper userMapper, PasswordEncoder passwordEncoder,
                                     @Value("${app.seed-user.enabled:true}") boolean enabled) {
        return args -> {
            if (!enabled) {
                return;
            }
            String username = "USER";
            String rawPassword = "admin";
            if (userMapper.findByUsername(username) != null) {
                return;
            }
            AppUser user = new AppUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole("USER");
            userMapper.insert(user);
        };
    }
}
