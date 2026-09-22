package com.back.shared.market.out;

import com.back.shared.market.dto.OrderItemDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class MarketApiClient {
	private final RestClient restClient;

	public MarketApiClient(@Value("${custom.global.internalBackUrl}") String internalBackUrl) {
		restClient = RestClient.builder()
			.baseUrl(internalBackUrl + "/api/v1/market")
			.build();
	}

	public List<OrderItemDto> getOrderItems(int orderId) {
		return restClient.get()
			.uri("/orders/%d/items".formatted(orderId))
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});
	}
}
