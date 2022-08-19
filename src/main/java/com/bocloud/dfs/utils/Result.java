package com.bocloud.dfs.utils;

import com.bocloud.dfs.constant.HttpCode;
import lombok.Data;

@Data
public class Result<T> {

    private int status;
    private String message;
    private T data;
    private long timestamp;

    public Result() {
        this.timestamp = Time.SYSTEM.milliseconds();
    }

    public static <T> Result<T> success(T data) {
        Result<T> resultData = new Result<>();
        resultData.setStatus(HttpCode.SUCCESS.getCode());
        resultData.setMessage(HttpCode.SUCCESS.getMessage());
        resultData.setData(data);
        return resultData;
    }

    public static <T> Result<T> success() {
        Result<T> resultData = new Result<>();
        resultData.setStatus(HttpCode.SUCCESS.getCode());
        resultData.setMessage(HttpCode.SUCCESS.getMessage());
        return resultData;
    }


    public static <T> Result<T> fail(int code, String message) {
        Result<T> resultData = new Result<>();
        resultData.setStatus(code);
        resultData.setMessage(message);
        return resultData;
    }

    public static <T> Result<T> fail() {
        Result<T> resultData = new Result<>();
        resultData.setStatus(HttpCode.FAILURE.getCode());
        resultData.setMessage(HttpCode.FAILURE.getMessage());
        return resultData;
    }

}
