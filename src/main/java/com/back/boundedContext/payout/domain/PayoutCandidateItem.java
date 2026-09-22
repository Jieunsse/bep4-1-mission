package com.back.boundedContext.payout.domain;

import com.back.global.jpa.entity.BaseIdAndTime;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name = "PAYOUT_PAYOUT_CANDIDATE_ITEM")
@NoArgsConstructor
@Getter
public class PayoutCandidateItem extends BaseIdAndTime {
	@Enumerated(EnumType.STRING)
	private PayoutEventType eventType;
	private String relTypeCode;
	private int relId;
	private LocalDateTime paymentDate;
	@ManyToOne(fetch = LAZY)
	private PayoutMember payer;
	@ManyToOne(fetch = LAZY)
	private PayoutMember payee;
	private long amount;
	@OneToOne(fetch = LAZY)
	@Setter
	private PayoutItem payoutItem;

	public PayoutCandidateItem(PayoutEventType eventType, String relTypeCode, int relId, LocalDateTime paymentDate, PayoutMember payer, PayoutMember payee, long amount) {
		this.eventType = eventType;
		this.relTypeCode = relTypeCode;
		this.relId = relId;
		this.paymentDate = paymentDate;
		this.payer = payer;
		this.payee = payee;
		this.amount = amount;
	}
}
