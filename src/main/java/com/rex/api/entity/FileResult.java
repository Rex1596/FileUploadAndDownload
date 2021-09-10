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
    private String fileName;

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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public FileResult(int isSucc, String msg, String fileName) {
        this.isSucc = isSucc;
        this.msg = msg;
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "FileResult{" +
                "isSucc=" + isSucc +
                ", msg='" + msg + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
