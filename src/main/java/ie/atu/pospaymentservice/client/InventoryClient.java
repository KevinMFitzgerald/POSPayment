package ie.atu.pospaymentservice.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service", url = "http://localhost:8080")
public interface InventoryClient {

    @PutMapping("/api/inventory/products/{id}/decrement")
    String decrementStock(@PathVariable("id") Long productId, @RequestBody DecrementRequest request);
    @GetMapping("/api/inventory/products/{id}")
    ProductDto getProductById(@PathVariable Long id);

    @Data
    class ProductDto {
        private Long id;
        private String name;
        private String category;
        private double price;
        private int quantity;
    }
    @Data
    class DecrementRequest {
        private int amount;
    }
}
