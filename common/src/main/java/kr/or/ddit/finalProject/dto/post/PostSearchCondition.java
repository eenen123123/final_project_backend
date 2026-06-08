package kr.or.ddit.finalProject.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostSearchCondition {
    private String keyword;
    private String searchType; // sender, receiver, content
    private String readType; // read, unread, all
    private String box; // received, sent, self
    private PostTypeEnum postType; // PERSONAL, SYSTEM

}
