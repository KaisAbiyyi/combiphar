# Combiphar Used Goods Core

Backend core (prototype) for PT Combiphar's **Second-Hands Goods Sales System**.

This repository currently contains a web app skeleton built with **Java 11 + Gradle + Javalin**, using **Pebble** templates, plus an initial database schema draft.

## Features (based on PRD/SRS/SDD)

Target system capabilities:
- Used-goods inventory and category management
- Transactions (cart/checkout) and order creation
- Payments: QRIS (automatic) and cash (manual admin validation)
- Shipment tracking (tracking number and status)
- User management with roles (RBAC)
- Sales and stock reporting

Note: the current codebase is still at an early stage (example route + template rendering).

## Prerequisites

- JDK 11
- Internet access to download Gradle dependencies (first run)

## Run

Windows (PowerShell/CMD):
- `gradlew.bat run`

Linux/macOS (bash):
- `./gradlew run`

The app runs at `http://localhost:7070/`.

## Build

- `./gradlew clean build`

## Project structure

- `src/main/java/Main.java` — Javalin entry point
- `src/main/resources/templates/` — Pebble templates (e.g., `home.pebble`)
- `database/schema.sql` — draft tables (users, categories, items, orders, order_items, payments, shipments)
- `docs/prd.md` — PRD
- `docs/SRS_Second-Hands_Goods_Sales_System.docx` — SRS
- `docs/SDD_Used-Goods.docx` — SDD

## Technical notes

- Template engine: Pebble via `io.javalin:javalin-rendering`.
- Default port: `7070`.
