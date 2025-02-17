package local.task.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class WalletRequest {
    @Setter(AccessLevel.NONE)
    private UUID walletId;
    private String operationType;
    private BigDecimal amount;

    public String getOperationType() {
        return operationType;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
