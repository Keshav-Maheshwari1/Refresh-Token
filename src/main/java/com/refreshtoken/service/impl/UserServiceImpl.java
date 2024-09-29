package com.refreshtoken.service.impl;

import com.refreshtoken.entities.User;
import com.refreshtoken.reposiotry.UserRepository;
import com.refreshtoken.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private  UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ResponseEntity<User> getUser(String email) {
        return Optional.of(userRepository.findByEmail(email))
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
        return Optional.of(userRepository.insert(user))
                .map(user1 -> ResponseEntity.ok("User added successfully"))
                .orElse(ResponseEntity.badRequest().body("User already exists"));
    }

    @Override
    public ResponseEntity<String> updateUser(String email, User user) {
        return Optional.of(userRepository.findByEmail(email))
                .map(oldUser->{
                    oldUser.setUsername(user.getUsername());
                    oldUser.setEmail(user.getEmail());
                    oldUser.setPassword(user.getPassword());
                    userRepository.save(oldUser);
                    return ResponseEntity.ok("User updated successfully");
                })
                .orElse(ResponseEntity.badRequest().body("User not found"));
    }

    @Override
    public ResponseEntity<String> deleteUser(String email) {
        return Optional.of(userRepository.findByEmail(email))
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok("User deleted successfully");
                }).orElse(ResponseEntity.badRequest().body("User not found"));
    }
}
