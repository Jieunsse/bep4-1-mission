package com.back.boundedContext.payout.app;

import com.back.boundedContext.payout.domain.Payout;
import com.back.boundedContext.payout.domain.PayoutMember;
import com.back.boundedContext.payout.out.PayoutMemberRepository;
import com.back.boundedContext.payout.out.PayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PayoutCreatePayoutUseCase {
	private final PayoutRepository payoutRepository;
	private final PayoutMemberRepository payoutMemberRepository;

	public Payout createPayout(int payeeId) {
		PayoutMember payoutMember = payoutMemberRepository.getReferenceById(payeeId);
		return payoutRepository.save(new Payout(payoutMember));
	}
}
