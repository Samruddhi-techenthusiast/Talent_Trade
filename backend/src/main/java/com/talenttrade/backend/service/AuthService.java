package com.talenttrade.backend.service;

import com.talenttrade.backend.dto.request.LoginRequest;
import com.talenttrade.backend.dto.request.RegisterRequest;
import com.talenttrade.backend.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
