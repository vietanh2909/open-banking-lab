package com.navi.ob.ewlts.utils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class IdGen {
    private IdGen() {}
    public static String paymentId() {
        return "EWC" + UUID.randomUUID().toString().replace("-", ""); // 35 chars
    }
    public static String otp6() {
        int v = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return String.format("%06d", v);
    }
}