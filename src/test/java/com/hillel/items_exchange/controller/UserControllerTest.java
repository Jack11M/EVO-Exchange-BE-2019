package com.hillel.items_exchange.controller;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.spring.api.DBRider;
import com.hillel.items_exchange.dto.UserChangeEmailDto;
import com.hillel.items_exchange.dto.UserChangePasswordDto;
import com.hillel.items_exchange.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.transaction.Transactional;
import java.util.Objects;

import static com.hillel.items_exchange.util.ChildDtoCreatingUtil.getJsonOfChildrenDto;
import static com.hillel.items_exchange.util.JsonConverter.asJsonString;
import static com.hillel.items_exchange.util.MessageSourceUtil.getExceptionMessageSource;
import static com.hillel.items_exchange.util.MessageSourceUtil.getExceptionParametrizedMessageSource;
import static com.hillel.items_exchange.util.UserDtoCreatingUtil.*;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DBRider
@AutoConfigureMockMvc
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:index-reset.sql")
class UserControllerTest {

    public static final String PATH_USER_INFO_CHANGE_PASSWORD = "/user/info/change-password";
    public static final String PATH_USER_INFO_CHANGE_EMAIL = "/user/info/change-email";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${max.children.amount}")
    private int maxChildrenAmount;

    private String validCreatingChildDtoJson;
    private String notValidCreatingChildDtoJson;
    private String validUpdatingChildDtoJson;
    private String notValidUpdatingChildDtoJson;
    private String badTotalAmountChildDtoJson;
    private String badAmountChildDtoJson;

    @BeforeEach
    void setUp() {
        validCreatingChildDtoJson = getJsonOfChildrenDto(0L, 0L, 2019);
        notValidCreatingChildDtoJson = getJsonOfChildrenDto(111L, 222L, 2019);
        validUpdatingChildDtoJson = getJsonOfChildrenDto(1L, 2L, 2018);
        notValidUpdatingChildDtoJson = getJsonOfChildrenDto(1L, 999L, 2018);
        badTotalAmountChildDtoJson = getJsonOfChildrenDto(maxChildrenAmount - 1);
        badAmountChildDtoJson = getJsonOfChildrenDto(maxChildrenAmount + 1);
    }

