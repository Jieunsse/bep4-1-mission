
package com.back.shared.post.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.back.standard.HasModelTypeCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor(onConstructor_ = @JsonCreator(mode = JsonCreator.Mode.PROPERTIES))
@Getter
public class PostDto implements HasModelTypeCode {
	private final int id;
	private final LocalDateTime createDate;
	private final LocalDateTime modifyDate;
	private final int authorId;
	private final String authorName;
	private final String title;
	private final String content;

	@Override
	public String getModelTypeCode() {
		return "Post";
	}

}
