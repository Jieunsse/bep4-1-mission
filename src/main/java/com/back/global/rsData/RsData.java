package com.back.global.rsData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.back.standard.ResultType;

@AllArgsConstructor
@Getter
public class RsData<T> implements ResultType {
	private final String resultCode;
	private final String msg;
	private final T data;

	public RsData(String resultCode, String msg) {
		this(resultCode, msg, null);
	}
}
