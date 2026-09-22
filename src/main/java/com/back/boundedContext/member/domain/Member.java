
package com.back.boundedContext.member.domain;



import com.back.global.jpa.entity.BaseIdAndTime;
import com.back.shared.member.dto.MemberDto;
import com.back.shared.member.event.MemberModifiedEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MEMBER_MEMBER")
@NoArgsConstructor
@Getter
public class Member extends BaseIdAndTime {
	@Column(unique = true)
	private String username;
	private String password;
	private String nickname;
	private int activityScore;

	public Member(String username, String password, String nickname) {
		this.username = username;
		this.password = password;
		this.nickname = nickname;
	}

	public int increaseActivityScore(int amount) {
		if (amount == 0) return getActivityScore();

		activityScore += amount;
		publishEvent(new MemberModifiedEvent(toDto()));

		return activityScore;
	}

	public MemberDto toDto() {
		return new MemberDto(getId(), getCreateDate(), getModifyDate(), username, nickname, activityScore);
	}
}
