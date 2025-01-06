package ie.atu.pospaymentservice.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "payment-service", url = "http://localhost:8081")
public interface PaymentClient {

    @PostMapping("/api/payment/purchase")
    String purchase(@RequestBody PurchaseRequest req);

    @Data
    public static class PurchaseRequest {
        private String buyerUsername;
        private List<ItemDto> items;
        private double totalCost;

        @Data
        public static class ItemDto {
            private Long productId;
            private int quantity;
        }
    }
}
