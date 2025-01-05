package ie.atu.pospaymentservice.service;

import ie.atu.pospaymentservice.client.InventoryClient;
import ie.atu.pospaymentservice.model.BuyerBalance;
import ie.atu.pospaymentservice.repository.BuyerBalanceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BuyerBalanceRepo buyerBalanceRepo;
    private final InventoryClient inventoryClient;

    public BuyerBalance allocateFunds(String buyerUsername, double amount) {
        BuyerBalance balance = buyerBalanceRepo.findByBuyerUsername(buyerUsername)
                .orElse(BuyerBalance.builder()
                        .buyerUsername(buyerUsername)
                        .balance(0.0)
                        .build());
        balance.setBalance(balance.getBalance() + amount);
        return buyerBalanceRepo.save(balance);
    }

    public double getBalance(String buyerUsername) {
        BuyerBalance balance = buyerBalanceRepo.findByBuyerUsername(buyerUsername)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));
        return balance.getBalance();
    }

    public String purchase(String buyerUsername, Long productId, double totalCost, int quantity) {
        BuyerBalance balance = buyerBalanceRepo.findByBuyerUsername(buyerUsername)
                .orElseThrow(() -> new RuntimeException("Buyer not found"));

        if (balance.getBalance() < totalCost) {
            return "Insufficient balance";
        }
        balance.setBalance(balance.getBalance() - totalCost);
        buyerBalanceRepo.save(balance);

        InventoryClient.DecrementRequest decReq = new InventoryClient.DecrementRequest();
        decReq.setAmount(quantity);
        String inventoryRes = inventoryClient.decrementStock(productId, decReq);

        return "Purchase successful! " + inventoryRes;

    }
}
