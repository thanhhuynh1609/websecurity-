package com.example.web_security.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.web_security.Repo.UsersRepository;
import com.example.web_security.model.Users;

import jakarta.annotation.PostConstruct;

@Controller
public class UserController {

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Tạo tài khoản admin mặc định
    @PostConstruct
    public void initAdmin() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            Users admin = new Users("admin", passwordEncoder.encode("admin123"),
                    "ROLE_ADMIN", "Admin", 20, "Da Nang");
            userRepository.save(admin);
        }
    }

    @GetMapping("/home")
    public String home(Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        if (role.equals("ROLE_ADMIN")) {
            return "redirect:/admin";
        } else {
            return "redirect:/user";
        }
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "logout", required = false) String logout, Model model) {
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new Users());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute Users user) {
        user.setRole("ROLE_USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/login";
    }

    @GetMapping("/admin")
    public String adminPage(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin";
    }

    @GetMapping("/user")
    public String userPage(Authentication authentication, Model model) {
        Users user = userRepository.findByUsername(authentication.getName()).get();
        model.addAttribute("user", user);
        return "user";
    }

    @GetMapping("/admin/edit/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        Users user = userRepository.findById(id).orElseThrow();
        model.addAttribute("user", user);
        return "edit";
    }

    @PostMapping("/admin/update")
public String updateUser(@ModelAttribute Users user) {
    // Lấy user hiện tại từ database dựa trên ID
    Users existingUser = userRepository.findById(user.getId()).orElseThrow(() -> new IllegalArgumentException("User not found"));

    // Cập nhật các trường khác
    existingUser.setUsername(user.getUsername());
    existingUser.setName(user.getName());
    existingUser.setAge(user.getAge());
    existingUser.setAddress(user.getAddress());
    existingUser.setRole(user.getRole());

    // Chỉ cập nhật mật khẩu nếu người dùng nhập mật khẩu mới
    if (user.getPassword() != null && !user.getPassword().isEmpty()) {
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    // Lưu lại user
    userRepository.save(existingUser);
    return "redirect:/admin";
}

    @GetMapping("/admin/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin";
    }

    @PostMapping("/user/update")
    public String updatePersonalInfo(Authentication authentication, @ModelAttribute Users user) {
        Users currentUser = userRepository.findByUsername(authentication.getName()).get();
        currentUser.setName(user.getName());
        currentUser.setAge(user.getAge());
        currentUser.setAddress(user.getAddress());
        userRepository.save(currentUser);
        return "redirect:/user";
    }
}