    @Test
    @WithMockUser(username = "admin")
    @Transactional
    @DataSet("database_init.yml")
    void getUserDto_shouldReturnUserDtoIfExists() throws Exception {
        mockMvc.perform(get("/user/my-info")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    @DataSet("database_init.yml")
    void negativeTestReceivingInformationAboutAnotherUser() throws Exception {
        mockMvc.perform(get("/user/my-info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "admin")
    @Transactional
    @DataSet("database_init.yml")
    @ExpectedDataSet(value = "user/update.yml", ignoreCols = {"last_online_time", "updated"})
    void updateUserInfo_shouldUpdateUserData() throws Exception {
        getResultActions(HttpMethod.PUT, "/user/info",
                createUserDtoForUpdatingWithChangedEmailAndFNameApAndLNameMinusWithoutPhones(), status().isAccepted())
                .andDo(print())
                .andExpect(jsonPath("$.email").value(NEW_VALID_EMAIL))
                .andExpect(jsonPath("$.firstName").value(NEW_VALID_NAME_WITH_APOSTROPHE))
                .andExpect(jsonPath("$.lastName").value(NEW_VALID_NAME_WITH_HYPHEN_MINUS));
    }

    @Test
    @WithMockUser(username = "admin")
    @Transactional
    @DataSet("database_init.yml")
    void updateUserInfo_shouldReturn403WhenUsernameIsChanged() throws Exception {
        MvcResult result = getResultActions(HttpMethod.PUT, "/user/info",
                createUserDtoForUpdatingWithChangedUsernameWithoutPhones(), status().isForbidden())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(
                getExceptionMessageSource("exception.illegal.field.change") + "username"));
    }

    @Test
    @WithMockUser(username = "admin")
    @Transactional
    @DataSet("database_init.yml")
    void updateUserInfo_shouldReturn403WhenLastOnlineTimeIsChanged() throws Exception {
        MvcResult result = getResultActions(HttpMethod.PUT, "/user/info",
                createUserDtoForUpdatingWithChangedLastOnlineTimeWithoutPhones(), status().isForbidden())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(
                getExceptionMessageSource("exception.illegal.field.change") + "lastOnlineTime"));
    }

    @Test
    @WithMockUser(username = "admin")
    @Transactional
    @DataSet("database_init.yml")
    void updateUserInfo_shouldReturn403WhenChildrenAreChanged() throws Exception {
        MvcResult result = getResultActions(HttpMethod.PUT, "/user/info",
                createUserDtoForUpdatingWithChangedChildrenWithoutPhones(), status().isForbidden())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(
                getExceptionMessageSource("exception.illegal.field.change") + "children"));
    }

    @Test
    @WithMockUser(username = "admin")
    @Transactional
    @DataSet("database_init.yml")
    void updateUserInfo_shouldReturn403WhenPhonesAreChanged() throws Exception {
        MvcResult result = getResultActions(HttpMethod.PUT, "/user/info",
                createUserDtoForUpdatingWithPhones(), status().isForbidden())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains(
                getExceptionMessageSource("exception.illegal.field.change") + "phones"));
    }

    @Test
    @WithMockUser(username = "admin")
    @Transactional
    @DataSet("database_init.yml")
    void updateUserInfo_shouldReturn400WhenShortFirstName() throws Exception {
        getResultActions(HttpMethod.PUT, "/user/info",
                createUserDtoForUpdatingWithInvalidShortFNameWithoutPhones(), status().isBadRequest())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "admin")
    @Transactional
    @DataSet("database_init.yml")
    void updateUserInfo_shouldReturn400WhenLastNameContainsTwoWords() throws Exception {
        getResultActions(HttpMethod.PUT, "/user/info",
                createUserDtoForUpdatingWithInvalidLNameWithoutPhones(), status().isBadRequest())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "admin")
    @DataSet({"database_init.yml", "user/update_init.yml"})
    void updateUserInfo_shouldReturn400WhenDuplicateEmail() throws Exception {
        getResultActions(HttpMethod.PUT, "/user/info",
                createUserDtoForUpdatingWithDuplicateEmailWithoutPhones(), status().isBadRequest())
                .andReturn();
    }

    private ResultActions getResultActions(HttpMethod httpMethod, String path,
                                           UserDto dto, ResultMatcher matcher) throws Exception {
        MockHttpServletRequestBuilder builder = request(httpMethod, path)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto));
        return mockMvc.perform(builder).andExpect(matcher);
    }

    @Test
    @WithMockUser(username = "admin")
    @DataSet("database_init.yml")
    void getChildren_Success_ShouldReturnUsersChildren() throws Exception {
        mockMvc.perform(get("/user/child/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].sex").value("MALE"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].sex").value("FEMALE"));
    }

    @Test
    @WithMockUser(username = "admin")
    @Transactional
    @DataSet("database_init.yml")
    @ExpectedDataSet(value = "/children/create.yml", ignoreCols = {"birth_date", "sex"})
    void addChildren_Success_ShouldReturnHttpStatusOk() throws Exception {
        mockMvc.perform(post("/user/child/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreatingChildDtoJson)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("3"))
                .andExpect(jsonPath("$[0].birthDate").value("2019-03-03"))
                .andExpect(jsonPath("$[0].sex").value("MALE"))
                .andExpect(jsonPath("$[1].id").value("4"))
                .andExpect(jsonPath("$[1].birthDate").value("2019-04-04"))
                .andExpect(jsonPath("$[1].sex").value("FEMALE"));
    }

    @Test
    @WithMockUser(username = "admin")
    @Transactional
    @DataSet("database_init.yml")
    @ExpectedDataSet(value = "children/delete.yml")
    void removeChild_Success_ShouldReturnHttpStatusOk() throws Exception {
        mockMvc.perform(delete("/user/child/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin")
    @Transactional
    @DataSet("database_init.yml")
    @ExpectedDataSet(value = "children/update.yml")
    void updateChild_Success_ShouldReturnHttpStatusOk() throws Exception {
        mockMvc.perform(put("/user/child/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validUpdatingChildDtoJson)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].birthDate").value("2018-03-03"))
                .andExpect(jsonPath("$[0].sex").value("MALE"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].birthDate").value("2018-04-04"))
                .andExpect(jsonPath("$[1].sex").value("FEMALE"));
    }

    @Test
    @WithMockUser(username = "admin")
    @DataSet("database_init.yml")
    void addChild_NotValidDto_ShouldThrowIllegalIdentifierException() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(post("/user/child/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(notValidCreatingChildDtoJson)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        assertTrue(mvcResult.getResponse().getContentAsString()
                .contains(getExceptionMessageSource("invalid.new.entity.id")));
    }

    @Test
    @WithMockUser(username = "admin")
    @DataSet("database_init.yml")
    void updateChild_NotValidDto_ShouldThrowIllegalIdentifierException() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(put("/user/child/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(notValidUpdatingChildDtoJson)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        assertTrue(mvcResult.getResponse().getContentAsString().contains("Not all children from dto present in"));
    }

    @Test
    @WithMockUser(username = "admin")
    @DataSet("database_init.yml")
    void addChild_BadAmount_ShouldThrowConstraintViolationException() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(post("/user/child")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badAmountChildDtoJson)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
        assertTrue(Objects.requireNonNull(mvcResult.getResolvedException()).getMessage()
                .contains(getExceptionMessageSource("exception.invalid.dto")));
    }

    @Test
    @WithMockUser(username = "admin")
    @DataSet("database_init.yml")
    void addChild_BadTotalAmount_ShouldThrowEntityAmountException() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(post("/user/child/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badTotalAmountChildDtoJson)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andReturn();
        assertTrue(Objects.requireNonNull(mvcResult.getResolvedException()).getMessage()
                .contains(getExceptionParametrizedMessageSource("exception.children-amount",
                        maxChildrenAmount)));
    }

    @Test
    @WithMockUser(username = "test")
    @Transactional
    @DataSet({"database_init.yml", "user/update_init.yml"})
    void updateUserInfo_shouldReturn403WhenUpdatedUserContainsNewChildren() throws Exception {
        MvcResult result = getResultActions(HttpMethod.PUT, "/user/info",
                createUserDtoForUpdatingWithChildrenWithoutPhones(), status().isForbidden())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString()
                .contains(getExceptionMessageSource("exception.illegal.children.phones.change")));
    }

    @Test
    @WithMockUser(username = "test")
    @Transactional
    @DataSet({"database_init.yml", "user/update_init.yml"})
    void updateUserInfo_shouldReturn403WhenUpdatedUserContainsNewPhone() throws Exception {
        MvcResult result = getResultActions(HttpMethod.PUT, "/user/info",
                createUserDtoForUpdatingWithPhoneWithoutChildren(), status().isForbidden())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString()
                .contains(getExceptionMessageSource("exception.illegal.children.phones.change")));
    }

    @Test
    @WithMockUser(username = "new_user")
    @Transactional
    @DataSet({"database_init.yml", "user/update_init.yml"})
    @ExpectedDataSet(value = {"database_init.yml", "user/children_phones_update.yml"}, ignoreCols = "updated")
    void updateUserInfo_shouldUpdateUserDataWithNewChildrenAndPhones() throws Exception {
        getResultActions(HttpMethod.PUT, "/user/info",
                createUserDtoForUpdatingWithNewChildAndPhones(), status().isAccepted())
                .andDo(print())
                .andExpect(jsonPath("$.children", hasSize(1)))
                .andExpect(jsonPath("$.phones", hasSize(1)));
            }

    @Test
    @WithMockUser(username = "trump")
    @Transactional
    @DataSet({"database_init.yml", "user/changing_password_or_email/changing_password_or_email_init.yml"})
    @ExpectedDataSet(value = {"database_init.yml",
            "user/changing_password_or_email/changing_password_or_email_expected.yml"},
            ignoreCols = {"password", "email", "lastOnlineTime", "updated"})
    void updateUserPassword_Successfully() throws Exception {
        getResultActionsPasswordDto(HttpMethod.PUT, PATH_USER_INFO_CHANGE_PASSWORD,
                createUserChangePasswordDtoWithCorrectData(), status().isAccepted())
                .andDo(print());
        assertTrue(bCryptPasswordEncoder.matches(
                createUserChangePasswordDtoWithCorrectData().getNewPassword(),
                "$2a$10$cQvCmoVUukbO/1vTCxeu3OmwJBwAf1S5wabTrL8vUsEZn5DBzlYLW"));
    }

    @Test
    @WithMockUser(username = "trump")
    @Transactional
    @DataSet({"database_init.yml", "user/changing_password_or_email/changing_password_or_email_init.yml"})
    void updateUserPassword_WhenOldPasswordWrong_ShouldThrowInvalidDtoException() throws Exception {
        final MvcResult mvcResult = getResultActionsPasswordDto(HttpMethod.PUT,
                PATH_USER_INFO_CHANGE_PASSWORD,
                createUserChangePasswordDtoWithWrongOldPassword(), status().isBadRequest())
                .andDo(print())
                .andReturn();
        assertTrue(mvcResult.getResponse().getContentAsString()
                .contains(getExceptionMessageSource("not.your.password")));
    }

    @Test
    @WithMockUser(username = "trump")
    @Transactional
    @DataSet({"database_init.yml", "user/changing_password_or_email/changing_password_or_email_init.yml"})
    void updateUserPassword_WhenPasswordConfirmationWrong_ShouldThrowIllegalArgumentException()
            throws Exception {
        final MvcResult mvcResult = getResultActionsPasswordDto(HttpMethod.PUT,
                PATH_USER_INFO_CHANGE_PASSWORD,
                createUserChangePasswordDtoWithWrongPasswordConfirmation(), status().isBadRequest())
                .andDo(print())
                .andReturn();

        final String message = Objects.requireNonNull(mvcResult.getResolvedException()).getMessage();

        assertTrue(message.contains(getExceptionMessageSource("invalid.confirm.password")));
    }

    @Test
    @WithMockUser(username = "trump")
    @Transactional
    @DataSet({"database_init.yml", "user/changing_password_or_email/changing_password_or_email_init.yml"})
    @ExpectedDataSet(value = {"database_init.yml",
            "user/changing_password_or_email/changing_password_or_email_expected.yml"},
            ignoreCols = {"password", "lastOnlineTime", "updated"})
    void updateUserEmail_Successfully() throws Exception {
        getResultActionsEmailDto(HttpMethod.PUT, PATH_USER_INFO_CHANGE_EMAIL,
                createUserChangeEmailDtoWithCorrectData(), status().isAccepted())
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "trump")
    @Transactional
    @DataSet({"database_init.yml", "user/changing_password_or_email/changing_password_or_email_init.yml"})
    void updateUserEmail_WhenEmailConfirmationWrong_ShouldThrowIllegalArgumentException()
            throws Exception {
        final MvcResult mvcResult = getResultActionsEmailDto(HttpMethod.PUT,
                PATH_USER_INFO_CHANGE_EMAIL,
                createUserChangeEmailDtoWithWrongEmailConfirmation(), status().isBadRequest())
                .andDo(print())
                .andReturn();

        final String message = Objects.requireNonNull(mvcResult.getResolvedException()).getMessage();

        assertTrue(message.contains(getExceptionMessageSource("invalid.confirm.email")));
    }

    private ResultActions getResultActionsPasswordDto(HttpMethod httpMethod, String path,
                                                      UserChangePasswordDto dto,
                                                      ResultMatcher matcher) throws Exception {
        MockHttpServletRequestBuilder builder = request(httpMethod, path)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto));
        return mockMvc.perform(builder).andExpect(matcher);
    }

    private ResultActions getResultActionsEmailDto(HttpMethod httpMethod, String path,
                                                   UserChangeEmailDto dto,
                                                   ResultMatcher matcher) throws Exception {
        MockHttpServletRequestBuilder builder = request(httpMethod, path)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(dto));
        return mockMvc.perform(builder).andExpect(matcher);
    }
}
