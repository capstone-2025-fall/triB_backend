package triB.triB.community.exception;

import triB.triB.global.exception.CustomException;
import triB.triB.global.exception.ErrorCode;

public class UnauthorizedPostAccessException extends CustomException {

    public UnauthorizedPostAccessException() {
        super(ErrorCode.UNAUTHORIZED_POST_ACCESS);
    }
}
