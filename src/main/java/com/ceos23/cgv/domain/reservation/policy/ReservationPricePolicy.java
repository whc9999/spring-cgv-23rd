package com.ceos23.cgv.domain.reservation.policy;

import com.ceos23.cgv.domain.movie.entity.Screening;

public final class ReservationPricePolicy {

    private static final int MORNING_DISCOUNT = 4000;

    private ReservationPricePolicy() {
    }

    public static int calculate(Screening screening, int peopleCount, String couponCode) {
        int calculatedPrice = calculateTicketPrice(screening) * peopleCount;
        return CouponDiscountPolicy.apply(calculatedPrice, couponCode);
    }

    private static int calculateTicketPrice(Screening screening) {
        int ticketPrice = screening.getTheater().getType().getBasePrice();

        if (Boolean.TRUE.equals(screening.getIsMorning())) {
            return ticketPrice - MORNING_DISCOUNT;
        }

        return ticketPrice;
    }
}
