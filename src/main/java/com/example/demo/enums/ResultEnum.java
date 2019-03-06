package com.example.demo.enums;

/**
 * Created by wang ming on 2019/2/18.
 */
public enum ResultEnum {

    SUCCESS(0, "成功"),
    NO_OBJECT(-1, "无响应对象");

    private Integer code;

    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
