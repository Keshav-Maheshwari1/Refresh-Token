package com.refreshtoken.service.impl;

import com.refreshtoken.entities.User;
import com.refreshtoken.models.JwtRequest;
import com.refreshtoken.models.JwtResponse;
import com.refreshtoken.reposiotry.UserRepository;
import com.refreshtoken.security.JwtHelper;
import com.refreshtoken.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private JwtHelper jwtHelper;


    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private  UserRepository userRepository;

    public UserServiceImpl(AuthenticationManager authenticationManager,  PasswordEncoder passwordEncoder, JwtHelper jwtHelper, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtHelper = jwtHelper;
        this.userRepository = userRepository;
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
        try{
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return Optional.of(userRepository.save(user))
                    .map(user1->ResponseEntity.ok("User created successfully"))
                    .orElse(ResponseEntity.internalServerError().body("error creating user server problem"));
        }catch(Exception e){
            throw new BadCredentialsException("Could not create user");
        }
    }
    public ResponseEntity<JwtResponse> loginUser(JwtRequest request){
        User user = authenticate(request);
        if(user!=null && passwordEncoder.matches(request.getPassword(), user.getPassword())){
            JwtResponse jwtResponse = JwtResponse.builder()
                    .username(request.getEmail())
                    .jwtToken(jwtHelper.generateTokenFromUsername(user.getUsername()))
                    .build();
            return ResponseEntity.ok(jwtResponse);
        }
        return ResponseEntity.badRequest().body(null);
    }
    private User authenticate(JwtRequest input) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword());
        try {
            authenticationManager.authenticate(authentication);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(" Invalid Username or Password  !!");
        }

        return userRepository.findByEmail(input.getEmail());
    }

    @Override
    public ResponseEntity<String> updateUser(String email, User user) {
        logger.info(userRepository.findByEmail(email).toString());
        return Optional.ofNullable(userRepository.findByEmail(email))
                .map(oldUser->{
                    oldUser.setName(user.getName());
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
