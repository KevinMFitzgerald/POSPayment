package ie.atu.pospaymentservice.repository;

import ie.atu.pospaymentservice.model.BuyerBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BuyerBalanceRepo extends JpaRepository<BuyerBalance, Long> {
    Optional<BuyerBalance> findByBuyerUsername(String buyerUsername);
}