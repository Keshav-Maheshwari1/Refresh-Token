package com.refreshtoken.controllers;

import com.refreshtoken.entities.User;
import com.refreshtoken.models.JwtRequest;
import com.refreshtoken.models.JwtResponse;
import com.refreshtoken.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    @Autowired
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    // Other controller methods...

    @GetMapping("/user/{email}")
    public ResponseEntity<User> getUser(@PathVariable String email) {
        return userService.getUser(email);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> allUsers() {
        return userService.allUsers();
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<String> signUp(@RequestBody User user) {
        User existingUser = userService.getUser(user.getEmail()).getBody();
        if(existingUser != null){
            JwtResponse response = signIn(new JwtRequest(user.getEmail(), user.getPassword())).getBody();
            if(response != null){
                return ResponseEntity.ok(response.getJwtToken());
            }else{
                return ResponseEntity.badRequest().body("User already exists! and invalid credentials");
            }
        }
        return userService.createUser(user);
    }

    @PostMapping("/auth/signin")
    public ResponseEntity<JwtResponse> signIn(@RequestBody JwtRequest request) {
        return userService.loginUser(request);
    }

    @PutMapping("/user/{email}")
    public ResponseEntity<String> updateUser(@PathVariable String email, @RequestBody User user) {
        return userService.updateUser(email, user);
    }

    @DeleteMapping("/user/{email}")
    public ResponseEntity<String> deleteUser(@PathVariable String email) {
        return userService.deleteUser(email);
    }
}
