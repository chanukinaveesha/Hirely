package com.recruitsystem.service.auth;

import com.recruitsystem.dto.auth.AuthResponse;
import com.recruitsystem.dto.auth.LoginRequest;
import com.recruitsystem.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
