package com.muiyurocodes.ecommerc.controller;

import com.muiyurocodes.ecommerc.dto.OrderDTO;
import com.muiyurocodes.ecommerc.service.OrderService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${server.url:http://localhost:8080}")
    private String serverUrl;

    private final OrderService orderService;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    /**
     * Create a Stripe checkout session for an order
     * @param userId The ID of the user
     * @param orderId The ID of the order to checkout
     * @return A map containing the Stripe session ID
     */
    @PostMapping("/checkout/{userId}/{orderId}")
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @PathVariable Long userId,
            @PathVariable Long orderId) {
        
        Optional<OrderDTO> orderOpt = orderService.getOrderDetails(userId, orderId);
        
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        OrderDTO order = orderOpt.get();
        
        try {
            // Create Stripe checkout session
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(serverUrl + "/api/payments/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(serverUrl + "/api/payments/cancel?session_id={CHECKOUT_SESSION_ID}")
                .setClientReferenceId(orderId.toString());
            
            // Add line items from order
            for (var item : order.getOrderItems()) {
                paramsBuilder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(item.getPrice().multiply(java.math.BigDecimal.valueOf(100)).longValue()) // Convert to cents
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(item.getProductName())
                                        .build()
                                )
                                .build()
                        )
                        .setQuantity(Long.valueOf(item.getQuantity()))
                        .build()
                );
            }
            
            Session session = Session.create(paramsBuilder.build());
            
            Map<String, String> responseData = new HashMap<>();
            responseData.put("sessionId", session.getId());
            
            return ResponseEntity.ok(responseData);
            
        } catch (StripeException e) {
            Map<String, String> errorData = new HashMap<>();
            errorData.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorData);
        }
    }
    
    /**
     * Handle successful payment
     * @param sessionId The Stripe session ID
     * @return A success message
     */
    @GetMapping("/success")
    public ResponseEntity<Map<String, String>> paymentSuccess(@RequestParam("session_id") String sessionId) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Payment completed successfully");
        response.put("sessionId", sessionId);
        
        // In a real implementation, you would update the order status here
        // This would typically be handled by the webhook for reliability
        // But this endpoint provides a user-friendly redirect
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Handle cancelled payment
     * @param sessionId The Stripe session ID
     * @return A cancellation message
     */
    @GetMapping("/cancel")
    public ResponseEntity<Map<String, String>> paymentCancel(@RequestParam("session_id") String sessionId) {
        Map<String, String> response = new HashMap<>();
        response.put("status", "cancelled");
        response.put("message", "Payment was cancelled");
        response.put("sessionId", sessionId);
        
        return ResponseEntity.ok(response);
    }
}