package com.tus.orderservice.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tus.orderservice.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
	
	//implementing date range filter for order service
	List<Order> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);

	// This built-in method supports pagination
    Page<Order> findAll(Pageable pageable);
    
    // Find all orders for a specific customer
    List<Order> findByCustomerId(Long customerId);
    
    // paginate orders for a specific customer
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

}
