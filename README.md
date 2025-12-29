# Домашнее задание №4 по КПО, ПИ, 2 курс

## Выполнила Рамазанова Зарина, студентка БПИ-249

Данный проект представляет собой реализацию распределенной системы интернет-магазина с использованием микросервисной архитектуры. 
Система разработана для обработки заказов и платежей с гарантированной доставкой сообщений и семантикой exactly once для финансовых операций.

## Начало работы
```
docker-compose build
docker-compose up -d
```

## Завершение работы
```
docker-compose down
```

## Документация API
Документация доступна через Swagger UI:

- **Orders Service:** http://localhost:8081/swagger-ui.html
- **Payments Service:** http://localhost:8082/swagger-ui.html

## Архитектура
Система построена по принципам микросервисной архитектуры с четким разделением ответственности:

1. API Gateway – порт 8080 - Центральный шлюз для маршрутизации запросов
2.  Orders Service – порт 8081 \
Отвечает за управление заказами:
```
POST   /api/orders           - Создать новый заказ (асинхронно запускает оплату)
GET    /api/orders/user/{userId} - Получить все заказы пользователя
GET    /api/orders/{orderId} - Получить информацию о заказе по ID
GET    /api/orders/health    - Health check сервиса
```
3. Payments Service – порт 8082
Отвечает за управление платежами и счетами:
```
POST   /api/accounts         - Создать новый счет (не более одного на пользователя)
POST   /api/accounts/deposit - Пополнить счет
GET    /api/accounts/{userId}/balance - Получить баланс счета
GET    /api/accounts/health  - Health check сервиса
```
4. Базы данных – PostgreSQL
Каждый сервис имеет свою изолированную БД:

- Orders DB: jdbc:postgresql://postgres-orders:5432/orders_db

- Payments DB: jdbc:postgresql://postgres-payments:5432/payments_db

5. Брокер сообщений – RabbitMQ
Очереди для асинхронной коммуникации:

- order.created.queue – события создания заказа

- payment.status.queue – события статуса оплаты

- dlq.queue – Dead Letter Queue для обработки ошибок

<img width="400" height="400" alt="image" src="https://github.com/user-attachments/assets/c5069cc5-43d7-407f-a911-a8c3ceadbc87" alt=""/>


с наступающим новым годом, рождеством, старым новым годом, татьяниным днем, днем таможенника и другими праздниками по вкусу!

