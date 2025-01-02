package cn.fengin.tiny.http;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;


import java.io.IOException;
import java.util.HashMap;


/**
 * HTTP请求包装类
 * 封装请求参数解析等功能
 *
 * @author fengin
 * @since 1.0.0
 */
public class HttpRequest {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);
    private final FullHttpRequest nettyRequest;
    private final Map<String, String> parameters = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, Object> attributes = new HashMap<>();
    @Getter
    private String body;
    
    public HttpRequest(FullHttpRequest request) {
        this.nettyRequest = request;
        parseHeaders();
        parseParameters();
        parseBody();
    }
    
    /**
     * 解析请求头
     */
    private void parseHeaders() {
        nettyRequest.headers().entries().forEach(entry -> 
            headers.put(entry.getKey().toLowerCase(), entry.getValue()));
    }
    
    /**
     * 解析请求参数
     * 包括URL查询参数和POST表单参数
     */
    private void parseParameters() {
        // 解析URL参数
        QueryStringDecoder queryDecoder = new QueryStringDecoder(nettyRequest.uri());
        queryDecoder.parameters().forEach((key, value) -> {
            if (!value.isEmpty()) {
                parameters.put(key, value.get(0));
            }
        });
            
        // 解析POST参数
        if (nettyRequest.method() == HttpMethod.POST) {
            String contentType = getHeader("content-type");
            if (contentType != null) {
                if (contentType.contains("application/x-www-form-urlencoded")) {
                    parseFormData();
                } else if (contentType.contains("multipart/form-data")) {
                    parseMultipartData();
                }
            }
        }
    }
    
    /**
     * 解析application/x-www-form-urlencoded表单数据
     */
    private void parseFormData() {
        HttpPostRequestDecoder decoder = null;
        try {
            decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), nettyRequest);
            for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) data;
                    try {
                        parameters.put(attribute.getName(), attribute.getValue());
                    } catch (IOException e) {
                        logger.error("Failed to get attribute value", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse form data", e);
        } finally {
            if (decoder != null) {
                decoder.destroy();
            }
        }
    }
    
    /**
     * 解析multipart/form-data表单数据
     */
    private void parseMultipartData() {
        HttpPostRequestDecoder decoder = null;
        try {
            decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), nettyRequest);
            for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) data;
                    try {
                        parameters.put(attribute.getName(), attribute.getValue());
                    } catch (IOException e) {
                        logger.error("Failed to get attribute value", e);
                    }
                } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                    FileUpload fileUpload = (FileUpload) data;
                    if (fileUpload.isCompleted()) {
                        // 处理文件上传，这里只存储文件名
                        parameters.put(fileUpload.getName(), fileUpload.getFilename());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse multipart data", e);
        } finally {
            if (decoder != null) {
                decoder.destroy();
            }
        }
    }
    
    /**
     * 解析请求体
     */
    private void parseBody() {
        String contentType = getHeader("content-type");
        if (contentType != null && contentType.contains("application/json")) {
            body = nettyRequest.content().toString(CharsetUtil.UTF_8);
            // 如果是JSON请求体，尝试解析JSON参数
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> jsonParams = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
                jsonParams.forEach((key, value) -> {
                    if (value != null) {
                        parameters.put(key, value.toString());
                    }
                });
            } catch (Exception e) {
                logger.debug("Failed to parse JSON body as parameters", e);
            }
        }
    }
    
    public String getParameter(String name) {
        return parameters.get(name);
    }
    
    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    public HttpMethod getMethod() {
        return nettyRequest.method();
    }
    
    public String getUri() {
        return nettyRequest.uri();
    }
    
    /**
     * 获取请求路径（不含查询参数）
     */
    public String getPath() {
        String uri = getUri();
        int queryIndex = uri.indexOf('?');
        return queryIndex > 0 ? uri.substring(0, queryIndex) : uri;
    }
    
    public Map<String, String> getParameters() {
        return new HashMap<>(parameters);
    }
    
    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }
    
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }
    
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
} 