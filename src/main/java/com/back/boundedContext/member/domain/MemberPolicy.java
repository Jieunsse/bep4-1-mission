
package com.back.boundedContext.member.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class MemberPolicy {
	private static int passwordChangeDays;

	@Value("${custom.member.password.changeDays}")
	public void setPasswordChangeDays(int passwordChangeDays) {
		MemberPolicy.passwordChangeDays = passwordChangeDays;
	}

	public Duration getNeedToChangePasswordPeriod() {
		return Duration.ofDays(passwordChangeDays);
	}

	public int getNeedToChangePasswordDays() {
		return passwordChangeDays;
	}

	public boolean isNeedToChangePassword(LocalDateTime lastChangeDate) {
		if (lastChangeDate == null) return true;

		return lastChangeDate.plusDays(passwordChangeDays)
			.isBefore(LocalDateTime.now());
	}
}
