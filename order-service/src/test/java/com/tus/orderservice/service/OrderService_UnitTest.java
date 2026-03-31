package com.tus.orderservice.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.client.RestTemplate;

import com.tus.orderservice.dto.OrderCreateDTO;
import com.tus.orderservice.dto.OrderDTO;
import com.tus.orderservice.entity.Order;
import com.tus.orderservice.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
public class OrderService_UnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RestTemplate restTemplate;

    private OrderService orderService;

    @BeforeEach
    public void setUp() {
        orderService = new OrderService(orderRepository, restTemplate);
        // Set the customer service URL via reflection since @Value won't work in unit tests
        try {
            java.lang.reflect.Field field = OrderService.class.getDeclaredField("customerServiceUrl");
            field.setAccessible(true);
            field.set(orderService, "http://localhost:9091");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test //TEST1: Create order successfully
    public void testCreateOrder_savesAndReturnsDTO() {
        Long customerId = 1L;
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setOrderDate(LocalDate.of(2026, 3, 1));
        dto.setAmount(99.99);

        // Stub: customer exists
        when(restTemplate.getForObject(anyString(), eq(Object.class))).thenReturn(new Object());

        Order saved = new Order();
        saved.setId(1L);
        saved.setOrderDate(LocalDate.of(2026, 3, 1));
        saved.setAmount(99.99);
        saved.setCustomerId(customerId);
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        OrderDTO result = orderService.createOrder(customerId, dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(99.99, result.getAmount());
        assertEquals(LocalDate.of(2026, 3, 1), result.getOrderDate());
        assertEquals(customerId, result.getCustomerId());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test //TEST2: Create order - customer not found
    public void testCreateOrder_customerNotFound_throws() {
        Long customerId = 99L;
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setOrderDate(LocalDate.now());
        dto.setAmount(50.0);

        when(restTemplate.getForObject(anyString(), eq(Object.class)))
                .thenThrow(new RuntimeException("Not found"));

        assertThrows(RuntimeException.class, () -> orderService.createOrder(customerId, dto));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test //TEST3: Get orders by customer
    public void testGetOrdersByCustomer_returnsList() {
        Long customerId = 1L;
        when(restTemplate.getForObject(anyString(), eq(Object.class))).thenReturn(new Object());

        Order o1 = new Order();
        o1.setId(1L);
        o1.setOrderDate(LocalDate.of(2026, 2, 25));
        o1.setAmount(99.99);
        o1.setCustomerId(customerId);

        Order o2 = new Order();
        o2.setId(2L);
        o2.setOrderDate(LocalDate.of(2026, 2, 26));
        o2.setAmount(150.50);
        o2.setCustomerId(customerId);

        when(orderRepository.findByCustomerId(customerId)).thenReturn(Arrays.asList(o1, o2));

        List<OrderDTO> list = orderService.getOrdersByCustomer(customerId);

        assertEquals(2, list.size());
        assertEquals(1L, list.get(0).getId());
        assertEquals(99.99, list.get(0).getAmount());
        assertEquals(2L, list.get(1).getId());
        verify(orderRepository, times(1)).findByCustomerId(customerId);
    }

    @Test //TEST4: Update order successfully
    public void testUpdateOrder_updatesAndReturnsDTO() {
        Long orderId = 1L;
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setOrderDate(LocalDate.of(2026, 4, 1));
        dto.setAmount(200.00);

        Order existing = new Order();
        existing.setId(orderId);
        existing.setOrderDate(LocalDate.of(2026, 3, 1));
        existing.setAmount(100.00);
        existing.setCustomerId(1L);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existing));

        Order updated = new Order();
        updated.setId(orderId);
        updated.setOrderDate(LocalDate.of(2026, 4, 1));
        updated.setAmount(200.00);
        updated.setCustomerId(1L);
        when(orderRepository.save(any(Order.class))).thenReturn(updated);

        OrderDTO result = orderService.updateOrder(orderId, dto);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(200.00, result.getAmount());
        assertEquals(LocalDate.of(2026, 4, 1), result.getOrderDate());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test //TEST5: Update order - not found
    public void testUpdateOrder_notFound_throws() {
        Long orderId = 99L;
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setOrderDate(LocalDate.now());
        dto.setAmount(50.0);

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.updateOrder(orderId, dto));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test //TEST6: Delete order successfully
    public void testDeleteOrder_exists_deletes() {
        Long orderId = 1L;
        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(1L);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        orderService.deleteOrder(orderId);

        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).delete(order);
    }

    @Test //TEST7: Delete order - not found
    public void testDeleteOrder_notFound_throws() {
        Long orderId = 99L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.deleteOrder(orderId));
        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test //TEST8: Get orders by date range
    public void testGetOrdersByDateRange_returnsList() {
        LocalDate start = LocalDate.of(2026, 2, 25);
        LocalDate end = LocalDate.of(2026, 2, 28);

        Order o1 = new Order();
        o1.setId(1L);
        o1.setOrderDate(LocalDate.of(2026, 2, 25));
        o1.setAmount(99.99);
        o1.setCustomerId(1L);

        Order o2 = new Order();
        o2.setId(2L);
        o2.setOrderDate(LocalDate.of(2026, 2, 28));
        o2.setAmount(45.00);
        o2.setCustomerId(2L);

        when(orderRepository.findByOrderDateBetween(start, end)).thenReturn(Arrays.asList(o1, o2));

        List<OrderDTO> list = orderService.getOrdersByDateRange(start, end);

        assertEquals(2, list.size());
        assertEquals(1L, list.get(0).getId());
        assertEquals(2L, list.get(1).getId());
        verify(orderRepository, times(1)).findByOrderDateBetween(start, end);
    }

    @Test //TEST9: Get paginated orders
    public void testGetPaginatedOrders_returnsPage() {
        Order o1 = new Order();
        o1.setId(1L);
        o1.setOrderDate(LocalDate.of(2026, 2, 25));
        o1.setAmount(99.99);
        o1.setCustomerId(1L);

        Order o2 = new Order();
        o2.setId(2L);
        o2.setOrderDate(LocalDate.of(2026, 2, 26));
        o2.setAmount(150.50);
        o2.setCustomerId(1L);

        Page<Order> orderPage = new PageImpl<>(Arrays.asList(o1, o2), PageRequest.of(0, 3), 2);
        when(orderRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(orderPage);

        Page<OrderDTO> result = orderService.getPaginatedOrders(0, 3);

        assertEquals(2, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
        assertEquals(2L, result.getContent().get(1).getId());
    }

}
