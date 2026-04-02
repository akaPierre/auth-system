package com.akaPierre.authsystem.config;

import com.akaPierre.authsystem.model.Role;
import com.akaPierre.authsystem.model.User;
import com.akaPierre.authsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@sistema.com";
    private static final String ADMIN_NAME = "Administrador";
    private static final String ADMIN_PASSWORD = "Admin@1234";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createDefaultAdminIfNotExists();
    }

    private void createDefaultAdminIfNotExists() {
        if (userRepository.existsByEmail(ADMIN_EMAIL)) {
            log.info("Admin padrão já existe - nenhuma ação necessária. E-mail: {}", ADMIN_EMAIL);
            return;
        }

        log.info("Admin padrão não encontrado. Criando...");

        String hashedPassword = passwordEncoder.encode(ADMIN_PASSWORD);

        User admin = new User(
            ADMIN_NAME,
            ADMIN_EMAIL,
            hashedPassword,
            Role.ROLE_ADMIN
        );

        userRepository.save(admin);

        log.info("✅ Admin padrão criado com sucesso.");
        log.info("   E-mail : {}", ADMIN_EMAIL);
        log.info("   Senha  : [PROTEGIDA - user '{}' para login]", ADMIN_PASSWORD);
        log.warn("⚠️  ATENÇÃO: Altere as credenciais do admin antes de ir para produção!");
    }
}