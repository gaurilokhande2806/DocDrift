package com.example.demo;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * Creates a new user record.
     * @param name User full name
     * @param email User email address
     * @param oldParam Outdated parameter tag no longer in method signature
     * @return User response object
     */
    @PostMapping
    public UserDto createUser(@RequestParam String name,
                              @RequestParam String email,
                              @RequestParam String phone,
                              @RequestParam String department) {
        return new UserDto(name, email, phone, department);
    }

    @GetMapping("/payments")
    public String getPaymentHistory() {
        return "payments";
    }
}
