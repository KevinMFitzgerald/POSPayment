package ie.atu.pospaymentservice.service;

import ie.atu.pospaymentservice.client.InventoryClient;
import ie.atu.pospaymentservice.client.PaymentClient;
import ie.atu.pospaymentservice.model.BuyerBalance;
import ie.atu.pospaymentservice.repository.BuyerBalanceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BuyerBalanceRepo balanceRepo;
    private final InventoryClient inventoryClient;

    public BuyerBalance allocateFunds(String buyerUsername, double amount) {
        BuyerBalance balance = balanceRepo.findByBuyerUsername(buyerUsername)
                .orElseGet(() -> new BuyerBalance(null, buyerUsername, 0.0));
        balance.setBalance(balance.getBalance() + amount);
        return balanceRepo.save(balance);
    }

    public double getBalance(String buyerUsername) {
        return balanceRepo.findByBuyerUsername(buyerUsername)
                .orElseThrow(() -> new RuntimeException("No balance found for user: " + buyerUsername))
                .getBalance();
    }

    @Transactional
    public String purchase(String buyerUsername, List<PaymentClient.PurchaseRequest.ItemDto> items, double totalCost) {
        BuyerBalance balance = balanceRepo.findByBuyerUsername(buyerUsername)
                .orElseThrow(() -> new RuntimeException("No balance found for user: " + buyerUsername));

        if (balance.getBalance() < totalCost) {
            return "Insufficient balance!";
        }

        // Step 1: Decrement stock for all items
        for (PaymentClient.PurchaseRequest.ItemDto item : items) {
            InventoryClient.DecrementRequest reserveRequest = new InventoryClient.DecrementRequest();
            reserveRequest.setAmount(item.getQuantity());
            String reserveResponse = inventoryClient.decrementStock(item.getProductId(), reserveRequest);

            if (!reserveResponse.contains("Stock decremented")) {
                throw new RuntimeException("Stock reservation failed for product ID: " + item.getProductId());
            }
        }

        // Step 2: Deduct total cost from balance
        balance.setBalance(balance.getBalance() - totalCost);
        balanceRepo.save(balance);

        return "Purchase successful!";
    }
}
