package com.akaPierre.authsystem.service;

import com.akaPierre.authsystem.dto.UserRegistrationDto;
import com.akaPierre.authsystem.model.User;

public interface UserService {
    
    User registerUser(UserRegistrationDto dto);

    boolean emailAlreadyExists(String email);
}