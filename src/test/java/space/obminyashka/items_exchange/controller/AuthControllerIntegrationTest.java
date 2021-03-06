package space.obminyashka.items_exchange.controller;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import space.obminyashka.items_exchange.dto.UserLoginDto;
import space.obminyashka.items_exchange.dto.UserRegistrationDto;
import space.obminyashka.items_exchange.exception.BadRequestException;
import space.obminyashka.items_exchange.security.jwt.InvalidatedTokensHolder;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static space.obminyashka.items_exchange.util.JsonConverter.asJsonString;
import static space.obminyashka.items_exchange.util.MessageSourceUtil.getMessageSource;
import static space.obminyashka.items_exchange.util.MessageSourceUtil.getParametrizedMessageSource;

@SpringBootTest
@DBRider
@AutoConfigureMockMvc
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:index-reset.sql")
class AuthControllerIntegrationTest {

    protected static final String REGISTER_URL = "/auth/register";
    protected static final String LOGIN_URL = "/auth/login";
    protected static final String LOGOUT_URL = "/auth/logout";
    protected static final String VALID_USERNAME = "test";
    protected static final String VALID_EMAIL = "test@test.com";
    protected static final String VALID_PASSWORD = "Test!1234";
    protected static final String EXISTENT_USERNAME = "admin";
    protected static final String EXISTENT_EMAIL = "admin@gmail.com";
    protected static final String INVALID_PASSWORD = "test123456";
    protected static final String INVALID_EMAIL = "email.com";
    protected static final String INVALID_USERNAME = "user name";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private InvalidatedTokensHolder invalidatedTokensHolder;

