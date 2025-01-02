package hello.jdbc.repository.ex;

public class MyDuplicateKeyException extends MyDBException { // 데이터베이스 관련 예외를 계층화 하기 위해 extends


    public MyDuplicateKeyException() {
    }

    public MyDuplicateKeyException(String message) {
        super(message);
    }

    public MyDuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDuplicateKeyException(Throwable cause) {
        super(cause);
    }
}
