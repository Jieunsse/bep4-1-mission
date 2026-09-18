package com.back.boundedContext.member.in;

import com.back.boundedContext.member.app.MemberFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Slf4j
public class MemberDataInit {
	private final MemberDataInit self;
	private final MemberFacade memberFacade;

	public MemberDataInit(
		@Lazy MemberDataInit self,
		MemberFacade memberFacade
	) {
		this.self = self;
		this.memberFacade = memberFacade;
	}

	@Bean
	@Order(1)
	public ApplicationRunner memberDataInitApplicationRunner() {
		return args -> self.makeBaseMembers();
	}

	@Transactional
	public void makeBaseMembers() {
		if (memberFacade.count() > 0)
			return;

		memberFacade.join("system", "1234", "시스템");
		memberFacade.join("holding", "1234", "홀딩");
		memberFacade.join("admin", "1234", "관리자");
		memberFacade.join("user1", "1234", "유저1");
		memberFacade.join("user2", "1234", "유저2");
		memberFacade.join("user3", "1234", "유저3");
	}
}
