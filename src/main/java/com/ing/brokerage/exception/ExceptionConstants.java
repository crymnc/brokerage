package com.ing.brokerage.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionConstants {

    public static final String INSUFFICIENT_ASSET = "B100";

    public static final String NO_ASSET_TO_UNLOCK = "B101";

    public static final String USERNAME_ALREADY_EXISTS = "B102";

    public static final String CUSTOMER_NOT_FOUND = "B103";

    public static final String TRY_ORDER_PRICE_MUST_BE_ONE = "B104";

    public static final String ORDER_NOT_FOUND = "B105";

    public static final String ORDER_STATUS_NOT_PENDING = "B106";
}
