package ie.atu.pospaymentservice.service;

import ie.atu.pospaymentservice.client.InventoryClient;
import ie.atu.pospaymentservice.client.PaymentClient;
import ie.atu.pospaymentservice.model.BuyerBalance;
import ie.atu.pospaymentservice.repository.BuyerBalanceRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentServiceTest {

    @Mock
    private BuyerBalanceRepo balanceRepo;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentService = new PaymentService(balanceRepo, inventoryClient);
    }

    @Test
    public void testAllocateFunds_NewUser() {
        when(balanceRepo.findByBuyerUsername("newUser")).thenReturn(Optional.empty());
        when(balanceRepo.save(any(BuyerBalance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BuyerBalance balance = paymentService.allocateFunds("newUser", 5000.0);

        assertNotNull(balance);
        assertEquals("newUser", balance.getBuyerUsername());
        assertEquals(5000.0, balance.getBalance());
        verify(balanceRepo, times(1)).save(any(BuyerBalance.class));
    }

    @Test
    public void testAllocateFunds_ExistingUser() {
        BuyerBalance existingBalance = new BuyerBalance(1L, "existingUser", 1000.0);
        when(balanceRepo.findByBuyerUsername("existingUser")).thenReturn(Optional.of(existingBalance));
        when(balanceRepo.save(any(BuyerBalance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BuyerBalance balance = paymentService.allocateFunds("existingUser", 500.0);

        assertEquals(1500.0, balance.getBalance());
        verify(balanceRepo, times(1)).save(existingBalance);
    }

    @Test
    public void testGetBalance_ExistingUser() {
        BuyerBalance balance = new BuyerBalance(1L, "testUser", 1000.0);
        when(balanceRepo.findByBuyerUsername("testUser")).thenReturn(Optional.of(balance));

        double result = paymentService.getBalance("testUser");

        assertEquals(1000.0, result);
    }

    @Test
    public void testGetBalance_UserNotFound() {
        when(balanceRepo.findByBuyerUsername("nonExistentUser")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            paymentService.getBalance("nonExistentUser");
        });

        assertEquals("No balance found for user: nonExistentUser", exception.getMessage());
    }

    @Test
    public void testPurchase_Successful() {
        BuyerBalance balance = new BuyerBalance(1L, "testUser", 1000.0);
        PaymentClient.PurchaseRequest.ItemDto itemDto = new PaymentClient.PurchaseRequest.ItemDto(1L, 1);
        List<PaymentClient.PurchaseRequest.ItemDto> items = Collections.singletonList(itemDto);

        when(balanceRepo.findByBuyerUsername("testUser")).thenReturn(Optional.of(balance));
        when(inventoryClient.decrementStock(eq(1L), any(InventoryClient.DecrementRequest.class)))
                .thenReturn("Stock decremented successfully");
        when(balanceRepo.save(any(BuyerBalance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = paymentService.purchase("testUser", items, 500.0);

        assertEquals("Purchase successful!", result);
        assertEquals(500.0, balance.getBalance());
    }

    @Test
    public void testPurchase_InsufficientBalance() {
        BuyerBalance balance = new BuyerBalance(1L, "testUser", 100.0);
        PaymentClient.PurchaseRequest.ItemDto itemDto = new PaymentClient.PurchaseRequest.ItemDto(1L, 1);
        List<PaymentClient.PurchaseRequest.ItemDto> items = Collections.singletonList(itemDto);

        when(balanceRepo.findByBuyerUsername("testUser")).thenReturn(Optional.of(balance));

        String result = paymentService.purchase("testUser", items, 200.0);

        assertEquals("Insufficient balance!", result);
        verify(balanceRepo, never()).save(any(BuyerBalance.class));
    }

    @Test
    public void testPurchase_FailedStockDecrement() {
        BuyerBalance balance = new BuyerBalance(1L, "testUser", 1000.0);
        PaymentClient.PurchaseRequest.ItemDto itemDto = new PaymentClient.PurchaseRequest.ItemDto(1L, 1);
        List<PaymentClient.PurchaseRequest.ItemDto> items = Collections.singletonList(itemDto);

        when(balanceRepo.findByBuyerUsername("testUser")).thenReturn(Optional.of(balance));
        when(inventoryClient.decrementStock(eq(1L), any(InventoryClient.DecrementRequest.class)))
                .thenReturn("Stock decrement failed");

        Exception exception = assertThrows(RuntimeException.class, () ->
                paymentService.purchase("testUser", items, 100.0));

        assertEquals("Stock reservation failed for product ID: 1", exception.getMessage());
    }
}
