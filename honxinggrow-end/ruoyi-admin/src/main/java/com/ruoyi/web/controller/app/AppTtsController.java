package com.ruoyi.web.controller.app;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.HxAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * TTS 配置接口
 */
@RestController
@RequestMapping("/app/tts")
public class AppTtsController extends BaseController {

    @Autowired
    private HxAiService aiService;

    /**
     * 获取 TTS 配置
     */
    @GetMapping("/config")
    public AjaxResult getConfig() {
        String apiKey = aiService.getApiKey();
        
        if (apiKey == null || apiKey.isEmpty()) {
            return AjaxResult.error("TTS 服务未配置");
        }

        Map<String, String> config = new HashMap<>();
        config.put("apiKey", apiKey);
        config.put("model", "qwen3-tts-flash-realtime");
        config.put("voice", "Cherry");

        return AjaxResult.success(config);
    }
}
