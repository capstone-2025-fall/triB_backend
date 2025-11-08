package triB.triB.community.exception;

import triB.triB.global.exception.CustomException;
import triB.triB.global.exception.ErrorCode;

public class PostNotFoundException extends CustomException {

    public PostNotFoundException() {
        super(ErrorCode.POST_NOT_FOUND);
    }
}
