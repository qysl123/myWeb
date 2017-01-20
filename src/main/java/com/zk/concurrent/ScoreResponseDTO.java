package com.zk.concurrent;

/**
 * 积分系统操作返回类
 * Created by Ken on 2016/11/14.
 */
public class ScoreResponseDTO {

    private boolean success;
    private String message;
    private ScoreExecuteResultDTO data;

    public ScoreResponseDTO(boolean success, String message, ScoreExecuteResultDTO data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ScoreResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ScoreResponseDTO(boolean success) {
        this.success = success;
    }

    public ScoreResponseDTO() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ScoreExecuteResultDTO getData() {
        return data;
    }

    public void setData(ScoreExecuteResultDTO data) {
        this.data = data;
    }

    public void setFailMessage(String message) {
        this.success = false;
        this.message = message;
    }

    public void setSuccessMessage(String message) {
        this.success = true;
        this.message = message;
    }
}
