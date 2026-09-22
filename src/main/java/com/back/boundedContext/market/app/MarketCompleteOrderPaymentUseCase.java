package com.back.boundedContext.market.app;

import com.back.boundedContext.market.domain.Order;
import com.back.boundedContext.market.out.OrderRepository;
import com.back.shared.market.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarketCompleteOrderPaymentUseCase {
	private final OrderRepository orderRepository;

	public void completeOrderPayment(OrderDto orderDto) {
		Order order = orderRepository.findById(orderDto.getId()).get();
		order.completePayment();
	}
}
