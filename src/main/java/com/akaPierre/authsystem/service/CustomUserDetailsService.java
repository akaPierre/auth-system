package com.akaPierre.authsystem.service;

import com.akaPierre.authsystem.model.User;
import com.akaPierre.authsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Carregando usuário pelo e-mail: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado para o e-mail: {}", email);
                    return new UsernameNotFoundException("Credenciais inválidas.");
                });

        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(user.getRole().name())
        );

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }
}