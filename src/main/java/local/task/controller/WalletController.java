package local.task.controller;

import local.task.dto.WalletRequest;
import local.task.exceptions.NotEnoughBalanceException;
import local.task.exceptions.WalletHasNotFoundException;
import local.task.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("api/v1")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/wallet")
    public ResponseEntity<String> operation(@RequestBody WalletRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Amount can't be negative!");
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Amount can't be zero!");
        }

        try {
            return switch (request.getOperationType()) {
                case "DEPOSIT" -> {
                    walletService.deposit(request.getWalletId(), request.getAmount());
                    yield ResponseEntity.ok("The operation " + request.getOperationType() + " is successful!");
                }
                case "WITHDRAW" -> {
                    walletService.withdraw(request.getWalletId(), request.getAmount());
                    yield ResponseEntity.ok("The operation " + request.getOperationType() + " is successful!");
                }
                default -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect operation type!");
            };
        } catch (NotEnoughBalanceException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (WalletHasNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<?> getCurrentBalance(@PathVariable UUID walletId) {
        try {
            return ResponseEntity.ok("Your current balance: " + walletService.getCurrentBalance(walletId));
        } catch (WalletHasNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request body!");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid UUID format!");
    }
}