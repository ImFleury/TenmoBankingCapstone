package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Transfer {

    private long transferId;
    private long transferTypeId;
    private long transferStatusID;
    private long accountFrom;
    private long accountTo;
    private BigDecimal amount;

    public long getTransferId() {
        return transferId;
    }

    public void setTransferId(long transferId) {
        this.transferId = transferId;
    }

    public long getTransferTypeId() {
        return transferTypeId;
    }

    public void setTransferTypeId(long transferTypeId) {
        this.transferTypeId = transferTypeId;
    }

    public long getTransferStatusID() {
        return transferStatusID;
    }

    public void setTransferStatusID(long transferStatusID) {
        this.transferStatusID = transferStatusID;
    }

    public long getAccountFrom() {
        return accountFrom;
    }

    public void setAccountFrom(long accountFrom) {
        this.accountFrom = accountFrom;
    }

    public long getAccountTo() {
        return accountTo;
    }

    public void setAccountTo(long accountTo) {
        this.accountTo = accountTo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
