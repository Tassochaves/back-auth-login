package com.dev.apilogin.controllers;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.apilogin.domain.user.User;
import com.dev.apilogin.dto.LoginRequestDTO;
import com.dev.apilogin.dto.RegisterRequestDTO;
import com.dev.apilogin.dto.ResponseDTO;
import com.dev.apilogin.infra.security.TokenService;
import com.dev.apilogin.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDTO loginDTO){
        User user = this.userRepository.findByEmail(loginDTO.email()).orElseThrow(() -> new RuntimeException("User Not Found!!"));

        if(passwordEncoder.matches(loginDTO.password(), user.getPassword())){
            String token = this.tokenService.generateToken(user);

            return ResponseEntity.ok(new ResponseDTO(user.getName(), token));
        }

        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequestDTO registerDTO){

        Optional<User> userExist = this.userRepository.findByEmail(registerDTO.email());

        if(userExist.isEmpty()){
            User newUser = new User();
            newUser.setPassword(passwordEncoder.encode(registerDTO.password()));
            newUser.setEmail(registerDTO.email());
            newUser.setName(registerDTO.name());

            this.userRepository.save(newUser);

            String token = this.tokenService.generateToken(newUser);

            return ResponseEntity.ok(new ResponseDTO(newUser.getName(), token));

        }
        
        return ResponseEntity.badRequest().build();
    }
}
