# **Product Requirements Document (PRD)**

Project Name: Second-Hands Goods Sales System (Combiphar)  
Version: 2.0 (Comprehensive)  
Status: Approved for Development  
Authors: Kais Abiyyi, Nazar Muhammad Fikri Fadhillah, Jarwo Eddy Wicaksono  
Last Updated: November 19, 2025

## **1\. Introduction**

### **1.1 Purpose**

The purpose of this document is to define the functional and non-functional requirements, system architecture, and design specifications for the **Second-Hands Goods Sales System**. This system aims to digitize the sales process of PT Combiphar's used corporate assets (medical equipment, office furniture, logistics gear) to improve efficiency, transparency, and data accuracy.

### **1.2 Scope**

The system is a web-based application facilitating:

* **Inventory Management:** Recording and categorizing used goods and their conditions.  
* **Sales Transactions:** End-to-end purchasing flow for customers.  
* **Payment Processing:** Handling QRIS (automated) and Cash (manual validation) payments.  
* **Logistics:** Tracking shipment status and delivery.  
* **Reporting:** Generating automated sales and stock reports.

### **1.3 Key Definitions**

* **QRIS:** Quick Response Code Indonesian Standard (Digital Payment).  
* **Feasibility Status:** A status determined by Admin (Eligible, Needs Repair, Not Eligible).  
* **RBAC:** Role-Based Access Control.

## **2\. User Personas & Roles**

| Role | Description & Responsibilities |
| :---- | :---- |
| **Admin Toko (Admin)** | Internal staff responsible for operations. Can manage inventory, validate cash payments, update shipment status, manage users, and view reports. |
| **Pelanggan (Customer)** | Employees or general public. Can browse catalog, place orders, pay via QRIS/Cash, and track order history. |
| **Pengguna Baru (Guest)** | Unregistered users who can visit the landing page and register for an account. |

## **3\. Functional Requirements (Detailed)**

### **3.1 Module: Inventory & Catalog**

| ID | Requirement | Description |
| :---- | :---- | :---- |
| **FR-01** | **Manage Items** | Admin must be able to Create, Read, Update, and Delete (CRUD) items. Data includes: Name, Category, Condition (New/Used/Damaged), Price, Stock, and Description. |
| **FR-02** | **Manage Categories** | Admin can manage item categories to classify inventory structuredly. |
| **FR-03** | **Product Catalog** | System displays a searchable and filterable list of items for Customers. Filters include Category and Price Range. |
| **FR-14** | **Item Condition Info** | System must display detailed condition information on the Product Detail Page (PDP). |
| **FR-15** | **Feasibility Check** | Admin can set eligibility\_status: 1\. ELIGIBLE (Publishable) 2\. NEEDS\_REPAIR (Hidden) 3\. NOT\_ELIGIBLE (Archived/Discarded). |

### **3.2 Module: Transactions & Orders**

| ID | Requirement | Description |
| :---- | :---- | :---- |
| **FR-04** | **Order Processing** | Customers can add items to a cart, input shipping details, and checkout. System generates a unique order\_number. |
| **FR-12** | **Stock Deduction** | System automatically decrements item stock upon successful order placement (Atomic transaction). |

### **3.3 Module: Payments**

| ID | Requirement | Description |
| :---- | :---- | :---- |
| **FR-05** | **Payment Methods** | System supports **QRIS** and **Cash**. |
| **FR-06** | **Payment Validation** | **QRIS:** validated automatically via gateway callback. **Cash:** Admin manually clicks "Confirm Payment" after receiving money at the store. |

### **3.4 Module: Logistics (Shipment)**

| ID | Requirement | Description |
| :---- | :---- | :---- |
| **FR-07** | **Record Shipment** | Admin inputs courier\_name and tracking\_number for processed orders. |
| **FR-08** | **Update Status** | Admin updates shipment status: PENDING \-\> PACKED \-\> SHIPPED \-\> DELIVERED. |

### **3.5 Module: User Management**

| ID | Requirement | Description |
| :---- | :---- | :---- |
| **FR-09** | **Self-Registration** | Guests can sign up as Customers providing Name, Email, and Password. |
| **FR-10** | **User Administration** | Admin can manage user accounts and assigning roles. |
| **FR-11** | **RBAC** | Middleware must restrict access to Admin pages (Dashboard, Inventory) from Customers. |

