-- Insert Sample Orders (customer_id references customers in Customer Service)
INSERT INTO Orders (order_date, amount, customer_id) 
VALUES ('2026-02-25', 99.99, 1);

INSERT INTO Orders (order_date, amount, customer_id) 
VALUES ('2026-02-26', 150.50, 1);

INSERT INTO Orders (order_date, amount, customer_id) 
VALUES ('2026-02-25', 150.50, 1);

INSERT INTO Orders (order_date, amount, customer_id) 
VALUES ('2026-02-28', 45.00, 2);
