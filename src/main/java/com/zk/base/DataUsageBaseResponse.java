package com.zk.base;

public class DataUsageBaseResponse {

    private String code;
    private String message;
    private String taskId;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "DataUsageBaseResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", taskId='" + taskId + '\'' +
                '}';
    }
}
