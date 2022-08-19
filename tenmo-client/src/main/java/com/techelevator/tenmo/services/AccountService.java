package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.util.BasicLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class AccountService {

    // Instance variables
    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;

    // Constructor
    public AccountService(String url) {
        this.baseUrl = url;
    }

    // Set the authentication token
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    // Get account using the account ID
    public Account getAccountByAccountId(long accountId) {
        Account account = null;
        try {
            ResponseEntity<Account> response =
                    restTemplate.exchange(this.baseUrl + "accounts/" + accountId, HttpMethod.GET,
                            makeAuthEntity(), Account.class);
            account = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return account;
    }

    // Get account using the user ID
    public Account getAccountByUserId(long userId) {
        Account account = null;
        try {
            ResponseEntity<Account> response =
                    restTemplate.exchange(this.baseUrl + "users/" + userId + "/account", HttpMethod.GET,
                            makeAuthEntity(), Account.class);
            account = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return account;
    }

    // Get balance using the account ID
    public BigDecimal getBalanceByAccountId(long accountId) {
        BigDecimal balance = null;
        try {
            ResponseEntity<BigDecimal> response =
                    restTemplate.exchange(this.baseUrl + "accounts/" + accountId + "/balance", HttpMethod.GET,
                            makeAuthEntity(), BigDecimal.class);
            balance = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return balance;
    }

    // Update account using the account ID
    public Account updateAccountBalanceByAccountId(BigDecimal amount, long accountId) {
        Account account = null;
        try {
            ResponseEntity<Account> response =
                    restTemplate.exchange(this.baseUrl + "accounts/" + accountId + "/balance?newBalance=" + amount,
                            HttpMethod.PUT, makeAuthEntity(), Account.class);
            account = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return account;
    }

    // Create the authentication entity using the token
    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }
}
