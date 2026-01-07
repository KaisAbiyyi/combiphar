

## 1. USER

```
┌─────────────────────────────────────────────────────────────────────┐
│                              User                                    │
├─────────────────────────────────────────────────────────────────────┤
│ - id: String                                                         │
│ - email: String                                                      │
│ - password: String                                                   │
│ - name: String                                                       │
│ - role: Role                                                         │
│ - status: String                                                     │
│ - createdAt: LocalDateTime                                           │
│ - updatedAt: LocalDateTime                                           │
├─────────────────────────────────────────────────────────────────────┤
│ «from UserRepository»                                                │
│ + findByEmail(email: String): Optional<User>                         │
│ + findById(id: String): Optional<User>                               │
│ + findAll(): List<User>                                              │
│ + save(user: User): User                                             │
│ + existsByEmail(email: String): boolean                              │
│ + countByRole(role: Role): int                                       │
├─────────────────────────────────────────────────────────────────────┤
│ «from AuthService»                                                   │
│ + login(email: String, password: String): Optional<User>             │
│ + register(name: String, email: String, password: String): User      │
│ + hashPassword(rawPassword: String): String                          │
│ + verifyPassword(rawPassword: String, hashedPassword: String): bool  │
│ + getCurrentUser(session: HttpSession): Optional<User>               │
├─────────────────────────────────────────────────────────────────────┤
│ «from AuthController»                                                │
│ + showLoginPage(): String                                            │
│ + showRegisterPage(): String                                         │
│ + processLogin(email, password, session): String                     │
│ + processRegister(name, email, password): String                     │
│ + logout(session: HttpSession): String                               │
├─────────────────────────────────────────────────────────────────────┤
│ «from AdminUserController»                                           │
│ + listUsers(model: Model): String                                    │
│ + updateUserStatus(userId: String, status: String): String           │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. ROLE (Enum)

```
┌─────────────────────────────────────────────────────────────────────┐
│                           «enumeration»                              │
│                              Role                                    │
├─────────────────────────────────────────────────────────────────────┤
│ CUSTOMER                                                             │
│ ADMIN                                                                │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. ADDRESS

```
┌─────────────────────────────────────────────────────────────────────┐
│                             Address                                  │
├─────────────────────────────────────────────────────────────────────┤
│ - id: String                                                         │
│ - userId: String                                                     │
│ - label: String                                                      │
│ - recipientName: String                                              │
│ - phone: String                                                      │
│ - fullAddress: String                                                │
│ - city: String                                                       │
│ - province: String                                                   │
│ - postalCode: String                                                 │
│ - isPrimary: boolean                                                 │
│ - createdAt: LocalDateTime                                           │
│ - updatedAt: LocalDateTime                                           │
├─────────────────────────────────────────────────────────────────────┤
│ «from AddressRepository»                                             │
│ + findById(id: String): Optional<Address>                            │
│ + findByUserId(userId: String): List<Address>                        │
│ + findPrimaryByUserId(userId: String): Optional<Address>             │
│ + save(address: Address): Address                                    │
│ + deleteById(id: String): void                                       │
│ + existsById(id: String): boolean                                    │
│ + countByUserId(userId: String): int                                 │
├─────────────────────────────────────────────────────────────────────┤
│ «from AddressController»                                             │
│ + listAddresses(session: HttpSession, model: Model): String          │
│ + showAddForm(model: Model): String                                  │
│ + showEditForm(id: String, model: Model): String                     │
│ + createAddress(form: AddressForm, session: HttpSession): String     │
│ + updateAddress(id: String, form: AddressForm): String               │
│ + deleteAddress(id: String, session: HttpSession): String            │
│ + setPrimary(id: String, session: HttpSession): String               │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 4. CATEGORY

```
┌─────────────────────────────────────────────────────────────────────┐
│                            Category                                  │
├─────────────────────────────────────────────────────────────────────┤
│ - id: String                                                         │
│ - name: String                                                       │
│ - description: String                                                │
│ - createdAt: LocalDateTime                                           │
│ - updatedAt: LocalDateTime                                           │
├─────────────────────────────────────────────────────────────────────┤
│ «from CategoryRepository»                                            │
│ + findById(id: String): Optional<Category>                           │
│ + findByName(name: String): Optional<Category>                       │
│ + findAll(): List<Category>                                          │
│ + save(category: Category): Category                                 │
│ + deleteById(id: String): void                                       │
│ + existsById(id: String): boolean                                    │
│ + existsByName(name: String): boolean                                │
├─────────────────────────────────────────────────────────────────────┤
│ «from CategoryController»                                            │
│ + listCategories(model: Model): String                               │
│ + showCreateForm(model: Model): String                               │
│ + showEditForm(id: String, model: Model): String                     │
│ + createCategory(name: String, description: String): String          │
│ + updateCategory(id: String, name: String, desc: String): String     │
│ + deleteCategory(id: String): String                                 │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 5. ITEM

