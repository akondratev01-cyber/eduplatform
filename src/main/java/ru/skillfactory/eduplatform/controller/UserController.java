package ru.skillfactory.eduplatform.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.skillfactory.eduplatform.dto.RegistrationRequest;
import ru.skillfactory.eduplatform.dto.UserResponse;
import ru.skillfactory.eduplatform.entity.User;
import ru.skillfactory.eduplatform.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegistrationRequest request) {

        User user = userService.registerStudent(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password()
        );

        return UserResponse.from(user);
    }
}