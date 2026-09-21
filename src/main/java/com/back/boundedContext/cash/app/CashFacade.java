package com.back.boundedContext.cash.app;

import com.back.boundedContext.cash.domain.CashMember;
import com.back.boundedContext.cash.domain.Wallet;
import com.back.boundedContext.cash.out.CashMemberRepository;
import com.back.boundedContext.cash.out.WalletRepository;
import com.back.global.eventPublisher.EventPublisher;
import com.back.shared.cash.dto.CashMemberDto;
import com.back.shared.cash.event.CashMemberCreatedEvent;
import com.back.shared.member.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CashFacade {
	private final CashMemberRepository cashMemberRepository;
	private final WalletRepository walletRepository;
	private final EventPublisher eventPublisher;

	@Transactional
	public CashMember syncMember(MemberDto member) {
		boolean isNew = !cashMemberRepository.existsById(member.getId());

		CashMember _member = cashMemberRepository.save(new CashMember(
			member.getId(),
			member.getCreateDate(),
			member.getModifyDate(),
			member.getUsername(),
			"",
			member.getNickname(),
			member.getActivityScore()
		));

		if (isNew)
			eventPublisher.publish(new CashMemberCreatedEvent(new CashMemberDto(_member)));

		return _member;
	}

	@Transactional
	public Wallet createWallet(CashMemberDto holder) {
		CashMember member = cashMemberRepository.getReferenceById(holder.getId());
		Wallet wallet = new Wallet(member);

		return walletRepository.save(wallet);
	}

	@Transactional(readOnly = true)
	public Optional<CashMember> findMemberByUsername(String username) {
		return cashMemberRepository.findByUsername(username);
	}

	@Transactional(readOnly = true)
	public Optional<Wallet> findWalletByHolder(CashMember holder) {
		return walletRepository.findByHolder(holder);
	}
}
