package local.task.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "wallets")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.NONE)
    @Getter
    @Column(name = "id", nullable = false)
    private UUID walletId;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    public void setBalance(BigDecimal amount) {
        balance = amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
