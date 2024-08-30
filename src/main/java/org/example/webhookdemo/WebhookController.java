package org.example.webhookdemo;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.HandlebarsException;
import com.github.jknack.handlebars.Template;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Webhookコントローラー
 */
@Controller
public class WebhookController {

    private RestTemplate restTemplate = new RestTemplate();

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Payloadを作成する
     * <p>
     * Handlebarsを使用して、テンプレートと値を使用してペイロードを生成します。
     * </p>
     * @param data テンプレートと値のマップ
     * @return 生成されたペイロード
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generatePayload(@RequestBody Map<String, Object> data) {
        try {
            Handlebars handlebars = new Handlebars();
            Template template = handlebars.compileInline((String) data.get("template"));
            String result = template.apply(data.get("values"));
            return ResponseEntity.ok(result);
        } catch (IOException | HandlebarsException e) {
            return ResponseEntity.badRequest().body("Error generating payload: " + e.getMessage());
        }
    }

    /**
     * メッセージを送信する
     * @param data メッセージデータ
     * @return メッセージ送信のステータス
     * @throws UnsupportedEncodingException エンコードエラー
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody Map<String, String> data) throws UnsupportedEncodingException {
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        String webhookUrl = data.get("webhookUrl");
        String payload = data.get("payload");

        try {
            restTemplate.postForObject(webhookUrl, payload, String.class);
            return ResponseEntity.ok("Message sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error sending message: " + e.getMessage());
        }
    }
}