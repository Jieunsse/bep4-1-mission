package com.back.boundedContext.payout.app;

import com.back.boundedContext.payout.domain.PayoutCandidateItem;
import com.back.boundedContext.payout.domain.PayoutMember;
import com.back.boundedContext.payout.out.PayoutCandidateItemRepository;
import com.back.boundedContext.payout.out.PayoutMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayoutSupport {
	private final PayoutMemberRepository payoutMemberRepository;
	private final PayoutCandidateItemRepository payoutCandidateItemRepository;

	public Optional<PayoutMember> findSystemMember() {
		return payoutMemberRepository.findByUsername("system");
	}

	public Optional<PayoutMember> findMemberById(int id) {
		return payoutMemberRepository.findById(id);
	}

	public List<PayoutCandidateItem> findPayoutCandidateItems() {
		return payoutCandidateItemRepository.findAll();
	}
}
