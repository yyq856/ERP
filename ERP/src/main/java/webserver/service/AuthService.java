package webserver.service;

import webserver.pojo.AuthResponse;
import webserver.pojo.LoginRequest;
import webserver.pojo.RegisterRequest;

public interface AuthService {
    
    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    AuthResponse register(RegisterRequest registerRequest);

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录结果
     */
    AuthResponse login(LoginRequest loginRequest);
}
