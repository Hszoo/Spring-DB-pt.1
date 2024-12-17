package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class UnCheckedTest {

    @Test
    void unchecked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void unchecked_throws() {
        Service service = new Service();

        Assertions.assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyUnCheckedException.class);
    }

    /*
    * RunTeimException을 상속받은 예외는 Unchecked 예외가 된다.
    * */
    static class MyUnCheckedException extends RuntimeException {
        public MyUnCheckedException(String message) {
            super(message);
        }
    }

    /**
     * Unchecked 예외는 catch or (throws, throws 생략)
     */
    static class Service {
        Repository repository = new Repository();

        /**
         * Unchecked 예외를 잡아서 처리하는 메서드
         */
        public void callCatch() {
            try {
                repository.call();
            } catch (MyUnCheckedException e) {
                log.info("예외 처리 message={}", e.getMessage(), e);
            }
        }

        /**
         * Unchecked 예외 throws 키워드 생략하는 메서드
         */
        public void callThrow() {
            repository.call();
        }
    }

    static class Repository {
        public void call() { // 언체크 예외는 throws 생략 가능
            throw new MyUnCheckedException("ex");
        }
    }

}
