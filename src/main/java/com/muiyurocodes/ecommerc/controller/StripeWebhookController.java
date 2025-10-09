package com.muiyurocodes.ecommerc.controller;

import com.muiyurocodes.ecommerc.model.Order;
import com.muiyurocodes.ecommerc.repository.OrderRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/webhooks/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;
    
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<String> handleStripeEvent(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        if (sigHeader == null) {
            log.error("Stripe-Signature header is missing");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Stripe signature is required");
        }

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook error: Invalid signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        // Handle the event
        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            case "payment_intent.succeeded":
                handlePaymentIntentSucceeded(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentIntentFailed(event);
                break;
            default:
                log.info("Unhandled event type: {}", event.getType());
        }
        
        return ResponseEntity.ok("Webhook processed successfully");
    }
    
    private void handleCheckoutSessionCompleted(Event event) {
        try {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
            String orderId = session.getClientReferenceId();
            
            if (orderId != null && !orderId.isEmpty()) {
                updateOrderStatus(Long.parseLong(orderId), "PAID");
                log.info("Order {} marked as PAID after successful checkout", orderId);
            } else {
                log.warn("Checkout session completed but no order ID was found in client_reference_id");
            }
        } catch (Exception e) {
            log.error("Error processing checkout.session.completed: {}", e.getMessage(), e);
        }
    }
    
    private void handlePaymentIntentSucceeded(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElseThrow();
            String orderId = paymentIntent.getMetadata().get("orderId");
            
            if (orderId != null && !orderId.isEmpty()) {
                updateOrderStatus(Long.parseLong(orderId), "PAID");
                log.info("Order {} marked as PAID after successful payment", orderId);
            } else {
                log.warn("Payment succeeded but no order ID was found in metadata");
            }
        } catch (Exception e) {
            log.error("Error processing payment_intent.succeeded: {}", e.getMessage(), e);
        }
    }
    
    private void handlePaymentIntentFailed(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElseThrow();
            String orderId = paymentIntent.getMetadata().get("orderId");
            
            if (orderId != null && !orderId.isEmpty()) {
                updateOrderStatus(Long.parseLong(orderId), "PAYMENT_FAILED");
                log.info("Order {} marked as PAYMENT_FAILED", orderId);
            } else {
                log.warn("Payment failed but no order ID was found in metadata");
            }
        } catch (Exception e) {
            log.error("Error processing payment_intent.payment_failed: {}", e.getMessage(), e);
        }
    }
    
    private void updateOrderStatus(Long orderId, String status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            orderRepository.save(order);
        } else {
            log.error("Could not find order with ID: {}", orderId);
        }
    }
}