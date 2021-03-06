package space.obminyashka.items_exchange.service;

import static org.springframework.http.MediaType.*;

public enum SupportedMediaTypes {
    JPEG(IMAGE_JPEG_VALUE),
    JPG("image/jpg"),
    PNG(IMAGE_PNG_VALUE),
    GIF(IMAGE_GIF_VALUE);

    String mediaType;

    SupportedMediaTypes(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMediaType() {
        return mediaType;
    }
}
