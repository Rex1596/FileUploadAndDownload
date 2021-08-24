package com.rex.api.entity;

import java.io.Serializable;

/**
 * Author lzw
 * Create 2021/8/23
 * Description
 */
public class FileResult implements Serializable {

    private int isSucc;
    private String msg;

    public int getIsSucc() {
        return isSucc;
    }

    public void setIsSucc(int isSucc) {
        this.isSucc = isSucc;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public FileResult() {
    }

    public FileResult(int isSucc, String msg) {
        this.isSucc = isSucc;
        this.msg = msg;
    }
}
