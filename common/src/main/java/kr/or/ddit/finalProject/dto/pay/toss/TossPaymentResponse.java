package kr.or.ddit.finalProject.dto.pay.toss;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossPaymentResponse implements Serializable {

    private String mId;
    private String lastTransactionKey;
    private String paymentKey;
    private String orderId;
    private String orderName;
    private int taxExemptionAmount;
    private String status;
    private String requestedAt;
    private String approvedAt;
    private boolean useEscrow;
    private boolean cultureExpense;

    private CardInfo card;
    private VirtualAccountInfo virtualAccount;
    private TransferInfo transfer;
    private MobilePhoneInfo mobilePhone;
    private GiftCertificateInfo giftCertificate;
    private CashReceiptInfo cashReceipt;
    private List<CashReceiptInfo> cashReceipts;
    private DiscountInfo discount;
    private List<CancelInfo> cancels;

    private String secret;
    private String type;
    private EasyPayInfo easyPay;
    private String country;
    private FailureInfo failure;
    private boolean isPartialCancelable;
    private ReceiptInfo receipt;
    private CheckoutInfo checkout;
    private String currency;
    private long totalAmount;
    private long balanceAmount;
    private long suppliedAmount;
    private long vat;
    private long taxFreeAmount;
    private String method;
    private String version;
    private Object metadata;

    // ---------- 중첩 DTO ----------

    @Getter
    @NoArgsConstructor
    public static class EasyPayInfo {
        private String provider;
        private long amount;
        private long discountAmount;
    }

    @Getter
    @NoArgsConstructor
    public static class ReceiptInfo {
        private String url;
    }

    @Getter
    @NoArgsConstructor
    public static class CheckoutInfo {
        private String url;
    }

    @Getter
    @NoArgsConstructor
    public static class CancelInfo implements Serializable {
        private String cancelAmount;
        private String cancelReason;
        private String canceledAt;
        private String transactionKey;
    }

    @Getter
    @NoArgsConstructor
    public static class FailureInfo implements Serializable {
        private String code;
        private String message;
    }

    @Getter
    @NoArgsConstructor
    public static class CardInfo implements Serializable {
        private String issuerCode;
        private String acquirerCode;
        private String number;
        private int installmentPlanMonths;
        private boolean isInterestFree;
        private String approveNo;
        private String cardType;
        private String ownerType;
    }

    @Getter
    @NoArgsConstructor
    public static class VirtualAccountInfo implements Serializable {
        private String accountType;
        private String accountNumber;
        private String bankCode;
        private String customerName;
        private String dueDate;
        private String refundStatus;
    }

    @Getter
    @NoArgsConstructor
    public static class TransferInfo implements Serializable {
        private String bankCode;
        private boolean settlementStatus;
    }

    @Getter
    @NoArgsConstructor
    public static class MobilePhoneInfo implements Serializable {
        private String customerMobilePhone;
        private String settlementStatus;
        private String receiptUrl;
    }

    @Getter
    @NoArgsConstructor
    public static class GiftCertificateInfo implements Serializable {
        private String approveNo;
        private String settlementStatus;
    }

    @Getter
    @NoArgsConstructor
    public static class CashReceiptInfo implements Serializable {
        private String type;
        private String receiptKey;
        private String issueNumber;
        private String receiptUrl;
        private long amount;
        private long taxFreeAmount;
    }

    @Getter
    @NoArgsConstructor
    public static class DiscountInfo implements Serializable {
        private long amount;
    }
}
