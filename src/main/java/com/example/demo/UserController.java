package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @GetMapping("/forgetPassword")
    public ResponseEntity<String> forgetPassword(@RequestParam String emailId){
        UserEntity userEntity = userRepository.findByEmailId(emailId);
        if (Objects.nonNull(userEntity)){
            TokenEntity token = new TokenEntity();
            token.setUserId(userEntity.getId());
            token.setToken(UUID.randomUUID().toString());
            tokenRepository.save(token);
            return ResponseEntity.ok(token.getToken());
        }
        return ResponseEntity.notFound().build();
    }


    @PostMapping("/changePassword")
    public ResponseEntity<String> changePassword(@RequestBody UserRequestDTO userRequestDTO) {
        if (userRequestDTO.getNewPassword().equals(userRequestDTO.getReTypePassword())) {
            TokenEntity token = tokenRepository.findByToken(userRequestDTO.getToken());
            if (Objects.nonNull(token)) {
                Optional<UserEntity> user = userRepository.findById(token.getUserId());
                if (user.isPresent()) {
                    UserEntity userEntity = user.get();
                    userEntity.setPassword(userRequestDTO.getNewPassword());
                    userRepository.save(userEntity);
                    tokenRepository.delete(token);
                    return ResponseEntity.ok().body("Password updated successfully!!");
                } else {
                    return ResponseEntity.notFound().build();
                }
            } else {
                return ResponseEntity.badRequest().body("Invalid Token");
            }
        } else {
            return ResponseEntity.badRequest().body("new password and retype password are different.");
        }
    }
}