    @Test
    @Commit
    @DataSet("database_init.yml")
    @ExpectedDataSet(value = "auth/register_user.yml", ignoreCols = {"password", "created", "updated", "last_online_time"})
    void register_shouldCreateValidNewUserAndReturnCreated() throws Exception {
        UserRegistrationDto validUser = new UserRegistrationDto(VALID_USERNAME, VALID_EMAIL,
                VALID_PASSWORD, VALID_PASSWORD);
        mockMvc.perform(post(REGISTER_URL)
                .content(asJsonString(validUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DataSet("database_init.yml")
    void register_whenDtoIsValid_shouldReturnSpecificSuccessMessage() throws Exception {
        UserRegistrationDto validUser = new UserRegistrationDto(VALID_USERNAME, VALID_EMAIL,
                VALID_PASSWORD, VALID_PASSWORD);
        MvcResult result = mockMvc.perform(post(REGISTER_URL)
                .content(asJsonString(validUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print()).andReturn();

        String seekingResponse = getMessageSource("user.created");
        assertTrue(result.getResponse().getContentAsString().contains(seekingResponse));
    }

    @Test
    void register_whenUserRegistrationDtoIsEmpty_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(REGISTER_URL, "")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_whenUserRegistrationDtoIsNull_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post(REGISTER_URL, (Object) null)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DataSet("database_init.yml")
    void register_whenUsernameOrEmailExists_shouldReturnUnprocessableEntityAndThrowUnprocessableEntityException()
            throws Exception {

        UserRegistrationDto existEmailUser = new UserRegistrationDto(VALID_USERNAME, EXISTENT_EMAIL,
                VALID_PASSWORD, VALID_PASSWORD);
        MvcResult result = this.mockMvc.perform(post(REGISTER_URL)
                .content(asJsonString(existEmailUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        assertThat(result.getResolvedException(), is(instanceOf(UndeclaredThrowableException.class)));
    }

    @Test
    @DataSet("database_init.yml")
    void register_whenUsernameExists_shouldReturnSpecificErrorMessage()
            throws Exception {

        UserRegistrationDto existEmailUser = new UserRegistrationDto(EXISTENT_USERNAME, VALID_EMAIL,
                VALID_PASSWORD, VALID_PASSWORD);
        MvcResult result = this.mockMvc.perform(post(REGISTER_URL)
                .content(asJsonString(existEmailUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        assertTrue(result.getResponse().getContentAsString().contains(getMessageSource("username.duplicate")));
    }

    @Test
    @DataSet("database_init.yml")
    void register_whenEmailExists_shouldReturnSpecificErrorMessage()
            throws Exception {

        UserRegistrationDto existEmailUser = new UserRegistrationDto(VALID_USERNAME, EXISTENT_EMAIL,
                VALID_PASSWORD, VALID_PASSWORD);
        MvcResult result = this.mockMvc.perform(post(REGISTER_URL)
                .content(asJsonString(existEmailUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        assertTrue(result.getResponse().getContentAsString().contains(getMessageSource("email.duplicate")));
    }

    @Test
    @DataSet("database_init.yml")
    void register_whenDifferentConfirmPassword_shouldReturnBadRequestAndThrowBadRequestException() throws Exception {
        UserRegistrationDto invalidConfirmPasswordUser = new UserRegistrationDto(VALID_USERNAME, VALID_EMAIL,
                VALID_PASSWORD, INVALID_PASSWORD);
        MvcResult result = this.mockMvc.perform(post(REGISTER_URL)
                .content(asJsonString(invalidConfirmPasswordUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        assertThat(result.getResolvedException(), is(instanceOf(BadRequestException.class)));
    }

    @Test
    @DataSet("database_init.yml")
    void register_whenDifferentConfirmPassword_shouldReturnSpecificErrorMessage()
            throws Exception {

        UserRegistrationDto existEmailUser = new UserRegistrationDto(VALID_USERNAME, VALID_EMAIL,
                VALID_PASSWORD, INVALID_PASSWORD);
        MvcResult result = this.mockMvc.perform(post(REGISTER_URL)
                .content(asJsonString(existEmailUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        String receivedMessage = Objects.requireNonNull(result.getResolvedException()).getMessage();
        assertEquals(getMessageSource("different.passwords"), receivedMessage);
    }

    @Test
    void register_whenPasswordInvalid_shouldReturnBadRequest() throws Exception {
        UserRegistrationDto invalidPasswordUser = new UserRegistrationDto(VALID_USERNAME, VALID_EMAIL,
                INVALID_PASSWORD, INVALID_PASSWORD);
        mockMvc.perform(post(REGISTER_URL)
                .content(asJsonString(invalidPasswordUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DataSet("database_init.yml")
    void register_whenPasswordInvalid_shouldReturnSpecificErrorMessage()
            throws Exception {

        UserRegistrationDto existEmailUser = new UserRegistrationDto(VALID_USERNAME, VALID_EMAIL,
                INVALID_PASSWORD, INVALID_PASSWORD);
        MvcResult result = this.mockMvc.perform(post(REGISTER_URL)
                .content(asJsonString(existEmailUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        String receivedMessage = Objects.requireNonNull(result.getResolvedException()).getMessage();
        assertTrue(receivedMessage.contains(getMessageSource("invalid.password")));
    }

    @Test
    void register_whenEmailInvalid_shouldReturnBadRequest() throws Exception {
        UserRegistrationDto invalidEmailUser = new UserRegistrationDto(VALID_USERNAME, INVALID_EMAIL,
                VALID_PASSWORD, VALID_PASSWORD);
        mockMvc.perform(post(REGISTER_URL)
                .content(asJsonString(invalidEmailUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DataSet("database_init.yml")
    void register_whenEmailInvalid_shouldReturnSpecificErrorMessage()
            throws Exception {

        UserRegistrationDto existEmailUser = new UserRegistrationDto(VALID_USERNAME, INVALID_EMAIL,
                VALID_PASSWORD, VALID_PASSWORD);
        MvcResult result = this.mockMvc.perform(post(REGISTER_URL)
                .content(asJsonString(existEmailUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        String receivedMessage = Objects.requireNonNull(result.getResolvedException()).getMessage();
        assertTrue(receivedMessage.contains(getMessageSource("invalid.email")));
    }

    @Test
    void register_whenUsernameInvalid_shouldReturnBadRequest() throws Exception {
        UserRegistrationDto invalidNameUser = new UserRegistrationDto(INVALID_USERNAME, VALID_EMAIL,
                VALID_PASSWORD, VALID_PASSWORD);

        mockMvc.perform(post(REGISTER_URL)
                .content(asJsonString(invalidNameUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DataSet("database_init.yml")
    void register_whenUsernameInvalid_shouldReturnSpecificErrorMessage()
            throws Exception {

        UserRegistrationDto existEmailUser = new UserRegistrationDto(INVALID_USERNAME, VALID_EMAIL,
                VALID_PASSWORD, VALID_PASSWORD);
        MvcResult result = this.mockMvc.perform(post(REGISTER_URL)
                .content(asJsonString(existEmailUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andReturn();

        String receivedMessage = Objects.requireNonNull(result.getResolvedException()).getMessage();
        assertTrue(receivedMessage.contains(getMessageSource("invalid.username")));
    }

    @Test
    @DataSet(value = "auth/login.yml")
    void login_Success_shouldReturnHttpOk() throws Exception {
        final String loginDto = asJsonString(new UserLoginDto(VALID_USERNAME, VALID_PASSWORD));
        mockMvc.perform(post(LOGIN_URL)
                .content(loginDto)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("{'firstname':'firstname'}"))
                .andExpect(content().json("{'lastname':'lastname'}"))
                .andExpect(content().json("{'email':'test@test.com'}"))
                .andExpect(content().json("{'avatarImage':'dGVzdCBpbWFnZSBwbmc='}"));
    }

    @Test
    @DataSet(value = "auth/login.yml")
    void logout_Success_ShouldReturnNoContent() throws Exception {
        final String token = obtainToken(new UserLoginDto(VALID_USERNAME, VALID_PASSWORD));
        mockMvc.perform(post(LOGOUT_URL)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DataSet(value = "auth/login.yml")
    void logout_Success_ShouldBeInvalidatedInInvalidatedTokensHolder() throws Exception {
        final String token = obtainToken(new UserLoginDto(VALID_USERNAME, VALID_PASSWORD));
        mockMvc.perform(post(LOGOUT_URL)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print());
        assertTrue(invalidatedTokensHolder.isInvalidated(token));
    }

    @Test
    @DataSet(value = "auth/login.yml")
    void logout_Failure_ShouldThrowJwtExceptionAfterRequestWithInvalidToken() throws Exception{
        final String token = "DefinitelyNotValidToken";
        mockMvc.perform(post(LOGOUT_URL)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    private String obtainToken(UserLoginDto loginDto) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                .content(asJsonString(loginDto))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }
}
