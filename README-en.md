
# Smart-Dining

## Overview

Smart-Dining is a takeout/food-ordering backend service covering the end-to-end user ordering flow and admin-side operations & analytics. It is **built on top of an open-source takeout project**.

## Key Features

Client (User):
- WeChat Login: integrates WeChat login and issues JWT for user authentication
- Category/Dish/Setmeal Browsing: browse dishes and setmeals by category
- Shopping Cart: add/remove items and clear cart
- Ordering: submit orders, view order details, and browse order history
- Payment: reserved WeChat Pay capability and payment callback integration
- Coupon Claiming: uses Redis middleware to guarantee atomicity under high-concurrency coupon grabs
- Coupon Querying: list available coupons and view my usable coupons
- Address Book: create/update/delete addresses and set default address
- Order Reminder: user “urge order” triggers server-side notification logic

Admin (Management):
- Staff Login: JWT-based authentication for admin users
- Staff Management: create/edit, paginated listing, enable/disable accounts
- Category Management: CRUD for dish/setmeal categories
- Dish Management: create/edit, on/off-shelf, paginated listing
- Setmeal Management: create/edit, on/off-shelf, paginated listing
- Order Management: search orders; accept/reject/cancel; deliver/complete
- Shop Status: toggle and query shop open/closed status
- Workspace Dashboard: today’s business data and overview metrics
- Analytics: turnover/user/order statistics and Top 10 sales ranking
- File Upload: upload endpoint (currently local storage; extensible to OSS)
- Coupon Publishing: publish coupons and initialize stock; list available coupons

Async & Notifications:
- Peak Shaving for Coupon Persistence: push successful claims to a Redis queue, then consume asynchronously to persist into MySQL and decrement DB stock
- WebSocket Push: WebSocket server support for message notifications (includes a scheduled push example)

## Tech Stack

- Java / Spring Boot 2.7.3
- Spring MVC / Spring AOP
- MyBatis + PageHelper
- MySQL
- Redis (Spring Data Redis) + Lua scripts + Spring Cache
- JWT (jjwt)
- WebSocket (spring-boot-starter-websocket + javax.websocket)
- Knife4j (Swagger API docs)
- Druid connection pool
- WeChat Pay API v3 (wechatpay-apache-httpclient) + Apache HttpClient
- Fastjson
- Apache POI (report/export capabilities)
- Maven multi-module project
- Test data generator: JavaFaker (sky-data-generator)

## Project Structure

- `backend/`: Maven multi-module backend
  - `sky-server`: core business service (Controller/Service/Mapper)
  - `sky-common`: shared utilities, constants, exceptions, configuration
  - `sky-pojo`: models (Entity/DTO/VO)
  - `sky-data-generator`: test data generation tool
- `docs/`: API docs, prototypes, and design documents
- `data/db/`: database backup/initialization scripts

