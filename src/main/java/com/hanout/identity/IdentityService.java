package com.hanout.identity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class IdentityService {

    private final UserRepository userRepository;
    private final PhoneValidator phoneValidator;
    private final JwtService jwtService;

    public IdentityService(UserRepository userRepository, PhoneValidator phoneValidator, JwtService jwtService) {
        this.userRepository = userRepository;
        this.phoneValidator = phoneValidator;
        this.jwtService = jwtService;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByPhone(String phone) {
        String formatted = phoneValidator.formatE164(phone);
        return userRepository.findByPhone(formatted);
    }

    @Transactional
    public String requestOtp(String phone) {
        String formatted = phoneValidator.formatE164(phone);
        User user = userRepository.findByPhone(formatted).orElseGet(() -> {
            User newUser = new User();
            newUser.setPhone(formatted);
            newUser.setFullName("User " + formatted.substring(Math.max(0, formatted.length() - 4)));
            newUser.setRole(Role.BUYER);
            return userRepository.save(newUser);
        });

        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        user.setOtpCode(otp);
        user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        return otp; // In production this is sent via SMS port; returning here for testing/dev flow
    }

    @Transactional
    public String verifyOtpAndLogin(String phone, String otp) {
        String formatted = phoneValidator.formatE164(phone);
        User user = userRepository.findByPhone(formatted)
                .orElseThrow(() -> new IllegalArgumentException("User not found with phone: " + phone));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp)) {
            throw new IllegalArgumentException("Invalid OTP code");
        }
        if (user.getOtpExpiresAt() == null || user.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP code has expired");
        }

        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        userRepository.save(user);

        return jwtService.generateToken(user);
    }

    public String generateTokenForUser(User user) {
        return jwtService.generateToken(user);
    }
}
