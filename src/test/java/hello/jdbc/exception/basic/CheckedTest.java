package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class CheckedTest {

    @Test
    void checked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void checked_trows() {
        Service service = new Service();

        // 예외를 마지막까지 던지는 경우를 테스트하기 위해 assertThatThrownBy() 사용
        Assertions.assertThatThrownBy(() -> service.callThrows()) // 메서드 호출시
                .isInstanceOf(MyCheckedException.class); // 지정한 예외 발생 -> 테스트 성공
    }


    /**
        * Exception을 상속받은 예외는 체크예외가 된다.
     */
    static class MyCheckedException extends Exception {
        public MyCheckedException(String message) {
            super(message);
        }

    }

    /**
     * Checked 예외는
     * catch로 잡아서 해결 or throws로 던지기  중 택1
     */
    static class Service {
        Repository repository = new Repository();

        /**
            * 예외를 잡아서 처리하는 메서드
         */
        public void callCatch() {
            try {
                repository.call(); // repository 호출하며 예외(** checked) 발생 -> throws or catch
            } catch (MyCheckedException e) { // catch 로 예외 처리
                // 예외 처리 로직
                log.info("예외처리 메시지={}", e.getMessage(), e); // exception 출력은 파라미터 바인딩 할 필요 X
            }
        }

        /**
         * 예외를 던지는 메서드
         */
        public void callThrows() throws MyCheckedException {
            repository.call();
        }
    }

    static class Repository {
        public void call() throws MyCheckedException { // check 예외는 throws로 던져줘야 컴파일러가 체크함
            throw new MyCheckedException("ex");
        }

    }
}
