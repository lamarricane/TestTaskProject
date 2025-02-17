package local.task.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WalletHasNotFoundException extends Exception {
    public WalletHasNotFoundException(String message) {
        super(message);
    }
}
