package com.hillel.items_exchange.controller;

import com.hillel.items_exchange.dto.UserDto;
import com.hillel.items_exchange.security.jwt.JwtUser;
import com.hillel.items_exchange.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.PositiveOrZero;
import java.security.Principal;

import static com.hillel.items_exchange.util.MessageSourceUtil.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/info/{id}")
    public @ResponseBody
    ResponseEntity<UserDto> getUserInfo(@PositiveOrZero @PathVariable("id") long id, Principal principal) {

        return ResponseEntity.ok(userService.getByUsernameOrEmail(principal.getName())
                .filter(user -> user.getId() == id)
                .orElseThrow(() -> new AccessDeniedException(
                        getExceptionMessageSource("exception.access-denied.user-data"))));
    }

    @PutMapping("/info")
    public ResponseEntity<UserDto> updateUserInfo(@Valid @RequestBody UserDto userDto,
                                                  @AuthenticationPrincipal JwtUser user) {
        if (user.getId() != userDto.getId()) {
            throw new AccessDeniedException(
                    getExceptionMessageSource("exception.permission-denied.user-profile"));
        }
        return new ResponseEntity<>(userService.update(userDto), HttpStatus.ACCEPTED);
    }
}
