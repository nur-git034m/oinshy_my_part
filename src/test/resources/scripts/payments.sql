insert into city(id, name) values (1, 'Almaty');

insert into category(id, name, image) values (1, 'Cat 1', 'img1.jpg');
insert into toy(id, category_id, price, subscription_price, name, quantity, show, show_on_main_page)
    values (1, 1, 1000, 700, 'Toy 1', 10, true, true);
insert into toy_image(toy_id, file_name) values (1, '123.jpg');
insert into toy(id, category_id, price, subscription_price, name, quantity, show, show_on_main_page)
    values (2, 1, 1200, 900, 'Toy 2', 10, true, true);
insert into toy_image(toy_id, file_name) values (2, '456.jpg');

insert into client(id, phone_number, children, key) values (1, '12345678901', 1, '123');
insert into subscription(client_id, valid_until) values (1, dateadd('day', -1, now()));
insert into client_order(id, client_id, ordered_at, status, payment_type, paid, city_id, street, building, apartment)
    values (1, 1, now(), 'Delivered', 'OnDelivery', false, 1, 'Some st.', 'Some bld.', 'Some apt.');
insert into client_order(id, client_id, ordered_at, status, payment_type, paid, city_id, street, building, apartment)
    values (4, 1, now(), 'Ordered', 'ByPaymentCard', true, 1, 'Some st.', 'Some bld.', 'Some apt.');
insert into client_order(id, client_id, ordered_at, status, payment_type, paid, city_id, street, building, apartment, delivery_price)
    values (5, 1, now(), 'Ordered', 'ByPaymentCard', false, 1, 'Some st.', 'Some bld.', 'Some apt.', 1000);
insert into order_item(order_id, toy_id, price, quantity) values (5, 1, 1000, 2);
insert into order_item(order_id, toy_id, price, quantity) values (5, 2, 1300, 3);

insert into client(id, phone_number, children, key) values (2, '12345678902', 1, '123');
insert into subscription(client_id, valid_until) values (2, dateadd('day', 1, now()));
insert into client_order(id, client_id, ordered_at, status, payment_type, paid, city_id, street, building, apartment)
    values (2, 2, now(), 'Ordered', 'OnDelivery', false, 1, 'Some st.', 'Some bld.', 'Some apt.');
