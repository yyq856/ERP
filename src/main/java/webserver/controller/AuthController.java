package webserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import webserver.pojo.AuthResponse;
import webserver.pojo.LoginRequest;
import webserver.pojo.RegisterRequest;
import webserver.service.AuthService;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/api/register")
    public AuthResponse register(@RequestBody RegisterRequest registerRequest) {
        log.info("用户注册请求: {}", registerRequest.getUsername());
        return authService.register(registerRequest);
    }

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录结果
     */
    @PostMapping("/api/login")
    public AuthResponse login(@RequestBody LoginRequest loginRequest) {
        log.info("用户登录请求: {}", loginRequest.getUsername());
        return authService.login(loginRequest);
    }
}
