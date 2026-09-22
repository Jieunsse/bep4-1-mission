package com.back.boundedContext.cash.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CashPolicy {
	public static int HOLDING_MEMBER_ID;

	@Value("${custom.global.holdingMemberId}")
	public void setHoldingMemberId(int holdingMemberId) {
		HOLDING_MEMBER_ID = holdingMemberId;
	}
}
