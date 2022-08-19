package com.techelevator.tenmo.dao;

import java.math.BigDecimal;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.ShareableUser;
import com.techelevator.tenmo.model.Transfer;

public interface AccountDAO {

    Account getAccountByAccountId(long account_id);

    Account getAccountByUserId(long user_id);

    BigDecimal getAccountBalanceByAccountId(long account_id);

    Account updateAccountBalanceByAccountId(BigDecimal balance, long account_id);

    // Method to get list TE individuals that the user can select for a transaction
    ShareableUser[] findUsersForTransfer(long user_id);

    // Method to get a transaction from the transfer ID
    Transfer getTransferByTransferId(long transferId);

    // Method to create a transfer record
    Transfer createTransfer(Transfer transfer);

    // Method to select all approved transactions to or from for a user
    Transfer[] getTransfersByAccountId(long accountId, long transferStatusId);

    // Method to find user from the account id
    ShareableUser getUserIdByAccountID(long accountId);

}