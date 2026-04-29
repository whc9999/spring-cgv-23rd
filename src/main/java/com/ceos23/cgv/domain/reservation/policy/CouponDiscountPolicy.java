package com.ceos23.cgv.domain.reservation.policy;

import com.ceos23.cgv.domain.reservation.enums.Coupon;

public final class CouponDiscountPolicy {

    private CouponDiscountPolicy() {
    }

    public static int apply(int currentPrice, String couponCode) {
        if (!hasCoupon(couponCode)) {
            return currentPrice;
        }

        return Coupon.from(couponCode).applyDiscount(currentPrice);
    }

    private static boolean hasCoupon(String couponCode) {
        return couponCode != null && !couponCode.isBlank();
    }
}
