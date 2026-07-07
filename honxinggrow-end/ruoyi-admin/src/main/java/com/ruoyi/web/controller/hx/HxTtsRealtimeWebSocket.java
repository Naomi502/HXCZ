package com.ruoyi.web.controller.hx;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.system.service.HxAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@ServerEndpoint("/hx/ai/tts/ws")
public class HxTtsRealtimeWebSocket {

    private static final Logger log = LoggerFactory.getLogger(HxTtsRealtimeWebSocket.class);

    private static final String ALIYUN_TTS_URL = "wss://dashscope.aliyuncs.com/api-ws/v1/realtime";

    private Session clientSession;
    private Session aliyunSession;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean sessionCreated = new AtomicBoolean(false);
    private static final ConcurrentHashMap<String, HxTtsRealtimeWebSocket> sessions = new ConcurrentHashMap<>();

    private String currentModel;
    private String currentVoice;
    private String userToken;

    private HxAiService getAiService() {
        return SpringUtils.getBean(HxAiService.class);
    }

    @OnOpen
    public void onOpen(Session session) {
        // 从查询参数中获取 token
        String queryString = session.getQueryString();
        this.userToken = extractTokenFromQuery(queryString);
        
        if (StringUtils.isEmpty(this.userToken)) {
            log.warn("TTS WebSocket connection without token: {}", session.getId());
            // 不立即关闭，允许后续通过消息认证
        } else {
            log.info("TTS WebSocket connection opened with token: {}", session.getId());
        }
        
        this.clientSession = session;
        sessions.put(session.getId(), this);
    }
    