```
┌─────────────────────────────────────────────────────────────────────┐
│                              Item                                    │
├─────────────────────────────────────────────────────────────────────┤
│ - id: String                                                         │
│ - categoryId: String                                                 │
│ - name: String                                                       │
│ - description: String                                                │
│ - price: BigDecimal                                                  │
│ - stock: int                                                         │
│ - imageUrl: String                                                   │
│ - status: ItemStatus                                                 │
│ - createdAt: LocalDateTime                                           │
│ - updatedAt: LocalDateTime                                           │
├─────────────────────────────────────────────────────────────────────┤
│ «from ItemRepository»                                                │
│ + findById(id: String): Optional<Item>                               │
│ + findAll(): List<Item>                                              │
│ + findByStatus(status: ItemStatus): List<Item>                       │
│ + findByCategoryId(categoryId: String): List<Item>                   │
│ + findByStatusAndCategoryId(status, categoryId): List<Item>          │
│ + save(item: Item): Item                                             │
│ + deleteById(id: String): void                                       │
│ + existsById(id: String): boolean                                    │
│ + searchByName(query: String): List<Item>                            │
│ + countByStatus(status: ItemStatus): int                             │
│ + countByCategoryId(categoryId: String): int                         │
│ + updateStock(id: String, newStock: int): void                       │
├─────────────────────────────────────────────────────────────────────┤
│ «from ItemController»                                                │
│ + listItems(model: Model): String                                    │
│ + showCreateForm(model: Model): String                               │
│ + showEditForm(id: String, model: Model): String                     │
│ + createItem(form: ItemForm, image: MultipartFile): String           │
│ + updateItem(id: String, form: ItemForm, image: MultipartFile): Str  │
│ + deleteItem(id: String): String                                     │
├─────────────────────────────────────────────────────────────────────┤
│ «from CatalogController»                                             │
│ + showCatalogPage(categoryId: String, model: Model): String          │
│ + showProductDetail(id: String, model: Model): String                │
│ + searchProducts(query: String, model: Model): String                │
├─────────────────────────────────────────────────────────────────────┤
│ «from QualityCheckController»                                        │
│ + listPendingItems(model: Model): String                             │
│ + showQcDetail(id: String, model: Model): String                     │
│ + performQualityCheck(id: String, approved: boolean): String         │
│ + batchApprove(itemIds: List<String>): String                        │
│ + batchReject(itemIds: List<String>): String                         │
│ + publishItem(id: String): String                                    │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 6. ITEM STATUS (Enum)

```
┌─────────────────────────────────────────────────────────────────────┐
│                           «enumeration»                              │
│                            ItemStatus                                │
├─────────────────────────────────────────────────────────────────────┤
│ PENDING_QC                                                           │
│ APPROVED                                                             │
│ REJECTED                                                             │
│ PUBLISHED                                                            │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 7. CART

