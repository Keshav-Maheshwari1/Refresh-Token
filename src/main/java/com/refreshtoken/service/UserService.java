package com.refreshtoken.service;

import com.refreshtoken.entities.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
    ResponseEntity<User> getUser(String email);
    ResponseEntity<List<User>> allUsers();
    ResponseEntity<String> createUser(User user);
    ResponseEntity<String> updateUser(String email,User user);
    ResponseEntity<String> deleteUser(String email);
}
