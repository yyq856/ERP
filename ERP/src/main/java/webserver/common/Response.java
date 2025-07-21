package webserver.common;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // 自动生成 getter、setter、toString、equals、hashCode 方法
@NoArgsConstructor // 无参构造器
@AllArgsConstructor // 全参构造器
public class Response<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Response<T> success(T data) {
        return new Response<>(200, "success", data);
    }

    public static <T> Response<T> error(String message) {
        return new Response<>(500, message, null);
    }
}
