package com.akaPierre.authsystem.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {
    
    @GetMapping("/home")
    public String userHome(
        @AuthenticationPrincipal UserDetails userDetails,
        Model model) {
            log.debug("Acesso à home do usuário: {}", userDetails.getUsername());
            
            model.addAttribute("username", userDetails.getUsername());
            model.addAttribute("userRole", "Usuário");

            return "home/user-home";
    }

    @GetMapping("/admin/home")
    public String adminHome(
        @AuthenticationPrincipal UserDetails userDetails,
        Model model) {
            log.debug("Acesso à home do admin: {}", userDetails.getUsername());

            model.addAttribute("username", userDetails.getUsername());
            model.addAttribute("userRole", "Administrador");

            return "home/admin-home";
    }

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }
}