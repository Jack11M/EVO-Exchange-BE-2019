package space.obminyashka.items_exchange.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import space.obminyashka.items_exchange.exception.ElementsNumberExceedException;
import space.obminyashka.items_exchange.exception.IllegalIdentifierException;
import space.obminyashka.items_exchange.exception.IllegalOperationException;
import space.obminyashka.items_exchange.exception.UnsupportedMediaTypeException;
import space.obminyashka.items_exchange.model.Advertisement;
import space.obminyashka.items_exchange.model.Image;
import space.obminyashka.items_exchange.model.User;
import space.obminyashka.items_exchange.model.enums.Status;
import space.obminyashka.items_exchange.service.AdvertisementService;
import space.obminyashka.items_exchange.service.ImageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static space.obminyashka.items_exchange.util.MessageSourceUtil.getMessageSource;
import static space.obminyashka.items_exchange.util.MessageSourceUtil.getParametrizedMessageSource;

@SpringBootTest
@AutoConfigureMockMvc
class ImageControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AdvertisementService advertisementService;
    @MockBean
    private ImageService imageService;
    @Mock
    private Advertisement advertisement;
    @Mock
    private User user;
    @Captor
    private ArgumentCaptor<List<byte[]>> listArgumentCaptor;
    private ArrayList<Image> testImages;
    private MockMultipartFile jpeg;

    @BeforeEach
    void setUp() throws IOException, UnsupportedMediaTypeException {
        user = new User();
        user.setStatus(Status.ACTIVE);
        jpeg = new MockMultipartFile("image", "image-jpeg.jpeg", MediaType.IMAGE_JPEG_VALUE, "image jpeg".getBytes());
        mocksInit();
    }

    private void mocksInit() throws IOException, UnsupportedMediaTypeException {
        when(advertisementService.existById(1L)).thenReturn(true);
        when(advertisementService.findByIdAndOwnerUsername(1L, "admin")).thenReturn(Optional.of(advertisement));
        testImages = IntStream.range(0, 10)
                .collect(ArrayList::new, (images, value) -> images.add(new Image()), ArrayList::addAll);
        when(advertisement.getImages()).thenReturn(testImages);
        when(advertisement.getUser()).thenReturn(user);
        when(imageService.compress(List.of(jpeg))).thenReturn(List.of(jpeg.getBytes()));
    }

    @WithMockUser("admin")
    @Test
    void saveImages_shouldThrowExceptionWhenTotalAmountMoreThan10() throws Exception {
        MvcResult mvcResult = mockMvc.perform(multipart("/image/{advertisement_id}", 1L)
                .file(jpeg)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andReturn();

        verify(advertisementService).existById(anyLong());
        verify(advertisementService).findByIdAndOwnerUsername(anyLong(), anyString());
        assertThat(mvcResult.getResolvedException(), is(instanceOf(ElementsNumberExceedException.class)));
    }

    @WithMockUser("admin")
    @Test
    void saveImages_shouldSaveImagesWhenTotalAmountLessThan10() throws Exception {
        testImages.remove(0);

        mockMvc.perform(multipart("/image/{advertisement_id}", 1L)
                .file(jpeg)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        verify(imageService).compress(anyList());
        verify(imageService).saveToAdvertisement(any(), listArgumentCaptor.capture());
        verify(advertisementService).findByIdAndOwnerUsername(anyLong(), anyString());
        assertEquals(jpeg.getBytes(), listArgumentCaptor.getValue().get(0));
    }

    @WithMockUser("admin")
    @Test
    void saveImages_shouldReturn404WhenAdvertisementIsNotExist() throws Exception {
        mockMvc.perform(multipart("/image/{advertisement_id}", 50L)
                .file(jpeg))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @WithMockUser("admin")
    @Test
    void deleteImages_shouldThrow400WhenImageIdNotExist() throws Exception {
        final MvcResult mvcResult = sendDeleteRequest("999", status().isBadRequest());

        verify(advertisementService).findByIdAndOwnerUsername(1L, "admin");
        verifyResultException(mvcResult, IllegalIdentifierException.class, getParametrizedMessageSource("exception.image.not-existed-id", "[999]"));
    }

    @WithMockUser
    @Test
    void deleteImages_shouldThrow403WhenUserNotOwnAdvertisement() throws Exception {
        final MvcResult mvcResult = sendDeleteRequest("1", status().isForbidden());

        verify(advertisementService).findByIdAndOwnerUsername(1L, "user");
        verifyResultException(mvcResult, IllegalOperationException.class, getMessageSource("user.not-owner"));
    }

    private MvcResult sendDeleteRequest(String imageId, ResultMatcher status) throws Exception {
        return mockMvc.perform(delete("/image/{advertisement_id}", 1L)
                .param("ids", imageId))
                .andDo(print())
                .andExpect(status)
                .andReturn();
    }

    private void verifyResultException(MvcResult mvcResult, Class<? extends Exception> exceptionClass, String exceptionMessage) {
        final var resolvedException = mvcResult.getResolvedException();
        assertNotNull(resolvedException);
        assertThat(resolvedException, is(instanceOf(exceptionClass)));
        assertEquals(exceptionMessage, resolvedException.getLocalizedMessage());
    }
}