### **3.6 Module: Reporting**

| ID | Requirement | Description |
| :---- | :---- | :---- |
| **FR-13** | **Generate Reports** | System generates **Sales Reports** (Revenue, Items Sold) and **Stock Reports** based on a selected date range. |

## **4\. Non-Functional Requirements (NFR)**

* **NFR-01 Performance:** System response time \< 3 seconds for normal operations.  
* **NFR-02 Security:** Data encryption for passwords (bcrypt) and secure transaction processing.  
* **NFR-03 Access Control:** Strict RBAC implementation (Admin vs Customer guards).  
* **NFR-04 Reliability:** Automatic database backups to ensure data integrity.  
* **NFR-05 Usability:** Intuitive UI requiring minimal training for Admins.  
* **NFR-07 Compatibility:** Fully responsive design (Mobile First & Desktop support).  
* **NFR-08 Scalability:** Modular architecture to support future API integrations.

## **5\. System Architecture & Database Design**

### **5.1 Technology Stack**

* **Frontend:** React.js / Next.js (Tailwind CSS)  
* **Backend:** Node.js (Express) / Laravel (PHP)  
* **Database:** MySQL / PostgreSQL  
* **Payment:** Third-party QRIS Gateway Integration

### **5.2 Database Schema (Physical Design)**

The system consists of 7 core tables:

1. **users**  
   * id (PK), name, email (Unique), password, role (ENUM: ADMIN, CUSTOMER), status.  
2. **categories**  
   * id (PK), name, description.  
3. **items**  
   * id (PK), category\_id (FK), name, condition (ENUM), price, stock, eligibility\_status (ENUM), is\_published.  
4. **orders**  
   * id (PK), user\_id (FK), order\_number, total\_price, payment\_method, status\_payment (PENDING, PAID, FAILED), status\_order.  
5. **order\_items**  
   * id (PK), order\_id (FK), item\_id (FK), quantity, unit\_price, subtotal.  
6. **payments**  
   * id (PK), order\_id (FK), type (QRIS/CASH), amount, status, qris\_ref.  
7. **shipments**  
   * id (PK), order\_id (FK), courier\_name, tracking\_number, shipment\_status.

## **6\. Process Flows (Key Use Cases)**

### **6.1 QRIS Payment Flow**

1. Customer creates Order \-\> Selects **QRIS**.  
2. System creates Order (Pending) \-\> Calls Gateway \-\> Displays QR Code.  
3. Customer scans & pays.  
4. Gateway sends Callback (Webhook) \-\> System updates Payment to SUCCESS & Order to PAID.

### **6.2 Cash Payment Flow**

1. Customer creates Order \-\> Selects **Cash**.  
2. System creates Order (Pending) \-\> Displays "Pay at Store" instructions.  
3. Customer pays Admin at store.  
4. Admin searches Order ID \-\> Clicks **"Confirm Cash Payment"**.  
5. System updates Payment to SUCCESS & Order to PAID.

### **6.3 Inventory Feasibility Flow**

1. Admin adds new item.  
2. Item status defaults to UNDER\_REVIEW (or Admin sets explicitly).  
3. Admin evaluates item:  
   * If Good \-\> Set eligibility\_status \= **ELIGIBLE** \-\> Item appears in Catalog.  
   * If Damaged \-\> Set eligibility\_status \= **NEEDS\_REPAIR** \-\> Item hidden.

## **7\. UI/UX Requirements**

The application must feature the following pages:

1. **Landing/Home Page:** Hero banner, featured items.  
2. **Catalog Page:** Grid view, sidebar filters (Category, Price).  
3. **Product Detail Page:** Large images, condition details, Add to Cart.  
4. **Cart & Checkout:** Review items, shipping form, payment selection.  
5. **User Dashboard:** Order history, profile settings.  
6. **Admin Dashboard:** KPI Cards (Sales, Stock), Charts.  
7. **Admin Inventory Management:** Data tables with CRUD actions.  
8. **Admin Order Management:** List of orders with status change actions.