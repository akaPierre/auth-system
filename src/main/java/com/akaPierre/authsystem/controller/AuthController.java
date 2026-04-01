package com.akaPierre.authsystem.controller;

import com.akaPierre.authsystem.dto.UserRegistrationDto;
import com.akaPierre.authsystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;

    @GetMapping("/login")
    public String showLoginPage(
        Authentication authentication,
        Model model) {
            if (isAuthenticated(authentication)) {
                return redirectByRole(authentication);
            }

            return "auth/login";
    }

    @GetMapping("/register")
    public String showRegisterPage(
        Authentication authentication,
        Model model) {
            if (isAuthenticated(authentication)) {
                return redirectByRole(authentication);
            }

            if (!model.containsAttribute("registrationDto")) {
                model.addAttribute("registrationDto", new UserRegistrationDto());
            }

            return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(
        @Valid @ModelAttribute("registrationDto") UserRegistrationDto dto,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes,
        Model model) {
            if (bindingResult.hasErrors()) {
                log.debug("Erros de validação no formulário de cadastro: {}", bindingResult.getAllErrors());
                return "auth/register";
            }

            try {
                userService.registerUser(dto);
                log.info("Novo usuário cadastrado via formulário: {}", dto.getEmail());

                redirectAttributes.addFlashAttribute("successMessage", "Cadastro realizado com sucesso! Faça login para continuar.");
                return "redirect:/login";
            } catch (IllegalArgumentException e) {
                model.addAttribute("errorMessage", e.getMessage());
                return "auth/register";
            }
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);
    }

    private String redirectByRole(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return isAdmin ? "redirect:/admin/home" : "redirect:/home";
    }
}