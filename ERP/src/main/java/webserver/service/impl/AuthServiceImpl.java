package webserver.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import webserver.mapper.UserMapper;
import webserver.pojo.*;
import webserver.service.AuthService;
import webserver.util.JwtUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        try {
            // 参数验证
            List<Map<String, String>> validationErrors = new ArrayList<>();

            if (!StringUtils.hasText(registerRequest.getUsername())) {
                Map<String, String> error = new HashMap<>();
                error.put("field", "username");
                error.put("error", "用户名不能为空");
                validationErrors.add(error);
            }

            // 验证用户名是否为数字（因为要作为ID存储）
            if (StringUtils.hasText(registerRequest.getUsername())) {
                try {
                    Long.parseLong(registerRequest.getUsername());
                } catch (NumberFormatException e) {
                    Map<String, String> error = new HashMap<>();
                    error.put("field", "username");
                    error.put("error", "用户名必须为数字");
                    validationErrors.add(error);
                }
            }

            if (!StringUtils.hasText(registerRequest.getPassword())) {
                Map<String, String> error = new HashMap<>();
                error.put("field", "password");
                error.put("error", "密码不能为空");
                validationErrors.add(error);
            }

            if (!validationErrors.isEmpty()) {
                return AuthResponse.error("请求参数验证失败", 400, "INVALID_INPUT", validationErrors);
            }

            // 密码格式验证（适配20字符限制）
            if (registerRequest.getPassword().length() < 6 || registerRequest.getPassword().length() > 20) {
                Map<String, Object> details = new HashMap<>();
                details.put("minLength", 6);
                details.put("maxLength", 20);
                details.put("requiresUppercase", true);
                return AuthResponse.error("密码不符合要求", 400, "INVALID_PASSWORD_FORMAT", details);
            }

            // 检查是否包含大写字母
            if (!registerRequest.getPassword().matches(".*[A-Z].*")) {
                Map<String, Object> details = new HashMap<>();
                details.put("minLength", 6);
                details.put("maxLength", 20);
                details.put("requiresUppercase", true);
                return AuthResponse.error("密码不符合要求", 400, "INVALID_PASSWORD_FORMAT", details);
            }

            // 检查用户名是否已存在
            int userCount = userMapper.countByUsername(registerRequest.getUsername());
            if (userCount > 0) {
                return AuthResponse.error("用户名已存在，请尝试其他用户名", 409, "USERNAME_ALREADY_EXISTS");
            }

            // 插入新用户（不加密密码）
            int result = userMapper.insertUser(registerRequest.getUsername(), registerRequest.getPassword());
            if (result > 0) {
                log.info("用户注册成功: {}", registerRequest.getUsername());
                return AuthResponse.successRegister("用户注册成功");
            } else {
                return AuthResponse.error("服务器内部错误，注册失败，请稍后再试", 500, "INTERNAL_SERVER_ERROR");
            }

        } catch (Exception e) {
            log.error("用户注册异常: {}", e.getMessage(), e);
            return AuthResponse.error("服务器内部错误，注册失败，请稍后再试", 500, "INTERNAL_SERVER_ERROR");
        }
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            // 参数验证
            List<String> missingParams = new ArrayList<>();

            if (!StringUtils.hasText(loginRequest.getUsername())) {
                missingParams.add("username");
            }

            if (!StringUtils.hasText(loginRequest.getPassword())) {
                missingParams.add("password");
            }

            if (!missingParams.isEmpty()) {
                return AuthResponse.error("请求参数缺失", 400, "MISSING_PARAMETERS", missingParams);
            }

            // 验证用户名是否为数字
            try {
                Long.parseLong(loginRequest.getUsername());
            } catch (NumberFormatException e) {
                return AuthResponse.error("用户名格式不正确", 400, "INVALID_USERNAME_FORMAT");
            }

            // 查询用户
            User user = userMapper.findByUsername(loginRequest.getUsername());
            if (user == null) {
                return AuthResponse.error("用户名或密码不正确", 401, "INVALID_CREDENTIALS");
            }

            // 验证密码（直接比较，不加密）
            if (!loginRequest.getPassword().equals(user.getPassword())) {
                return AuthResponse.error("用户名或密码不正确", 401, "INVALID_CREDENTIALS");
            }

            // 生成JWT token
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());

            // 构建用户数据
            Map<String, Object> userData = new HashMap<>();
            userData.put("user", Map.of(
                "id", user.getId().toString(),
                "username", user.getUsername(),
                "email", user.getEmail() != null ? user.getEmail() : ""
            ));

            log.info("用户登录成功: {}", loginRequest.getUsername());
            return AuthResponse.successLogin(token, "登录成功", userData);

        } catch (Exception e) {
            log.error("用户登录异常: {}", e.getMessage(), e);
            return AuthResponse.error("服务器内部错误，登录失败，请稍后再试", 500, "INTERNAL_SERVER_ERROR");
        }
    }
}
