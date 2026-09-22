package com.back.boundedContext.payout.app;

import com.back.shared.market.dto.OrderDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PayoutAddPayoutCandidateItemsUseCase {
	public void addPayoutCandidateItems(OrderDto order) {
		log.debug("addPayoutCandidateItems.order: {}", order.getId());
	}
}
