package com.example.easycycle.data.model

data class Payment(
    var paymentUId: String = "",
    var scheduleId: String = "",
    var userId: String = "",
    var status: PaymentStatus = PaymentStatus.COMPLETED, // Enum for "completed", "cancelled"
    var amount: Double = 0.0,
    var timeStamp: Long = 0L,
    var transactionDetails: String = "" // Description of payment method
)

enum class PaymentStatus {
    COMPLETED,
    CANCELLED
}