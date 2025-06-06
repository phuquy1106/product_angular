package com.example.demo_springboot.controller;
import com.example.demo_springboot.model.User;
import com.example.demo_springboot.repository.UserRepository;
import com.example.demo_springboot.util.Stringutil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    @Autowired
    UserRepository userRepository;

    @CrossOrigin("*")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User register) {
        boolean isCheckUser = userRepository.findByUsername(register.getUsername()).isPresent();
        if(isCheckUser){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }else{
            User user = new User();
            user.setUsername(register.getUsername());
            user.setPassword(Stringutil.md5(register.getPassword()));
            user.setEmail(register.getEmail());
            userRepository.save(user);
            return ResponseEntity.ok("Registered successfully");
        }
    }
    @CrossOrigin("*")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User userLogin) {

        boolean isSuccess = userRepository.findByUsername(userLogin.getUsername())
                .map(user -> Stringutil.md5(userLogin.getPassword()).equals(user.getPassword()))
                .orElse(false);
        if (isSuccess) {
            return ResponseEntity.ok("Login successful");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

}