    private String extractTokenFromQuery(String queryString) {
        if (StringUtils.isEmpty(queryString)) {
            return null;
        }
        
        try {
            String[] params = queryString.split("&");
            for (String param : params) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2 && "token".equals(kv[0])) {
                    return java.net.URLDecoder.decode(kv[1], "UTF-8");
                }
            }
        } catch (Exception e) {
            log.error("Error extracting token from query: {}", e.getMessage(), e);
        }
        
        return null;
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            JSONObject json = JSON.parseObject(message);
            String type = json.getString("type");

            log.debug("Received client message type: {}", type);

            if ("connect".equals(type)) {
                handleConnect(json);
            } else if ("text".equals(type)) {
                handleText(json);
            } else if ("finish".equals(type)) {
                handleFinish();
            }
        } catch (Exception e) {
            log.error("Error handling message: {}", e.getMessage(), e);
            sendError("处理消息失败: " + e.getMessage());
        }
    }

    private void handleConnect(JSONObject json) {
        try {
            HxAiService aiService = getAiService();
            String apiKey = aiService.getApiKey();

            if (StringUtils.isEmpty(apiKey)) {
                sendError("系统未配置 API Key");
                return;
            }

            currentModel = json.getString("model");
            currentVoice = json.getString("voice");

            if (StringUtils.isEmpty(currentModel)) {
                currentModel = aiService.getTtsRealtimeModel();
            }
            if (StringUtils.isEmpty(currentVoice)) {
                currentVoice = aiService.getTtsRealtimeVoice();
            }

            log.info("Connecting to Aliyun TTS with model: {}, voice: {}", currentModel, currentVoice);

            ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                    .configurator(new ClientEndpointConfig.Configurator() {
                        @Override
                        public void beforeRequest(java.util.Map<String, java.util.List<String>> headers) {
                            headers.put("Authorization", java.util.Collections.singletonList("Bearer " + apiKey));
                            headers.put("Content-Type", java.util.Collections.singletonList("application/json"));
                        }
                    })
                    .build();

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            aliyunSession = container.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    log.info("Aliyun TTS WebSocket connected");
                    session.addMessageHandler(new MessageHandler.Whole<String>() {
                        @Override
                        public void onMessage(String message) {
                            handleAliyunMessage(message);
                        }
                    });
                    isConnected.set(true);
                }
            }, config, URI.create(ALIYUN_TTS_URL));

            log.info("Aliyun TTS connection established");

        } catch (Exception e) {
            log.error("Failed to connect to Aliyun: {}", e.getMessage(), e);
            sendError("连接阿里云失败: " + e.getMessage());
        }
    }

    private void sendSessionUpdate() {
        if (aliyunSession == null || !aliyunSession.isOpen()) {
            log.error("Cannot send session.update: Aliyun session not open");
            return;
        }

        try {
            JSONObject event = new JSONObject();
            event.put("event_type", "session.update");

            JSONObject sessionConfig = new JSONObject();
            sessionConfig.put("model", currentModel);
            sessionConfig.put("voice", currentVoice);
            sessionConfig.put("mode", "server_commit");

            JSONObject inputTextBuffer = new JSONObject();
            inputTextBuffer.put("text", "");
            sessionConfig.put("input_text_buffer", inputTextBuffer);

            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "mp3");
            sessionConfig.put("response_format", responseFormat);

            event.put("session", sessionConfig);

            log.info("Sending session.update: {}", event.toString());
            aliyunSession.getBasicRemote().sendText(event.toString());

        } catch (IOException e) {
            log.error("Failed to send session.update: {}", e.getMessage(), e);
            sendError("配置会话失败: " + e.getMessage());
        }
    }

    private void handleText(JSONObject json) {
        if (aliyunSession == null || !aliyunSession.isOpen()) {
            sendError("未连接到阿里云服务");
            return;
        }

        if (!sessionCreated.get()) {
            sendError("会话尚未初始化，请稍后重试");
            return;
        }

        try {
            String text = json.getString("text");
            if (StringUtils.isEmpty(text)) {
                return;
            }

            JSONObject event = new JSONObject();
            event.put("event_type", "input_text_buffer.append");
            event.put("delta", text);

            log.debug("Sending input_text_buffer.append with text length: {}", text.length());
            aliyunSession.getBasicRemote().sendText(event.toString());

        } catch (Exception e) {
            log.error("Failed to send text: {}", e.getMessage(), e);
            sendError("发送文本失败: " + e.getMessage());
        }
    }

    private void handleFinish() {
        if (aliyunSession != null && aliyunSession.isOpen()) {
            try {
                JSONObject event = new JSONObject();
                event.put("event_type", "session.finish");
                log.info("Sending session.finish");
                aliyunSession.getBasicRemote().sendText(event.toString());
            } catch (IOException e) {
                log.error("Failed to send session.finish: {}", e.getMessage(), e);
            }
        }
    }

    private void handleAliyunMessage(String message) {
        try {
            JSONObject json = JSON.parseObject(message);
            String eventType = json.getString("event_type");

            log.debug("Received Aliyun event: {}", eventType);

            if ("session.created".equals(eventType)) {
                sessionCreated.set(true);
                log.info("Aliyun TTS session created");
                sendSessionUpdate();

            } else if ("session.updated".equals(eventType)) {
                log.info("Aliyun TTS session updated");
                sendToClient("connected", null);

            } else if ("response.audio.delta".equals(eventType)) {
                String delta = json.getString("delta");
                if (StringUtils.isNotEmpty(delta)) {
                    sendAudioToClient(delta);
                }

            } else if ("response.audio.done".equals(eventType)) {
                log.debug("Audio response done");

            } else if ("response.done".equals(eventType)) {
                log.info("Response completed");
                sendToClient("done", null);

            } else if ("session.finished".equals(eventType)) {
                log.info("Session finished");
                sendToClient("done", null);

            } else if ("error".equals(eventType)) {
                String errorMsg = json.getString("message");
                if (StringUtils.isEmpty(errorMsg) && json.containsKey("error")) {
                    JSONObject errorObj = json.getJSONObject("error");
                    if (errorObj != null) {
                        errorMsg = errorObj.getString("message");
                    }
                }
                log.error("Aliyun TTS error: {}", errorMsg);
                sendError("阿里云错误: " + errorMsg);

            } else {
                log.debug("Unhandled event type: {} - {}", eventType, message);
            }
        } catch (Exception e) {
            log.error("Error handling Aliyun message: {} - {}", e.getMessage(), message, e);
        }
    }

    private void sendToClient(String type, String data) {
        if (clientSession != null && clientSession.isOpen()) {
            try {
                JSONObject json = new JSONObject();
                json.put("type", type);
                if (data != null) {
                    json.put("data", data);
                }
                clientSession.getBasicRemote().sendText(json.toString());
            } catch (IOException e) {
                log.error("Failed to send to client: {}", e.getMessage(), e);
            }
        }
    }

    private void sendAudioToClient(String base64Audio) {
        if (clientSession != null && clientSession.isOpen()) {
            try {
                JSONObject json = new JSONObject();
                json.put("type", "audio");
                json.put("data", base64Audio);
                clientSession.getBasicRemote().sendText(json.toString());
            } catch (IOException e) {
                log.error("Failed to send audio to client: {}", e.getMessage(), e);
            }
        }
    }

    private void sendError(String message) {
        sendToClient("error", message);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session.getId());
        if (aliyunSession != null && aliyunSession.isOpen()) {
            try {
                aliyunSession.close();
            } catch (IOException e) {
                log.error("Error closing Aliyun session: {}", e.getMessage(), e);
            }
        }
        log.info("TTS WebSocket connection closed: {}", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("TTS WebSocket error: {}", error.getMessage(), error);
        sendError("WebSocket错误: " + error.getMessage());
    }
}
