package com.ceos23.cgv.domain.reservation.enums;

import com.ceos23.cgv.global.exception.CustomException;
import com.ceos23.cgv.global.exception.ErrorCode;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

public enum Coupon {

    WELCOME_CGV(price -> price - 3000),
    VIP_HALF_PRICE(price -> price - price / 2);

    private final IntUnaryOperator discountPolicy;

    Coupon(IntUnaryOperator discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

    public static Coupon from(String couponCode) {
        return Arrays.stream(values())
                .filter(coupon -> coupon.name().equals(couponCode))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_COUPON_CODE));
    }

    public int applyDiscount(int price) {
        return Math.max(discountPolicy.applyAsInt(price), 0);
    }
}