```
┌─────────────────────────────────────────────────────────────────────┐
│                              Cart                                    │
├─────────────────────────────────────────────────────────────────────┤
│ - id: String                                                         │
│ - userId: String                                                     │
│ - createdAt: LocalDateTime                                           │
│ - updatedAt: LocalDateTime                                           │
├─────────────────────────────────────────────────────────────────────┤
│ «from CartRepository»                                                │
│ + findById(id: String): Optional<Cart>                               │
│ + findByUserId(userId: String): Optional<Cart>                       │
│ + save(cart: Cart): Cart                                             │
│ + deleteById(id: String): void                                       │
│ + deleteByUserId(userId: String): void                               │
├─────────────────────────────────────────────────────────────────────┤
│ «from CartService»                                                   │
│ + getOrCreateCart(userId: String): Cart                              │
│ + getCartWithItems(userId: String): CartWithItems                    │
│ + addItem(userId: String, itemId: String, quantity: int): void       │
│ + updateQuantity(userId: String, itemId: String, qty: int): void     │
│ + removeItem(userId: String, itemId: String): void                   │
│ + clearCart(userId: String): void                                    │
│ + getCartTotal(userId: String): BigDecimal                           │
│ + getCartItemCount(userId: String): int                              │
│ + isEmpty(userId: String): boolean                                   │
├─────────────────────────────────────────────────────────────────────┤
│ «from CartController»                                                │
│ + viewCart(session: HttpSession, model: Model): String               │
│ + addToCart(itemId: String, qty: int, session: HttpSession): String  │
│ + updateCartItem(itemId: String, qty: int, session): String          │
│ + removeFromCart(itemId: String, session: HttpSession): String       │
│ + clearCart(session: HttpSession): String                            │
│ + getCartCount(session: HttpSession): ResponseEntity<Integer>        │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 8. CART ITEM

```
┌─────────────────────────────────────────────────────────────────────┐
│                            CartItem                                  │
├─────────────────────────────────────────────────────────────────────┤
│ - id: String                                                         │
│ - cartId: String                                                     │
│ - itemId: String                                                     │
│ - quantity: int                                                      │
│ - createdAt: LocalDateTime                                           │
│ - updatedAt: LocalDateTime                                           │
│                                                                      │
│ «transient/joined»                                                   │
│ - item: Item                                                         │
├─────────────────────────────────────────────────────────────────────┤
│ «from CartItemRepository»                                            │
│ + findById(id: String): Optional<CartItem>                           │
│ + findByCartId(cartId: String): List<CartItem>                       │
│ + findByCartIdAndItemId(cartId, itemId: String): Optional<CartItem>  │
│ + save(cartItem: CartItem): CartItem                                 │
│ + deleteById(id: String): void                                       │
│ + deleteByCartId(cartId: String): void                               │
│ + deleteByCartIdAndItemId(cartId: String, itemId: String): void      │
│ + existsByCartIdAndItemId(cartId: String, itemId: String): boolean   │
│ + countByCartId(cartId: String): int                                 │
│ + findByCartIdWithItems(cartId: String): List<CartItem>              │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 9. ORDER

```
┌─────────────────────────────────────────────────────────────────────┐
│                              Order                                   │
├─────────────────────────────────────────────────────────────────────┤
│ - id: String                                                         │
│ - userId: String                                                     │
│ - addressId: String                                                  │
│ - totalAmount: BigDecimal                                            │
│ - shippingCost: BigDecimal                                           │
│ - status: OrderStatus                                                │
│ - courierName: String                                                │
│ - notes: String                                                      │
│ - createdAt: LocalDateTime                                           │
│ - updatedAt: LocalDateTime                                           │
│                                                                      │
│ «transient/computed»                                                 │
│ - orderItems: List<OrderItem>                                        │
│ - payment: Payment                                                   │
│ - shipment: Shipment                                                 │
│ - address: Address                                                   │
├─────────────────────────────────────────────────────────────────────┤
│ «from OrderRepository»                                               │
│ + findById(id: String): Optional<Order>                              │
│ + findByUserId(userId: String): List<Order>                          │
│ + findAll(): List<Order>                                             │
│ + findByStatus(status: OrderStatus): List<Order>                     │
│ + save(order: Order): Order                                          │
│ + deleteById(id: String): void                                       │
│ + countByStatus(status: OrderStatus): int                            │
│ + countByUserId(userId: String): int                                 │
│ + findRecentOrders(limit: int): List<Order>                          │
│ + updateStatus(id: String, status: OrderStatus): void                │
├─────────────────────────────────────────────────────────────────────┤
│ «from OrderService»                                                  │
│ + createOrder(userId, addressId, courierName, notes): Order          │
│ + getOrderById(id: String): Optional<Order>                          │
│ + getOrderWithDetails(id: String): OrderDetails                      │
│ + getOrdersByUser(userId: String): List<Order>                       │
│ + calculateOrder(cart, courierName): OrderSummary                    │
│ + getAvailableCouriers(): Map<String, BigDecimal>                    │
│ + cancelOrder(orderId: String): void                                 │
│ + updateOrderStatus(orderId: String, status: OrderStatus): void      │
├─────────────────────────────────────────────────────────────────────┤
│ «from CheckoutController»                                            │
│ + showCheckoutPage(session: HttpSession, model: Model): String       │
│ + showAddressSelection(session: HttpSession, model: Model): String   │
│ + selectAddress(addressId: String, session: HttpSession): String     │
│ + showCourierSelection(session: HttpSession, model: Model): String   │
│ + selectCourier(courierName: String, session: HttpSession): String   │
│ + showOrderSummary(session: HttpSession, model: Model): String       │
│ + placeOrder(notes: String, session: HttpSession): String            │
│ + showOrderConfirmation(orderId: String, model: Model): String       │
├─────────────────────────────────────────────────────────────────────┤
│ «from AdminOrderController»                                          │
│ + listOrders(status: String, model: Model): String                   │
│ + viewOrderDetail(orderId: String, model: Model): String             │
│ + updateOrderStatus(orderId: String, status: OrderStatus): String    │
│ + cancelOrder(orderId: String): String                               │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 10. ORDER STATUS (Enum)

```
┌─────────────────────────────────────────────────────────────────────┐
│                           «enumeration»                              │
│                           OrderStatus                                │
├─────────────────────────────────────────────────────────────────────┤
│ PENDING_PAYMENT                                                      │
│ PAYMENT_UPLOADED                                                     │
│ PAYMENT_VERIFIED                                                     │
│ PROCESSING                                                           │
│ SHIPPED                                                              │
│ DELIVERED                                                            │
│ CANCELLED                                                            │
│ REFUNDED                                                             │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 11. ORDER ITEM

