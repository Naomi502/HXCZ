package com.ruoyi.web.controller.hx;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.HxAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson2.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/hx/ai")
public class HxAiController extends BaseController {

    @Autowired
    private HxAiService aiService;

    /**
     * AI Chat Interface
     */
    @PreAuthorize("@ss.hasAnyRoles('student,teacher,admin')")
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        if (question == null || question.trim().isEmpty()) {
            return AjaxResult.error("问题不能为空");
        }
        String answer = aiService.chat(question);
        return AjaxResult.success("操作成功", answer);
    }

    @PreAuthorize("@ss.hasAnyRoles('student,teacher,admin')")
    @PostMapping(value = "/chat/stream", produces = "text/event-stream")
    public void chatStream(@RequestBody Map<String, String> body, HttpServletResponse response) throws IOException {
        String question = body.get("question");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/event-stream");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("X-Accel-Buffering", "no");

        if (question == null || question.trim().isEmpty()) {
            response.getWriter().write("data: " + JSON.toJSONString(Collections.singletonMap("error", "问题不能为空")) + "\n\n");
            response.getWriter().flush();
            return;
        }

        try {
            aiService.streamChat(question, delta -> {
                try {
                    response.getWriter().write("data: " + JSON.toJSONString(Collections.singletonMap("delta", delta)) + "\n\n");
                    response.getWriter().flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            response.getWriter().write("data: [DONE]\n\n");
            response.getWriter().flush();
        } catch (RuntimeException e) {
            response.getWriter().write("data: " + JSON.toJSONString(Collections.singletonMap("error", "流式输出中断")) + "\n\n");
            response.getWriter().flush();
        }
    }

    @PreAuthorize("@ss.hasAnyRoles('student,teacher,admin')")
    @PostMapping("/tts")
    public AjaxResult tts(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        String voice = body.get("voice");
        String model = body.get("model");
        String languageType = body.get("languageType");
        Map<String, Object> result = aiService.tts(text, voice, model, languageType);
        if (result.containsKey("error")) {
            return AjaxResult.error(String.valueOf(result.get("error")));
        }
        return AjaxResult.success("操作成功", result);
    }
    
    /**
     * Get AI Configuration
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/config")
    public AjaxResult getConfig() {
        return AjaxResult.success(aiService.getConfig());
    }
    
    /**
     * Get AI Model Name (Public)
     */
    @PreAuthorize("@ss.hasAnyRoles('student,teacher,admin')")
    @GetMapping("/model")
    public AjaxResult getModelName() {
        return AjaxResult.success("操作成功", aiService.getModelName());
    }
    
    /**
     * Update AI Configuration
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @PostMapping("/config")
    public AjaxResult updateConfig(@RequestBody Map<String, String> config) {
        aiService.updateConfig(config);
        return AjaxResult.success();
    }
}
