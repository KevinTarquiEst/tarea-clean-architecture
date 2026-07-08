package org.cat.usercleanarchitecture.infraestructure.adapters.input;

import org.cat.usercleanarchitecture.aplication.ports.input.IUserUseCase;
import org.cat.usercleanarchitecture.domain.model.User;
import org.cat.usercleanarchitecture.infraestructure.adapters.input.dto.UserRequest;
import org.cat.usercleanarchitecture.infraestructure.adapters.input.dto.UserResponse;
import org.cat.usercleanarchitecture.infraestructure.adapters.input.mapper.UserRequestMapper;
import org.cat.usercleanarchitecture.infraestructure.adapters.input.mapper.UserResponseMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    private final IUserUseCase userUseCase;

    public UserController(IUserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @PostMapping
    public ResponseEntity<UserResponse> getUser(@RequestBody UserRequest request) {
        User user = userUseCase.create(UserRequestMapper.INSTANCE.toUser(request));
        return new ResponseEntity<>(
                UserResponseMapper.INSTANCE.toUserRequest(user),
                HttpStatus.CREATED
        );
    }
}
