package ie.atu.pospaymentservice.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "payment-service", url = "http://localhost:8081")
public interface PaymentClient {

    @PostMapping("/api/payment/purchase")
    String purchase(@RequestBody PurchaseRequest req);

    @PostMapping("/api/payment/allocate")
    String allocate(@RequestBody AllocateRequest request);

    @Data
    class AllocateRequest {
        private String buyerUsername;
        private double amount;
    }
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
     class PurchaseRequest {
        private String buyerUsername;
        private List<ItemDto> items;
        private double totalCost;

        @AllArgsConstructor
        @NoArgsConstructor
        @Data
        public static class ItemDto {
            private Long productId;
            private int quantity;
        }
    }
}