```
┌─────────────────────────────────────────────────────────────────────┐
│                            OrderItem                                 │
├─────────────────────────────────────────────────────────────────────┤
│ - id: String                                                         │
│ - orderId: String                                                    │
│ - itemId: String                                                     │
│ - quantity: int                                                      │
│ - priceAtPurchase: BigDecimal                                        │
│ - createdAt: LocalDateTime                                           │
│                                                                      │
│ «transient/joined»                                                   │
│ - item: Item                                                         │
├─────────────────────────────────────────────────────────────────────┤
│ «from OrderItemRepository»                                           │
│ + findById(id: String): Optional<OrderItem>                          │
│ + findByOrderId(orderId: String): List<OrderItem>                    │
│ + findByOrderIdWithItems(orderId: String): List<OrderItem>           │
│ + save(orderItem: OrderItem): OrderItem                              │
│ + saveAll(orderItems: List<OrderItem>): List<OrderItem>              │
│ + deleteByOrderId(orderId: String): void                             │
│ + countByOrderId(orderId: String): int                               │
│ + calculateSubtotal(orderId: String): BigDecimal                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 12. PAYMENT

```
┌─────────────────────────────────────────────────────────────────────┐
│                             Payment                                  │
├─────────────────────────────────────────────────────────────────────┤
│ - id: String                                                         │
│ - orderId: String                                                    │
│ - amount: BigDecimal                                                 │
│ - bank: String                                                       │
│ - accountNumber: String                                              │
│ - accountName: String                                                │
│ - proofImageUrl: String                                              │
│ - status: PaymentStatus                                              │
│ - notes: String                                                      │
│ - verifiedAt: LocalDateTime                                          │
│ - verifiedBy: String                                                 │
│ - createdAt: LocalDateTime                                           │
│ - updatedAt: LocalDateTime                                           │
├─────────────────────────────────────────────────────────────────────┤
│ «from PaymentRepository»                                             │
│ + findById(id: String): Optional<Payment>                            │
│ + findByOrderId(orderId: String): Optional<Payment>                  │
│ + findAll(): List<Payment>                                           │
│ + findByStatus(status: PaymentStatus): List<Payment>                 │
│ + save(payment: Payment): Payment                                    │
│ + deleteById(id: String): void                                       │
│ + countByStatus(status: PaymentStatus): int                          │
│ + updateStatus(id: String, status: PaymentStatus): void              │
├─────────────────────────────────────────────────────────────────────┤
│ «from PaymentService»                                                │
│ + createPayment(orderId: String, amount: BigDecimal): Payment        │
│ + getPaymentByOrderId(orderId: String): Optional<Payment>            │
│ + uploadProof(orderId, bank, accNum, accName, proofFile): Payment    │
│ + verifyPayment(paymentId: String, adminId: String): void            │
│ + rejectPayment(paymentId: String, adminId: String, notes): void     │
│ + getPendingPayments(): List<Payment>                                │
├─────────────────────────────────────────────────────────────────────┤
│ «from PaymentController»                                             │
│ + showPaymentPage(orderId: String, model: Model): String             │
│ + showPaymentStatus(orderId: String, model: Model): String           │
├─────────────────────────────────────────────────────────────────────┤
│ «from PaymentUploadController»                                       │
│ + showUploadForm(orderId: String, model: Model): String              │
│ + uploadPaymentProof(orderId, bank, accNum, accName, file): String   │
├─────────────────────────────────────────────────────────────────────┤
│ «from AdminPaymentController»                                        │
│ + listPendingPayments(model: Model): String                          │
│ + viewPaymentDetail(paymentId: String, model: Model): String         │
│ + verifyPayment(paymentId: String, session: HttpSession): String     │
│ + rejectPayment(paymentId, notes, session: HttpSession): String      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 13. PAYMENT STATUS (Enum)

