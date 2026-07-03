package com.familyos.familyos.authentication.service;

import com.familyos.familyos.authentication.entity.User;
import com.familyos.familyos.authentication.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User findOrCreateUser(String email, String name, String provider) {
        log.debug("Finding or creating user for email: {}", email);
        
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            log.debug("User found: {}", email);
            return user;
        }
        
        User newUser = new User(email, name, provider);
        User savedUser = userRepository.save(newUser);
        log.info("User created: {}", email);
        
        return savedUser;
    }

    @Transactional
    public User updateUser(User user) {
        log.debug("Updating user: {}", user.getEmail());
        
        if (!userRepository.existsById(user.getId())) {
            log.warn("User not found for update: {}", user.getId());
            throw new IllegalArgumentException("User not found: " + user.getId());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User updated: {}", user.getEmail());
        
        return updatedUser;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(java.util.UUID id) {
        log.debug("Finding user by id: {}", id);
        return userRepository.findById(id);
    }
}
