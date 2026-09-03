package com.example.studentcoursemanagement.service;

import com.example.studentcoursemanagement.dto.auth.LoginRequest;
import com.example.studentcoursemanagement.dto.auth.LoginResponse;
import com.example.studentcoursemanagement.dto.auth.RegisterRequest;
import com.example.studentcoursemanagement.dto.auth.UserResponse;
import com.example.studentcoursemanagement.entity.Role;
import com.example.studentcoursemanagement.entity.Student;
import com.example.studentcoursemanagement.entity.User;
import com.example.studentcoursemanagement.exception.DuplicateStudentException;
import com.example.studentcoursemanagement.exception.DuplicateUserException;
import com.example.studentcoursemanagement.repo.StudentRepository;
import com.example.studentcoursemanagement.repo.UserRepository;
import com.example.studentcoursemanagement.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       StudentRepository studentRepository,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateUserException("Username already exists");
        }

        if (studentRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateStudentException("A student with this email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);
        user.setEnabled(true);

        User savedUser;
        try {
            savedUser = userRepository.save(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateUserException("Username already exists");
        }

        Student student = new Student();
        student.setName(request.getName().trim());
        student.setEmail(email);
        student.setUser(savedUser);
        studentRepository.save(student);

        return new UserResponse(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername().trim(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return new LoginResponse(jwtService.generateToken(userDetails));
    }
}
