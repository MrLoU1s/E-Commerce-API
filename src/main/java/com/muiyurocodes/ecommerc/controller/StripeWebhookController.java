package com.muiyurocodes.ecommerc.controller;

import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.stripe.exception.SignatureVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/stripe")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping
    public void handleStripeEvent(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        if (sigHeader == null) {
            // Or handle as an error
            return;
        }

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            // Invalid signature
            System.out.println("Webhook error:: Invalid signature.");
            // Or handle as an error
            return;
        }

        // Handle the event
        switch (event.getType()) {
            case "checkout.session.completed":
                // Then define and call a method to handle the successful payment intent.
                // handleCheckoutSessionCompleted(event);
                System.out.println("Checkout session completed!");
                break;
            case "payment_intent.succeeded":
                // Then define and call a method to handle the successful payment intent.
                // handlePaymentIntentSucceeded(event);
                System.out.println("Payment intent succeeded!");
                break;
            case "payment_intent.payment_failed":
                // Then define and call a method to handle the failed payment intent.
                // handlePaymentIntentFailed(event);
                System.out.println("Payment intent failed!");
                break;
            // ... handle other event types
            default:
                System.out.println("Unhandled event type: " + event.getType());
        }
    }
}
