package com.ruoyi.system.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.HxQa;
import com.ruoyi.system.mapper.HxQaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class HxAiService {

    private static final Logger log = LoggerFactory.getLogger(HxAiService.class);

    @Autowired
    private HxQaMapper hxQaMapper;

    @Autowired
    private ISysConfigService configService;

    // Config keys
    private static final String KEY_PROVIDER = "hx.ai.provider";
    private static final String KEY_API_URL = "hx.ai.api.url";
    private static final String KEY_API_KEY = "hx.ai.api.key";
    private static final String KEY_MODEL = "hx.ai.model";
    private static final String KEY_PROMPT_TEMPLATE = "hx.ai.prompt.template";
    private static final String KEY_TTS_MODEL = "hx.ai.tts.model";
    private static final String KEY_TTS_VOICE = "hx.ai.tts.voice";

    // Default Prompt Template
    private static final String DEFAULT_PROMPT_TEMPLATE = 
        "你是一个红芯成长的智能助手，致力于为用户提供准确、温暖的解答。\n" +
        "\n" +
        "### 你的任务\n" +
        "请根据下方的【参考资料】回答【用户问题】。请遵循以下原则：\n" +
        "1. **优先引用资料**：如果参考资料中有答案，请务必基于资料回答，确保准确性。\n" +
        "2. **诚实兜底**：如果参考资料无法回答问题，请先说明“资料库中暂无相关信息”，然后基于你的通用知识提供有帮助的建议。\n" +
        "3. **风格要求**：回答应条理清晰，分点表述，语气亲切自然。\n" +
        "\n" +
        "### 参考资料\n" +
        "{reference}\n" +
        "\n" +
        "### 用户问题\n" +
        "{question}";

    public String chat(String question) {
        String prompt = buildPrompt(question);
        return callAiApi(prompt);
    }

    public void streamChat(String question, Consumer<String> onDelta) {
        String prompt = buildPrompt(question);
        callAiStreamApi(prompt, onDelta);
    }

    public Map<String, Object> tts(String text, String voice, String model, String languageType) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isEmpty(text)) {
            result.put("error", "文本不能为空");
            return result;
        }

        String apiKey = configService.selectConfigByKey(KEY_API_KEY);
        if (StringUtils.isEmpty(apiKey)) {
            result.put("error", "系统未配置 AI 参数，请联系管理员。");
            return result;
        }

        String ttsModel = StringUtils.isEmpty(model) ? configService.selectConfigByKey(KEY_TTS_MODEL) : model;
        if (StringUtils.isEmpty(ttsModel)) {
             ttsModel = "qwen3-tts-flash";
        }
        
        String ttsVoice = StringUtils.isEmpty(voice) ? configService.selectConfigByKey(KEY_TTS_VOICE) : voice;
        if (StringUtils.isEmpty(ttsVoice)) {
             ttsVoice = "Cherry";
        }
        
        String ttsLang = StringUtils.isEmpty(languageType) ? "Chinese" : languageType;
        String apiUrl = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", ttsModel);
            JSONObject input = new JSONObject();
            input.put("text", text);
            input.put("voice", ttsVoice);
            input.put("language_type", ttsLang);
            requestBody.put("input", input);
            
            JSONObject parameters = new JSONObject();
            parameters.put("format", "mp3");
            requestBody.put("parameters", parameters);

            String response = sendAiRequest(apiUrl, requestBody.toString(), apiKey);
            JSONObject jsonResponse = JSON.parseObject(response);
            JSONObject output = jsonResponse.getJSONObject("output");
            if (output != null) {
                JSONObject audio = output.getJSONObject("audio");
                if (audio != null) {
                    String data = audio.getString("data");
                    String url = audio.getString("url");
                    String format = audio.getString("format");
                    if (StringUtils.isNotEmpty(data)) {
                        result.put("audioBase64", data);
                        if (StringUtils.isNotEmpty(format)) {
                            result.put("format", format);
                        }
                        return result;
                    }
                    if (StringUtils.isNotEmpty(url)) {
                        result.put("audioUrl", url);
                        if (StringUtils.isNotEmpty(format)) {
                            result.put("format", format);
                        }
                        return result;
                    }
                }
            }
            if (jsonResponse.containsKey("error")) {
                result.put("error", jsonResponse.getJSONObject("error").getString("message"));
                return result;
            }
            result.put("error", "TTS 响应为空");
            return result;
        } catch (Exception e) {
            log.error("Call TTS API failed", e);
            result.put("error", "TTS 调用失败：" + e.getMessage());
            return result;
        }
    }

    private String buildPrompt(String question) {
        // 1. Retrieve Knowledge
        HxQa query = new HxQa();
        query.setStatus("1"); // Only published
        List<HxQa> allQaList = hxQaMapper.selectHxQaList(query);
        List<HxQa> finalQaList;

        // Dynamic Strategy:
        // If the knowledge base is small (<= 10 items), send ALL content to AI to ensure 100% recall.
        // If the knowledge base is large, use the retrieval algorithm to find Top 10.
        if (allQaList.size() <= 10) {
            finalQaList = allQaList;
            log.info("Knowledge base small ({}), using FULL CONTEXT strategy.", allQaList.size());
        } else {
            // Calculate relevance score for each QA
            List<Map.Entry<HxQa, Integer>> scoredList = new ArrayList<>();
            for (HxQa qa : allQaList) {
                int score = calculateRelevanceScore(qa, question);
                if (score > 0) {
                    scoredList.add(new java.util.AbstractMap.SimpleEntry<>(qa, score));
                }
            }

            // Sort by score desc
            scoredList.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

            // Take top 10 most relevant
            int maxCount = 10;
            finalQaList = scoredList.stream()
                    .limit(maxCount)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            log.info("Knowledge base large ({}), using RETRIEVAL strategy. Found {} matches.", allQaList.size(), finalQaList.size());
            
            // Fallback: If no relevant QA found in large DB, add top 3 latest as backup context
            if (finalQaList.isEmpty() && !allQaList.isEmpty()) {
                 finalQaList = allQaList.stream().limit(3).collect(Collectors.toList());
            }
        }

        // 2. Construct Prompt
        String template = configService.selectConfigByKey(KEY_PROMPT_TEMPLATE);
        if (StringUtils.isEmpty(template)) {
            template = DEFAULT_PROMPT_TEMPLATE;
        }

        StringBuilder referenceBuilder = new StringBuilder();
        if (!finalQaList.isEmpty()) {
            referenceBuilder.append("【参考资料库 (Top ").append(finalQaList.size()).append(")】：\n");
            for (int i = 0; i < finalQaList.size(); i++) {
                HxQa qa = finalQaList.get(i);
                // Simplify content
                String cleanContent = qa.getContent() != null ? qa.getContent().replaceAll("\\s+", " ") : "";
                referenceBuilder.append(i + 1).append(". [提问]：").append(qa.getTitle())
                      .append("\n   [回答]：").append(cleanContent).append("\n");
            }
        }

        // Replace placeholders
        String prompt = template.replace("{reference}", referenceBuilder.toString())
                                .replace("{question}", question);

        return prompt;
    }

    /**
     * Relevance scoring using Bigram overlap (optimized for Chinese)
     */
    private int calculateRelevanceScore(HxQa qa, String question) {
        if (qa == null || question == null) return 0;
        
        // 1. Basic Cleaning
        String q = question.toLowerCase().trim();
        String t = qa.getTitle().toLowerCase().trim();
        String c = qa.getContent() != null ? qa.getContent().toLowerCase().trim() : "";
        String r = qa.getRemark() != null ? qa.getRemark().toLowerCase().trim() : "";
        
        if (q.isEmpty() || t.isEmpty()) return 0;

        // 2. Exact Match High Priority
        if (t.equals(q)) return 100;
        if (t.contains(q)) return 90; 
        if (q.contains(t)) return 90;

        // 3. Bigram Generation
        // Remove all punctuation and spaces for continuous stream
        String cleanQ = q.replaceAll("[\\p{P}\\p{S}\\s]", "");
        String cleanT = t.replaceAll("[\\p{P}\\p{S}\\s]", "");
        String cleanC = c.replaceAll("[\\p{P}\\p{S}\\s]", "");
        String cleanR = r.replaceAll("[\\p{P}\\p{S}\\s]", "");
        
        Set<String> qBigrams = getBigrams(cleanQ);
        Set<String> tBigrams = getBigrams(cleanT);
        Set<String> cBigrams = getBigrams(cleanC);
        Set<String> rBigrams = getBigrams(cleanR);
        
        int score = 0;
        
        // 4. Score Calculation
        // Title Overlap (High Weight)
        for (String bi : qBigrams) {
            if (tBigrams.contains(bi)) score += 10;
        }
        
        // Content & Remark Overlap (Low Weight, but helps recall)
        for (String bi : qBigrams) {
            if (cBigrams.contains(bi)) score += 2;
            if (rBigrams.contains(bi)) score += 2; // Match keywords in remark
        }
        
        return score;
    }
    
    private Set<String> getBigrams(String text) {
        Set<String> bigrams = new HashSet<>();
        if (text == null || text.length() < 2) return bigrams;
        for (int i = 0; i < text.length() - 1; i++) {
            bigrams.add(text.substring(i, i + 2));
        }
        return bigrams;
    }

    private String callAiApi(String content) {
        String provider = configService.selectConfigByKey(KEY_PROVIDER);
        String apiUrl = configService.selectConfigByKey(KEY_API_URL);
        String apiKey = configService.selectConfigByKey(KEY_API_KEY);
        String model = configService.selectConfigByKey(KEY_MODEL);

        if (StringUtils.isEmpty(apiKey)) {
            return "系统未配置 AI 参数，请联系管理员。";
        }
        
        // Default URL for Zhipu if not set
        if (StringUtils.isEmpty(apiUrl)) {
            if ("zhipu".equals(provider)) {
                apiUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
            } else if ("deepseek".equals(provider)) {
                 apiUrl = "https://api.deepseek.com/chat/completions";
            } else if ("aliyun".equals(provider)) {
                 apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
            } else {
                 // Fallback to Zhipu default
                 apiUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
            }
        }
        
        // Default Model if not set
        if (StringUtils.isEmpty(model)) {
            model = "glm-4.6v-flash";
        }

        try {
            // Construct OpenAI format request body
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", content);
            messages.add(message);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);

            // Send Request using custom method with timeout
            String response = sendAiRequest(apiUrl, requestBody.toString(), apiKey);
            
            // Parse Response
            JSONObject jsonResponse = JSON.parseObject(response);
            if (jsonResponse.containsKey("choices")) {
                return jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            } else if (jsonResponse.containsKey("error")) {
                log.error("AI API Error: {}", response);
                return "AI 响应错误: " + jsonResponse.getJSONObject("error").getString("message");
            }
            return "AI 暂时无法回答，请稍后再试。";
        } catch (Exception e) {
            log.error("Call AI API failed", e);
            return "系统繁忙，请稍后再试（" + e.getMessage() + "）。";
        }
    }

    private void callAiStreamApi(String content, Consumer<String> onDelta) {
        String provider = configService.selectConfigByKey(KEY_PROVIDER);
        String apiUrl = configService.selectConfigByKey(KEY_API_URL);
        String apiKey = configService.selectConfigByKey(KEY_API_KEY);
        String model = configService.selectConfigByKey(KEY_MODEL);

        if (StringUtils.isEmpty(apiKey)) {
            onDelta.accept("系统未配置 AI 参数，请联系管理员。");
            return;
        }

        if (StringUtils.isEmpty(apiUrl)) {
            if ("zhipu".equals(provider)) {
                apiUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
            } else if ("deepseek".equals(provider)) {
                apiUrl = "https://api.deepseek.com/chat/completions";
            } else if ("aliyun".equals(provider)) {
                apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
            } else {
                apiUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
            }
        }

        if (StringUtils.isEmpty(model)) {
            model = "glm-4.6v-flash";
        }

        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", content);
            messages.add(message);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("stream", true);

            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(120000);
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setRequestProperty("User-Agent", "HxAiService/1.0");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (trimmed.startsWith("data:")) {
                    String data = trimmed.substring(5).trim();
                    if ("[DONE]".equals(data)) {
                        break;
                    }
                    JSONObject json = JSON.parseObject(data);
                    JSONArray choices = json.getJSONArray("choices");
                    if (choices != null && !choices.isEmpty()) {
                        JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                        if (delta != null) {
                            String contentDelta = delta.getString("content");
                            if (StringUtils.isNotEmpty(contentDelta)) {
                                onDelta.accept(contentDelta);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Call AI stream API failed", e);
            onDelta.accept("系统繁忙，请稍后再试。");
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception e) {}
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Custom HTTP POST with extended timeout for AI requests
     */
    private String sendAiRequest(String apiUrl, String jsonBody, String apiKey) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            
            // Timeouts: Connect 10s, Read 120s (2 minutes)
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(120000);
            
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "HxAiService/1.0");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                // Try to read error stream
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream(), StandardCharsets.UTF_8));
            }

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.error("AI API returned code: {}, body: {}", responseCode, result);
            }
            
            return result.toString();
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception e) {}
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    
    /**
     * Get current AI configuration
     */
    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("provider", configService.selectConfigByKey(KEY_PROVIDER));
        config.put("apiKey", configService.selectConfigByKey(KEY_API_KEY));
        config.put("model", configService.selectConfigByKey(KEY_MODEL));
        config.put("apiUrl", configService.selectConfigByKey(KEY_API_URL));
        config.put("promptTemplate", configService.selectConfigByKey(KEY_PROMPT_TEMPLATE));
        config.put("ttsModel", configService.selectConfigByKey(KEY_TTS_MODEL));
        config.put("ttsVoice", configService.selectConfigByKey(KEY_TTS_VOICE));
        return config;
    }

    /**
     * Get current AI model name (Safe for public)
     */
    public String getModelName() {
        return configService.selectConfigByKey(KEY_MODEL);
    }

    /**
     * Update AI configuration
     */
    public void updateConfig(Map<String, String> config) {
        updateConfigValue(KEY_PROVIDER, config.get("provider"));
        updateConfigValue(KEY_API_KEY, config.get("apiKey"));
        updateConfigValue(KEY_MODEL, config.get("model"));
        updateConfigValue(KEY_API_URL, config.get("apiUrl"));
        updateConfigValue(KEY_PROMPT_TEMPLATE, config.get("promptTemplate"));
        updateConfigValue(KEY_TTS_MODEL, config.get("ttsModel"));
        updateConfigValue(KEY_TTS_VOICE, config.get("ttsVoice"));
    }

    private void updateConfigValue(String key, String value) {
        if (value == null) return; // Ignore null updates
        
        com.ruoyi.system.domain.SysConfig config = new com.ruoyi.system.domain.SysConfig();
        config.setConfigKey(key);
        List<com.ruoyi.system.domain.SysConfig> list = configService.selectConfigList(config);
        
        if (list.size() > 0) {
            com.ruoyi.system.domain.SysConfig exists = list.get(0);
            exists.setConfigValue(value);
            configService.updateConfig(exists);
        } else {
            com.ruoyi.system.domain.SysConfig newConfig = new com.ruoyi.system.domain.SysConfig();
            newConfig.setConfigName("AI Config " + key);
            newConfig.setConfigKey(key);
            newConfig.setConfigValue(value);
            newConfig.setConfigType("Y"); 
            configService.insertConfig(newConfig);
        }
    }
}
