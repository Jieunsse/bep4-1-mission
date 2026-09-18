package com.back.boundedContext.member.app;

import com.back.boundedContext.member.domain.Member;
import com.back.boundedContext.member.out.MemberRepository;
import com.back.global.exception.DomainException;
import com.back.global.rsData.RsData;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberJoinUseCase {
	private final MemberRepository memberRepository;

	public RsData<Member> join(String username, String password, String nickname) {
		memberRepository.findByUsername(username).ifPresent(m -> {
			throw new DomainException("409-1", "이미 존재하는 username 입니다.");
		});

		return memberRepository.save(new Member(username, password, nickname));
	}
}