package com.example.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.client.RestTemplate;
//import org.springframework.web.servlet.view.RedirectView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class PaymentController {

    @Value("${paytech.api.token}")
    private String bearerToken;
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    private static final String PAYTECH_API_URL = "https://engine-sandbox.pay.tech/api/v1/payments";

    @GetMapping("/")
    public String index() {
        return "home";

    }

    @PostMapping("/")
    public String makePayment(@RequestParam("amount") String amount, Model model) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + bearerToken);
        headers.set("accept", "application/json");
        headers.set("content-type", "application/json");

        String requestBody = "{\"paymentType\":\"DEPOSIT\",\"currency\":\"EUR\",\"amount\":" + amount
                + ",\"customer\":{\"citizenshipCountryCode\":\"AU\",\"referenceId\":\"2\"}}";
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {

            ResponseEntity<String> apiResponse = restTemplate.exchange(
                    PAYTECH_API_URL,
                    HttpMethod.POST,
                    request,
                    String.class);

            if (apiResponse.getStatusCode().is2xxSuccessful()) {

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonResponse = objectMapper.readTree(apiResponse.getBody());
                String redirectUrl = jsonResponse.get("result").get("redirectUrl").asText();

                model.addAttribute("redirectUrl", redirectUrl);
                return "redirectPage";

            } else {
                model.addAttribute("error", "Payment failed: Invalid response from API");
                return "error";
            }

        } catch (Exception e) {
            log.info("Exception");
            model.addAttribute("error", "Payment failed: " + e.getMessage());
            return "error";
        }
    }
}
