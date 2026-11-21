package com.hanout.identity;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final IdentityService identityService;

    public AuthController(IdentityService identityService) {
        this.identityService = identityService;
    }

    public record OtpRequest(String phone) {}
    public record OtpVerifyRequest(String phone, String otp) {}

    @PostMapping("/otp/request")
    public ResponseEntity<Map<String, String>> requestOtp(@RequestBody OtpRequest request) {
        String otp = identityService.requestOtp(request.phone());
        return ResponseEntity.ok(Map.of(
                "message", "OTP sent successfully",
                "phone", request.phone(),
                "devOtp", otp
        ));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody OtpVerifyRequest request) {
        String token = identityService.verifyOtpAndLogin(request.phone(), request.otp());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/token/{userId}")
    public ResponseEntity<Map<String, String>> getTokenForTestUser(@PathVariable Long userId) {
        User user = identityService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        String token = identityService.generateTokenForUser(user);
        return ResponseEntity.ok(Map.of(
                "token", token,
                "role", user.getRole().name(),
                "name", user.getFullName()
        ));
    }
}
