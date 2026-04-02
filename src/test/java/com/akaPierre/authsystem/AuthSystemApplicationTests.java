package com.akaPierre.authsystem;

import com.akaPierre.authsystem.model.Role;
import com.akaPierre.authsystem.model.User;
import com.akaPierre.authsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthSystemApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

	@Autowired
	private WebApplicationContext context;

	@BeforeEach
	void setUpMvc() {
		mockMvc = MockMvcBuilders
			.webAppContextSetup(context)
			.apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
			.build();
	}

    @BeforeEach
    void setUp() {
        if (!userRepository.existsByEmail("user@teste.com")) {
            userRepository.save(new User(
                    "Usuário Teste",
                    "user@teste.com",
                    passwordEncoder.encode("Senha@123"),
                    Role.ROLE_USER
            ));
        }
        if (!userRepository.existsByEmail("admin@sistema.com")) {
            userRepository.save(new User(
                    "Administrador",
                    "admin@sistema.com",
                    passwordEncoder.encode("Admin@1234"),
                    Role.ROLE_ADMIN
            ));
        }
    }

    @Test
    @Order(1)
    @DisplayName("Contexto Spring deve carregar sem erros")
    void contextLoads() {}

    @Test
    @Order(2)
    @DisplayName("GET /login deve retornar 200 para usuário não autenticado")
    void loginPage_deveRetornar200_semAutenticacao() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @Order(3)
    @DisplayName("GET /register deve retornar 200 para usuário não autenticado")
    void registerPage_deveRetornar200_semAutenticacao() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }

    @Test
    @Order(4)
    @DisplayName("GET /home deve redirecionar para /login quando não autenticado")
    void home_deveRedirecionar_semAutenticacao() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @Order(5)
    @DisplayName("GET /admin/home deve redirecionar para /login quando não autenticado")
    void adminHome_deveRedirecionar_semAutenticacao() throws Exception {
        mockMvc.perform(get("/admin/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @Order(6)
    @DisplayName("Login com credenciais válidas de usuário comum deve autenticar")
    void login_comCredenciaisValidas_deveAutenticar() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .userParameter("email")
                        .user("user@teste.com")
                        .password("Senha@123"))
                .andExpect(authenticated()
                        .withUsername("user@teste.com")
                        .withRoles("USER"));
    }

    @Test
    @Order(7)
    @DisplayName("Login com credenciais válidas de admin deve autenticar como ADMIN")
    void login_comCredenciaisAdmin_deveAutenticarComoAdmin() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .userParameter("email")
                        .user("admin@sistema.com")
                        .password("Admin@1234"))
                .andExpect(authenticated()
                        .withUsername("admin@sistema.com")
                        .withRoles("ADMIN"));
    }

    @Test
    @Order(8)
    @DisplayName("Login com senha errada deve retornar unauthenticated")
    void login_comSenhaErrada_deveRetornarUnauthenticated() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .userParameter("email")
                        .user("user@teste.com")
                        .password("senhaErrada"))
                .andExpect(unauthenticated());
    }

    @Test
    @Order(9)
    @DisplayName("Login com e-mail inexistente deve retornar unauthenticated")
    void login_comEmailInexistente_deveRetornarUnauthenticated() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .userParameter("email")
                        .user("naoexiste@email.com")
                        .password("qualquersenha"))
                .andExpect(unauthenticated());
    }

    @Test
    @Order(10)
    @DisplayName("GET /home com ROLE_USER deve retornar 200")
    @WithMockUser(username = "user@teste.com", roles = "USER")
    void home_comRoleUser_deveRetornar200() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/user-home"));
    }

    @Test
    @Order(11)
    @DisplayName("GET /admin/home com ROLE_ADMIN deve retornar 200")
    @WithMockUser(username = "admin@sistema.com", roles = "ADMIN")
    void adminHome_comRoleAdmin_deveRetornar200() throws Exception {
        mockMvc.perform(get("/admin/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home/admin-home"));
    }

    @Test
    @Order(12)
    @DisplayName("GET /admin/home com ROLE_USER deve retornar 403")
    @WithMockUser(username = "user@teste.com", roles = "USER")
    void adminHome_comRoleUser_deveRetornar403() throws Exception {
        mockMvc.perform(get("/admin/home"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(13)
    @DisplayName("GET /home com ROLE_ADMIN deve retornar 403")
    @WithMockUser(username = "admin@sistema.com", roles = "ADMIN")
    void home_comRoleAdmin_deveRetornar403() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(14)
    @DisplayName("POST /register com dados válidos deve redirecionar para /login")
    void register_comDadosValidos_deveRedirecionarParaLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Novo Usuário")
                        .param("email", "novo@teste.com")
                        .param("password", "Senha@456")
                        .param("confirmPassword", "Senha@456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @Order(15)
    @DisplayName("POST /register com e-mail duplicado deve retornar formulário com erro")
    void register_comEmailDuplicado_deveRetornarFormularioComErro() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Outro Usuário")
                        .param("email", "user@teste.com")
                        .param("password", "Senha@789")
                        .param("confirmPassword", "Senha@789"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @Order(16)
    @DisplayName("POST /register com senhas divergentes deve retornar formulário com erro")
    void register_comSenhasDivergentes_deveRetornarFormularioComErro() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Usuário X")
                        .param("email", "x@teste.com")
                        .param("password", "Senha@111")
                        .param("confirmPassword", "SenhaDiferente@222"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @Order(17)
    @DisplayName("POST /register com campos vazios deve retornar erros de validação")
    void register_comCamposVazios_deveRetornarErrosValidacao() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")
                        .param("email", "email-invalido")
                        .param("password", "123")
                        .param("confirmPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors(
                        "registrationDto", "name", "email", "password", "confirmPassword"));
    }

    @Test
    @Order(18)
    @DisplayName("POST /logout com usuário autenticado deve redirecionar para /login")
    @WithMockUser(username = "user@teste.com", roles = "USER")
    void logout_comUsuarioAutenticado_deveRedirecionarParaLogin() throws Exception {
        mockMvc.perform(logout("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"))
                .andExpect(unauthenticated());
    }

    @Test
    @Order(19)
    @DisplayName("POST /register sem token CSRF deve retornar 403")
    void register_semCSRF_deveRetornar403() throws Exception {
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Teste")
                        .param("email", "csrf@teste.com")
                        .param("password", "Senha@999")
                        .param("confirmPassword", "Senha@999"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(20)
    @DisplayName("Senha do usuário deve estar hasheada com BCrypt no banco")
    void senha_deveestarHasheadaComBCrypt() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Teste Hash")
                        .param("email", "hash@teste.com")
                        .param("password", "Senha@Hash1")
                        .param("confirmPassword", "Senha@Hash1"))
                .andExpect(status().is3xxRedirection());

        User user = userRepository.findByEmail("hash@teste.com").orElseThrow();

        assert !user.getPassword().equals("Senha@Hash1")
                : "FALHA DE SEGURANÇA: senha em texto puro no banco!";

        assert user.getPassword().startsWith("$2a$12$")
                : "Hash não é BCrypt com strength 12";

        assert passwordEncoder.matches("Senha@Hash1", user.getPassword())
                : "BCrypt.matches() falhou — hash inválido";
    }
}