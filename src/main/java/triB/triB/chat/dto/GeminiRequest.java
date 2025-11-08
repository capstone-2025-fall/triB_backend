package triB.triB.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class GeminiRequest {

    // 프롬프트
    @JsonProperty("system_instruction")
    private SystemInstruction systemInstruction;

    // 사용자 질문/데이터
    private List<Content> contents;

    @Getter @Setter @Builder
    public static class SystemInstruction {
        private List<Part> parts;
    }

    @Getter @Setter @Builder
    public static class Content {
        private List<Part> parts;
    }

    @Getter @Setter @Builder
    public static class Part {
        private String text;
    }

    public static Part ofText(String text) {
        return Part.builder().text(text).build();
    }

}
