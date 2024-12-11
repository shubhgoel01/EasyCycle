package com.example.easycycle.data.model

import com.example.easycycle.data.Enum.CouponStatus
import com.example.easycycle.data.Enum.CouponType

//For future updates

data class Coupon(
    var couponId: String = "",                // Unique ID for the coupon
    var description: String = "",             // Short description of the coupon
    var discountPercentage: Int = 0,          // Discount in percentage (e.g., 10 for 10%)
    var maxDiscountAmount: Double = 0.0,      // Maximum discount amount allowed
    var validityStart: Long = 0L,             // Epoch timestamp for start of validity
    var validityEnd: Long = 0L,               // Epoch timestamp for end of validity
    var totalUsesAllowed: Int = 0,            // Total times this coupon can be used
    var usedCount: Int = 0,                   // Number of times this coupon has been used
    var applicableToAllUsers: Boolean = true, // If true, all users can use this coupon
    var userSpecificIds: List<String>? = null, // List of user IDs if coupon is user-specific
    var status: CouponStatus = CouponStatus.ACTIVE, // Status of the coupon (active, expired, etc.)
    var createdByAdminId: String = "",        // Admin ID who created this coupon
    var couponType: CouponType = CouponType.GENERAL // Type of coupon (General/User-Specific)
)



data class AppliedCoupon(
    var userId: String = "",                  // ID of the user who applied the coupon
    var couponId: String = "",                // ID of the applied coupon
    var appliedOn: Long = 0L,                 // Timestamp when the coupon was applied
    var claimedDiscount: Double = 0.0         // Discount amount applied during use
)



//active coupons in realtime database
//all coupons in storage - expired or claimed
//All applied coupons
