package com.findteam.findteam.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostPageResponse {

	private List<PostResponse> content;
	/** Zero-based page index. */
	private int page;
	/** Page size used for this response (after server-side cap). */
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean last;
}
