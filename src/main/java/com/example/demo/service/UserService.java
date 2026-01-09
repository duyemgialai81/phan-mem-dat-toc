package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;


    public Optional<User> login(String username, String password) {
        Optional<User> user = userRepository.findByUsernameAndPassword(username, password);
        return null;
    }
    public List<User> getAll(){
        List<User> users = userRepository.findAll();
        return users;
    }
}
