package com.bocloud.dfs.constant;

public enum HttpCode {

    /**
     * 操作成功
     **/
    SUCCESS(200, "操作成功"),
    FAILURE(1001, "操作失败"),
    INVALID_TOKEN(2001, "访问令牌不合法");

    /**
     * 自定义状态码
     **/
    private final int code;
    /**
     * 自定义描述
     **/
    private final String message;

    HttpCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }


}
