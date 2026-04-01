package com.akaPierre.authsystem.service;

import com.akaPierre.authsystem.dto.UserRegistrationDto;
import com.akaPierre.authsystem.model.Role;
import com.akaPierre.authsystem.model.User;
import com.akaPierre.authsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        log.info("Tentativa de cadastro para o e-mail: {}", dto.getEmail());

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("As senhas não coincidem.");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Tentativa de cadastro com e-mail já existente: {}", dto.getEmail());
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        String hashedPassword = passwordEncoder.encode(dto.getPassword());

        User newUser = new User(
            dto.getName(),
            dto.getEmail(),
            hashedPassword,
            Role.ROLE_USER
        );

        User savedUser = userRepository.save(newUser);
        log.info("Usuário cadastrado com sucesso. ID: {}, E-mail: {}", savedUser.getId(), savedUser.getEmail());

        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailAlreadyExists(String email) {
        return userRepository.existsByEmail(email);
    }
}