package com.refreshtoken.service;

import com.refreshtoken.entities.User;
import com.refreshtoken.models.JwtRequest;
import com.refreshtoken.models.JwtResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
    ResponseEntity<User> getUser(String email);
    ResponseEntity<List<User>> allUsers();
    ResponseEntity<String> createUser(User user);
    ResponseEntity<JwtResponse> loginUser(JwtRequest request);
    ResponseEntity<String> updateUser(String email,User user);
    ResponseEntity<String> deleteUser(String email);
}
