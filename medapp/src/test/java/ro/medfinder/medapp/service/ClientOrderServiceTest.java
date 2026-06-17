package ro.medfinder.medapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ro.medfinder.medapp.dto.OrderCreateRequest;
import ro.medfinder.medapp.entity.*;
import ro.medfinder.medapp.entity.enums.OrderStatus;
import ro.medfinder.medapp.repository.*;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientOrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private MedStockRepository medStockRepository;
    @Mock private MedicationRepository medicationRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private HoldingFeeCalculator holdingFeeCalculator;

    @InjectMocks
    private ClientOrderService clientOrderService;

    private Client mockClient;
    private OrderCreateRequest mockRequest;
    private Medication mockMedication;
    private Location mockLocation;
    private MedStock mockMedStock;

    @BeforeEach
    void setUp() {
        mockClient = new Client();
        mockClient.setId(1L);

        mockRequest = new OrderCreateRequest();
        mockRequest.setMedicationId(100L);
        mockRequest.setLocationId(200L);
        mockRequest.setQuantity(2);
        mockRequest.setReservationHours(3);

        mockMedication = new Medication();
        mockMedication.setId(100L);

        mockLocation = new Location();
        mockLocation.setId(200L);

        mockMedStock = new MedStock();
        mockMedStock.setAvailable(true);
        mockMedStock.setQuantity(10);
        mockMedStock.setPrice(new BigDecimal("50.00"));
    }

    @Test
    void createOrder_success() {
        // Arrange
        when(medicationRepository.findById(100L)).thenReturn(Optional.of(mockMedication));
        when(locationRepository.findById(200L)).thenReturn(Optional.of(mockLocation));
        when(medStockRepository.findByLocationIdAndMedicationId(200L, 100L)).thenReturn(Optional.of(mockMedStock));
        when(orderRepository.countByClientIdAndStatus(1L, OrderStatus.PENDING)).thenReturn(0L);
        when(orderRepository.existsActiveOrderToday(anyLong(), anyLong(), anyLong(), any(), any())).thenReturn(false);
        when(holdingFeeCalculator.calculate(3, mockClient)).thenReturn(new HoldingFeeCalculator.HoldingFeeResult(BigDecimal.ZERO, true));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order createdOrder = clientOrderService.createOrder(mockClient, mockRequest);

        // Assert
        assertNotNull(createdOrder);
        assertEquals(OrderStatus.PENDING, createdOrder.getStatus());
        assertEquals(1, createdOrder.getItems().size());
        assertEquals(0, new BigDecimal("100.00").compareTo(createdOrder.getTotalPrice())); // 2 * 50 + 0 fee
        assertTrue(createdOrder.getUsedFreePerk());
        
        verify(clientRepository, times(1)).save(mockClient); // Since perk was used
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_insufficientStock_throwsException() {
        mockMedStock.setQuantity(1); // Requested 2

        when(medicationRepository.findById(100L)).thenReturn(Optional.of(mockMedication));
        when(locationRepository.findById(200L)).thenReturn(Optional.of(mockLocation));
        when(medStockRepository.findByLocationIdAndMedicationId(200L, 100L)).thenReturn(Optional.of(mockMedStock));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            clientOrderService.createOrder(mockClient, mockRequest);
        });
        assertTrue(ex.getMessage().contains("Insufficient stock"));
    }

    @Test
    void createOrder_pendingLimitReached_throwsException() {
        when(medicationRepository.findById(100L)).thenReturn(Optional.of(mockMedication));
        when(locationRepository.findById(200L)).thenReturn(Optional.of(mockLocation));
        when(medStockRepository.findByLocationIdAndMedicationId(200L, 100L)).thenReturn(Optional.of(mockMedStock));
        when(orderRepository.countByClientIdAndStatus(1L, OrderStatus.PENDING)).thenReturn(10L); // Limit reached

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            clientOrderService.createOrder(mockClient, mockRequest);
        });
        assertTrue(ex.getMessage().contains("limita de 10 comenzi"));
    }

    @Test
    void createOrder_antiHoardingSameDay_throwsException() {
        when(medicationRepository.findById(100L)).thenReturn(Optional.of(mockMedication));
        when(locationRepository.findById(200L)).thenReturn(Optional.of(mockLocation));
        when(medStockRepository.findByLocationIdAndMedicationId(200L, 100L)).thenReturn(Optional.of(mockMedStock));
        when(orderRepository.countByClientIdAndStatus(1L, OrderStatus.PENDING)).thenReturn(0L);
        when(orderRepository.existsActiveOrderToday(anyLong(), anyLong(), anyLong(), any(), any())).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            clientOrderService.createOrder(mockClient, mockRequest);
        });
        assertTrue(ex.getMessage().contains("already have an active reservation"));
    }

    @Test
    void cancelOrder_restoresStockAndPerk() {
        Order mockOrder = new Order();
        mockOrder.setClient(mockClient);
        mockOrder.setStatus(OrderStatus.ACCEPTED); // Stock was deducted here!
        mockOrder.setUsedFreePerk(true);
        
        OrderItem item = new OrderItem();
        item.setMedication(mockMedication);
        item.setQuantity(2);
        mockOrder.getItems().add(item);
        mockOrder.setPickupLocation(mockLocation);

        mockClient.setFreeLongReservationsLeft(0);

        when(orderRepository.findById(99L)).thenReturn(Optional.of(mockOrder));
        when(medStockRepository.findByLocationIdAndMedicationId(200L, 100L)).thenReturn(Optional.of(mockMedStock));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order cancelled = clientOrderService.cancelOrder(mockClient, 99L);

        // Assert
        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        assertEquals(1, mockClient.getFreeLongReservationsLeft()); // Restored
        assertEquals(12, mockMedStock.getQuantity()); // 10 original + 2 restored
        
        verify(clientRepository, times(1)).save(mockClient);
        verify(medStockRepository, times(1)).save(mockMedStock);
    }

    @Test
    void cancelOrder_invalidStatus_throwsException() {
        Order mockOrder = new Order();
        mockOrder.setClient(mockClient);
        mockOrder.setStatus(OrderStatus.PICKED_UP); // Too late to cancel

        when(orderRepository.findById(99L)).thenReturn(Optional.of(mockOrder));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            clientOrderService.cancelOrder(mockClient, 99L);
        });
        assertTrue(ex.getMessage().contains("Cannot cancel order"));
    }
}
