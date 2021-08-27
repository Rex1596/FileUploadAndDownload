package com.rex.api.entity;

/**
 * Author lzw
 * Create 2021/8/26
 * Description
 */
public class HttpResponse {
    private int code;
    private String content;

    public HttpResponse(int status, String content) {
        this.code = status;
        this.content = content;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toString(){
        return "[ code = " + code +
                " , content = " + content + " ]";
    }
}
