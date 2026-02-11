package com.example.mangxahoi.Controller;

import com.example.mangxahoi.Entity.UserEntity;
import com.example.mangxahoi.Repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributesController {

    private final UserRepository userRepository;

    public GlobalModelAttributesController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute
    public void addUserInfo(Model model, Authentication auth) {
        if (auth != null) {
            String username = auth.getName();
            UserEntity userEntity = userRepository.findByUsername(username).orElse(null);
            model.addAttribute("avatar", userEntity.getAvatar());
        }
    }
}
