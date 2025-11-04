package triB.triB.chat.dto;

public enum ActionType {
    NEW_MESSAGE,      // 새 메시지
    NEW_MAP_MESSAGE, //새 장소공유
    BOOKMARK_UPDATE,  // 북마크 설정/해제
    TAG_UPDATE,       // 태그 설정
    MESSAGE_EDIT,     // 메시지 수정
    MESSAGE_DELETE    // 메시지 삭제
}
