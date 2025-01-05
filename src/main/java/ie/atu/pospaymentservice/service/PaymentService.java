package ie.atu.pospaymentservice.service;

import ie.atu.pospaymentservice.client.InventoryClient;
import ie.atu.pospaymentservice.model.BuyerBalance;
import ie.atu.pospaymentservice.repository.BuyerBalanceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BuyerBalanceRepo buyerBalanceRepo;
    private final InventoryClient inventoryClient;

    public BuyerBalance allocateFunds(String buyerUsername, double amount) {
        BuyerBalance balance = buyerBalanceRepo.findByBuyerUsername(buyerUsername)
                .orElse(new BuyerBalance(null, buyerUsername, amount)); // Using null for ID
        balance.setBalance(balance.getBalance() + amount);
        return buyerBalanceRepo.save(balance);
    }

    public BuyerBalance assignRandomFunds(String buyerUsername) {
        double randomAmount = new Random().nextInt(9001) + 1000;
        return allocateFunds(buyerUsername, randomAmount);
    }

    public double getBalance(String buyerUsername) {
        return buyerBalanceRepo.findByBuyerUsername(buyerUsername)
                .map(BuyerBalance::getBalance)
                .orElse(0.0);
    }

    public String purchase(String buyerUsername, Long productId, double totalCost, int quantity) {
        BuyerBalance balance = buyerBalanceRepo.findByBuyerUsername(buyerUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (balance.getBalance() < totalCost) {
            return "Insufficient balance!";
        }

        balance.setBalance(balance.getBalance() - totalCost);
        buyerBalanceRepo.save(balance);

        InventoryClient.DecrementRequest decrementRequest = new InventoryClient.DecrementRequest();
        decrementRequest.setAmount(quantity);
        inventoryClient.decrementStock(productId, decrementRequest);


        return "Purchase successful!";
    }
}
