package onemfive;

/**
 * General Exception within this Context.
 * TODO: provide automatic logging
 */
public class OneMFiveException extends Exception {

    public OneMFiveException() {
    }

    public OneMFiveException(String message) {
        super(message);
    }

    public OneMFiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public OneMFiveException(Throwable cause) {
        super(cause);
    }
}
