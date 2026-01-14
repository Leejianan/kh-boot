package com.kh.boot.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Standard API Response")
public class Result<T> {

    @Schema(description = "Status Code (200: Success, Other: Error)", example = "200")
    private Integer code;

    @Schema(description = "Message", example = "Success")
    private String msg;

    @Schema(description = "Data Payload")
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return result(200, "Success", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return result(200, msg, data);
    }

    public static <T> Result<T> error(String msg) {
        return result(500, msg, null);
    }

    public static <T> Result<T> error(int code, String msg) {
        return result(code, msg, null);
    }

    private static <T> Result<T> result(int code, String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}
