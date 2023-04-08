package cn.leftsite.sqltoentity.exception;

public class ExecuteException extends RuntimeException {
    public ExecuteException(String message) {
        super(message);
    }

    public ExecuteException(Exception e) {
        super(e);
    }

    public ExecuteException() {
    }
}
