package kr.or.ddit.finalProject.dto.message;

import java.io.Serializable;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMessageRoomRequestDto implements Serializable {

    @NotBlank
    private String roomName;


    @NotEmpty
    private List<String> participantIds;
}
