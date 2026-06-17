package ro.medfinder.medapp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ro.medfinder.medapp.dto.OrderStatusUpdate;
import ro.medfinder.medapp.entity.*;
import ro.medfinder.medapp.entity.enums.OrderStatus;
import ro.medfinder.medapp.repository.ClientRepository;
import ro.medfinder.medapp.repository.MedStockRepository;
import ro.medfinder.medapp.repository.OrderRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private MedStockRepository medStockRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private ClientOrderService clientOrderService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    private Order mockOrder;
    private Client mockClient;
    private MedStock mockMedStock;

    @BeforeEach
    void setUp() {
        mockClient = new Client();
        mockClient.setId(1L);

        mockOrder = new Order();
        mockOrder.setId(10L);
        mockOrder.setStatus(OrderStatus.PENDING);
        mockOrder.setClient(mockClient);

        Location loc = new Location();
        loc.setId(200L);
        mockOrder.setPickupLocation(loc);

        Medication med = new Medication();
        med.setId(100L);

        OrderItem item = new OrderItem();
        item.setMedication(med);
        item.setQuantity(5);
        mockOrder.getItems().add(item);

        mockMedStock = new MedStock();
        mockMedStock.setQuantity(20);
    }

    @Test
    void updateOrderStatus_pendingToAccepted_decreasesStock() {
        OrderStatusUpdate update = new OrderStatusUpdate();
        update.setNewStatus(OrderStatus.ACCEPTED);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(mockOrder));
        when(medStockRepository.findByLocationIdAndMedicationId(200L, 100L)).thenReturn(Optional.of(mockMedStock));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.updateOrderStatus(10L, update);

        assertEquals(OrderStatus.ACCEPTED, mockOrder.getStatus());
        assertNotNull(mockOrder.getAcceptedAt());
        assertEquals(15, mockMedStock.getQuantity()); // 20 - 5

        verify(medStockRepository, times(1)).save(mockMedStock);
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }

    @Test
    void updateOrderStatus_pendingToAccepted_insufficientStock_throwsException() {
        OrderStatusUpdate update = new OrderStatusUpdate();
        update.setNewStatus(OrderStatus.ACCEPTED);
        mockMedStock.setQuantity(2); // Only 2 in stock, order wants 5!

        when(orderRepository.findById(10L)).thenReturn(Optional.of(mockOrder));
        when(medStockRepository.findByLocationIdAndMedicationId(200L, 100L)).thenReturn(Optional.of(mockMedStock));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            orderService.updateOrderStatus(10L, update);
        });

        assertTrue(ex.getMessage().contains("Insufficient stock"));
        verify(medStockRepository, never()).save(any());
    }

    @Test
    void updateOrderStatus_pendingToRejected_restoresPerk() {
        OrderStatusUpdate update = new OrderStatusUpdate();
        update.setNewStatus(OrderStatus.REJECTED);
        update.setRejectionReason("Not available");

        mockOrder.setUsedFreePerk(true);
        mockClient.setFreeLongReservationsLeft(0);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.updateOrderStatus(10L, update);

        assertEquals(OrderStatus.REJECTED, mockOrder.getStatus());
        assertEquals(1, mockClient.getFreeLongReservationsLeft());

        verify(clientRepository, times(1)).save(mockClient);
    }

    @Test
    void updateOrderStatus_acceptedToReady_success() {
        mockOrder.setStatus(OrderStatus.ACCEPTED);
        
        OrderStatusUpdate update = new OrderStatusUpdate();
        update.setNewStatus(OrderStatus.READY_FOR_PICKUP);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.updateOrderStatus(10L, update);

        assertEquals(OrderStatus.READY_FOR_PICKUP, mockOrder.getStatus());
    }

    @Test
    void updateOrderStatus_invalidTransition_throwsException() {
        mockOrder.setStatus(OrderStatus.PENDING); // Pending can't go straight to Picked Up
        
        OrderStatusUpdate update = new OrderStatusUpdate();
        update.setNewStatus(OrderStatus.PICKED_UP);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(mockOrder));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            orderService.updateOrderStatus(10L, update);
        });

        assertTrue(ex.getMessage().contains("Invalid status transition"));
    }
}
