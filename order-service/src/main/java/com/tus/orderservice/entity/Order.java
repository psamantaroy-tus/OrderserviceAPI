package com.tus.orderservice.entity;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity @Table(name = "orders") // avoid SQL keyword conflict
public class Order {	
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Long id; 
	private LocalDate orderDate; 
	private Double amount;
	
	// Store customerId as a plain field (customer lives in a separate microservice)
	@Column(name = "customer_id", nullable = false)
	private Long customerId;
	
	//constructors
	
	public Order() {}
	
	public Order(Long id, LocalDate orderDate, Double amount, Long customerId) {
		super();
		this.id = id;
		this.orderDate = orderDate;
		this.amount = amount;
		this.customerId = customerId;
	}
		
	//getters and setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDate orderDate) {
		this.orderDate = orderDate;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

}
