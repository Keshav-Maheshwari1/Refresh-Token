package com.refreshtoken.service.impl;

import com.refreshtoken.entities.User;
import com.refreshtoken.reposiotry.UserRepository;
import com.refreshtoken.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private  UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ResponseEntity<User> getUser(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<User>> allUsers() {
        return Optional.of(userRepository.findAll())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<String> createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
       return Optional.ofNullable(userRepository.findByEmail(user.getEmail()))
               .map(oldUser -> {
                   userRepository.delete(oldUser);
                   return ResponseEntity.ok("User updated successfully");
               })
               .orElse(ResponseEntity.ok(userRepository.save(user).toString()));
    }

    @Override
    public ResponseEntity<String> updateUser(String email, User user) {
        return Optional.ofNullable(userRepository.findByEmail(email))
                .map(oldUser->{
                    oldUser.setUsername(user.getUsername());
                    oldUser.setEmail(user.getEmail());
                    oldUser.setPassword(passwordEncoder.encode(user.getPassword()));
                    userRepository.save(oldUser);
                    return ResponseEntity.ok("User updated successfully");
                })
                .orElse(ResponseEntity.badRequest().body("User not found"));
    }

    @Override
    public ResponseEntity<String> deleteUser(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email))
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok("User deleted successfully");
                }).orElse(ResponseEntity.badRequest().body("User not found"));
    }
}
