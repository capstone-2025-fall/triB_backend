package triB.triB.community.exception;

import triB.triB.global.exception.CustomException;
import triB.triB.global.exception.ErrorCode;

public class CommentNotFoundException extends CustomException {

    public CommentNotFoundException() {
        super(ErrorCode.COMMENT_NOT_FOUND);
    }
}
