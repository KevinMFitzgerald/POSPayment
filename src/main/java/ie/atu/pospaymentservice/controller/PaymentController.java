package ie.atu.pospaymentservice.controller;

import ie.atu.pospaymentservice.model.BuyerBalance;
import ie.atu.pospaymentservice.service.PaymentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/allocate")
    public ResponseEntity<?> allocate(@RequestBody AllocateRequest req) {
        BuyerBalance bal = paymentService.allocateFunds(req.getBuyerUsername(), req.getAmount());
        return ResponseEntity.ok(bal);
    }

    @GetMapping("/balance/{username}")
    public ResponseEntity<?> getBalance(@PathVariable String username) {
        try {
            double bal = paymentService.getBalance(username);
            return ResponseEntity.ok(bal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> purchase(@RequestBody PurchaseRequest req) {
        String result = paymentService.purchase(
                req.getBuyerUsername(),
                req.getProductId(),
                req.getTotalCost(),
                req.getQuantity()
        );
        if (result.startsWith("Purchase successful")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @Data
    static class AllocateRequest {
        private String buyerUsername;
        private double amount;
    }

    @Data
    static class PurchaseRequest {
        private String buyerUsername;
        private Long productId;
        private double totalCost;
        private int quantity;
    }
}