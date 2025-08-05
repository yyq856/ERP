package webserver.util;

import java.util.Base64;

public class ImageUtil {

    public static String convertToBase64Url(byte[] imageData) {
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageData);
    }
}
