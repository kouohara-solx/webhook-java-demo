package org.example.webhookdemo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class WebhookControllerTest {

    @InjectMocks
    private WebhookController webhookController;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webhookController.setRestTemplate(restTemplate); // Ensure the mocked RestTemplate is used
    }

    @Test
    void generatePayloadReturnsOkWithValidData() throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("template", "Hello {{name}}");
        Map<String, String> values = new HashMap<>();
        values.put("name", "World");
        data.put("values", values);

        ResponseEntity<String> response = webhookController.generatePayload(data);

        assertEquals(ResponseEntity.ok("Hello World"), response);
    }

    @Test
    void generatePayloadReturnsBadRequestOnIOException() throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("template", "{{#broken}}");

        ResponseEntity<String> response = webhookController.generatePayload(data);
        if (response.getBody() != null) {
            assertTrue(response.getBody().contains("Error generating payload:"));
        } else {
            fail();
        }
    }

    @Test
    void sendMessageReturnsOkWithValidData() throws UnsupportedEncodingException {
        Map<String, String> data = new HashMap<>();
        data.put("webhookUrl", "http://example.com/webhook");
        data.put("payload", "Test payload");

        when(restTemplate.postForObject(anyString(), anyString(), eq(String.class))).thenReturn(null);

        ResponseEntity<String> response = webhookController.sendMessage(data);

        assertEquals(ResponseEntity.ok("Message sent successfully"), response);
    }

    @Test
    void sendMessageReturnsBadRequestOnException() throws UnsupportedEncodingException {
        Map<String, String> data = new HashMap<>();
        data.put("webhookUrl", "http://example.com/webhook");
        data.put("payload", "Test payload");

        doThrow(new RuntimeException("Connection error")).when(restTemplate).postForObject(anyString(), anyString(), eq(String.class));

        ResponseEntity<String> response = webhookController.sendMessage(data);

        assertEquals(ResponseEntity.badRequest().body("Error sending message: Connection error"), response);
    }
}