```
┌─────────────────────────────────────────────────────────────────────┐
│                           «enumeration»                              │
│                          PaymentStatus                               │
├─────────────────────────────────────────────────────────────────────┤
│ PENDING                                                              │
│ UPLOADED                                                             │
│ VERIFIED                                                             │
│ REJECTED                                                             │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 14. SHIPMENT

```
┌─────────────────────────────────────────────────────────────────────┐
│                            Shipment                                  │
├─────────────────────────────────────────────────────────────────────┤
│ - id: String                                                         │
│ - orderId: String                                                    │
│ - addressId: String                                                  │
│ - courierName: String                                                │
│ - trackingNumber: String                                             │
│ - status: ShipmentStatus                                             │
│ - shippedAt: LocalDateTime                                           │
│ - deliveredAt: LocalDateTime                                         │
│ - notes: String                                                      │
│ - createdAt: LocalDateTime                                           │
│ - updatedAt: LocalDateTime                                           │
│                                                                      │
│ «transient/joined»                                                   │
│ - address: Address                                                   │
│ - order: Order                                                       │
├─────────────────────────────────────────────────────────────────────┤
│ «from ShipmentRepository»                                            │
│ + findById(id: String): Optional<Shipment>                           │
│ + findByOrderId(orderId: String): Optional<Shipment>                 │
│ + findAll(): List<Shipment>                                          │
│ + findByStatus(status: ShipmentStatus): List<Shipment>               │
│ + save(shipment: Shipment): Shipment                                 │
│ + deleteById(id: String): void                                       │
│ + countByStatus(status: ShipmentStatus): int                         │
│ + updateStatus(id: String, status: ShipmentStatus): void             │
│ + updateTrackingNumber(id: String, trackingNumber: String): void     │
├─────────────────────────────────────────────────────────────────────┤
│ «from ShipmentService»                                               │
│ + createShipment(orderId: String, addressId: String): Shipment       │
│ + getShipmentByOrderId(orderId: String): Optional<Shipment>          │
│ + updateTrackingNumber(shipmentId, trackingNumber: String): void     │
│ + markAsShipped(shipmentId: String): void                            │
│ + markAsDelivered(shipmentId: String): void                          │
│ + updateStatus(shipmentId: String, status: ShipmentStatus): void     │
│ + getShipmentsByStatus(status: ShipmentStatus): List<Shipment>       │
├─────────────────────────────────────────────────────────────────────┤
│ «from AdminShipmentController»                                       │
│ + listShipments(status: String, model: Model): String                │
│ + viewShipmentDetail(shipmentId: String, model: Model): String       │
│ + updateTrackingNumber(shipmentId, trackingNumber: String): String   │
│ + updateShipmentStatus(shipmentId: String, status: String): String   │
│ + markAsShipped(shipmentId: String): String                          │
│ + markAsDelivered(shipmentId: String): String                        │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 15. SHIPMENT STATUS (Enum)

```
┌─────────────────────────────────────────────────────────────────────┐
│                           «enumeration»                              │
│                         ShipmentStatus                               │
├─────────────────────────────────────────────────────────────────────┤
│ PENDING                                                              │
│ PROCESSING                                                           │
│ SHIPPED                                                              │
│ IN_TRANSIT                                                           │
│ DELIVERED                                                            │
│ FAILED                                                               │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 16. DTO/Helper Classes (Penting untuk Logic)

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CartWithItems                               │
├─────────────────────────────────────────────────────────────────────┤
│ - cart: Cart                                                         │
│ - items: List<CartItem>                                              │
│ - totalAmount: BigDecimal                                            │
│ - totalItems: int                                                    │
├─────────────────────────────────────────────────────────────────────┤
│ + getSubtotal(): BigDecimal                                          │
│ + isEmpty(): boolean                                                 │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                          OrderSummary                                │
├─────────────────────────────────────────────────────────────────────┤
│ - items: List<CartItem>                                              │
│ - subtotal: BigDecimal                                               │
│ - shippingCost: BigDecimal                                           │
│ - totalAmount: BigDecimal                                            │
│ - courierName: String                                                │
│ - address: Address                                                   │
└─────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────┐
│                          OrderDetails                                │
├─────────────────────────────────────────────────────────────────────┤
│ - order: Order                                                       │
│ - orderItems: List<OrderItem>                                        │
│ - payment: Payment                                                   │
│ - shipment: Shipment                                                 │
│ - address: Address                                                   │
│ - user: User                                                         │
└─────────────────────────────────────────────────────────────────────┘
```

---
