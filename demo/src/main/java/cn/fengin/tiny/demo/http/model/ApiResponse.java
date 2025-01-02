package cn.fengin.tiny.demo.http.model;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * API统一响应对象，框架没提供了，开发可以参考这里
 */
public class ApiResponse {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    private int code;
    private Object data;
    private String msg;
    private boolean success;

    public static String success(Object data) {
        try {
            ApiResponse response = new ApiResponse();
            response.setCode(200);
            response.setData(data);
            response.setMsg("");
            response.setSuccess(true);
            return mapper.writeValueAsString(response);
        } catch (Exception e) {
            return error(500, "服务器内部错误");
        }
    }

    public static String error(int code, String msg) {
        try {
            ApiResponse response = new ApiResponse();
            response.setCode(code);
            response.setData(null);
            response.setMsg(msg);
            response.setSuccess(false);
            return mapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"code\":500,\"data\":null,\"msg\":\"服务器内部错误\",\"success\":false}";
        }
    }

    // Getters and Setters
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
} 