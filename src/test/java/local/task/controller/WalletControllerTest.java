package local.task.controller;

import local.task.WalletProjectApplication;
import local.task.dto.WalletRequest;
import local.task.exceptions.NotEnoughBalanceException;
import local.task.exceptions.WalletHasNotFoundException;
import local.task.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {WalletProjectApplication.class, WalletController.class})
public class WalletControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(walletController).build();
    }

    @Test
    public void testDepositSuccess() throws Exception {
        UUID walletId = UUID.randomUUID();
        String operationType = "DEPOSIT";
        WalletRequest request = new WalletRequest(walletId, operationType, BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletId\":\"" + walletId + "\",\"operationType\":\"" + operationType + "\",\"amount\":100}"))
                .andExpect(status().isOk())
                .andExpect(content().string("The operation DEPOSIT is successful!"));
    }

    @Test
    public void testWithdrawSuccess() throws Exception {
        UUID walletId = UUID.randomUUID();
        String operationType = "WITHDRAW";
        WalletRequest request = new WalletRequest(walletId, operationType, BigDecimal.valueOf(50));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletId\":\"" + walletId + "\",\"operationType\":\"" + operationType + "\",\"amount\":50}"))
                .andExpect(status().isOk())
                .andExpect(content().string("The operation WITHDRAW is successful!"));
    }

    @Test
    public void testWithdrawNotEnoughBalance() throws Exception {
        UUID walletId = UUID.randomUUID();
        String operationType = "WITHDRAW";
        WalletRequest request = new WalletRequest(walletId, operationType, BigDecimal.valueOf(1000));

        doThrow(new NotEnoughBalanceException("Not enough balance for this operation!"))
                .when(walletService).withdraw(walletId, BigDecimal.valueOf(1000));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletId\":\"" + walletId + "\",\"operationType\":\"" + operationType + "\",\"amount\":1000}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Not enough balance for this operation!"));
    }

    @Test
    public void testInvalidOperationType() throws Exception {
        UUID walletId = UUID.randomUUID();
        String operationType = "INVALID";
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletId\":\"" + walletId + "\",\"operationType\":\"" + operationType + "\",\"amount\":100}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Incorrect operation type!"));
    }

    @Test
    public void testWalletNotFound() throws Exception {
        UUID walletId = UUID.randomUUID();

        when(walletService.getCurrentBalance(walletId))
                .thenThrow(new WalletHasNotFoundException("Wallet hasn't found!"));

        mockMvc.perform(get("/api/v1/wallets/" + walletId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Wallet hasn't found!"));
    }

    @Test
    public void testNegativeAmount() throws Exception {
        UUID walletId = UUID.randomUUID();
        String operationType = "DEPOSIT";
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletId\":\"" + walletId + "\",\"operationType\":\"" + operationType + "\",\"amount\":-100}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Amount can't be negative!"));
    }

    @Test
    public void testZeroAmount() throws Exception {
        UUID walletId = UUID.randomUUID();
        String operationType = "DEPOSIT";
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletId\":\"" + walletId + "\",\"operationType\":\"" + operationType + "\",\"amount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Amount can't be zero!"));
    }

    @Test
    public void testMissingRequestBody() throws Exception {
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInvalidWalletId() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testWalletNotFoundForOperation() throws Exception {
        UUID walletId = UUID.randomUUID();
        String operationType = "DEPOSIT";
        doThrow(new WalletHasNotFoundException("Wallet hasn't found!"))
                .when(walletService).deposit(walletId, BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletId\":\"" + walletId + "\",\"operationType\":\"" + operationType + "\",\"amount\":100}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Wallet hasn't found!"));
    }

    @Test
    public void testInvalidJson() throws Exception {
        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid-json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testInvalidHttpMethod() throws Exception {
        mockMvc.perform(get("/api/v1/wallet"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testGetCurrentBalanceSuccess() throws Exception {
        UUID walletId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.valueOf(500);

        when(walletService.getCurrentBalance(walletId)).thenReturn(balance);

        mockMvc.perform(get("/api/v1/wallets/" + walletId))
                .andExpect(status().isOk())
                .andExpect(content().string("Your current balance: " + balance));
    }
}