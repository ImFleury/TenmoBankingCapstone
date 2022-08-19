package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.TreeMap;

public class TransferService {

    // Instance variables
    private final String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;

    // Constructor
    public TransferService(String url) {
        this.baseUrl = url;
    }

    // Set the authentication token
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    // Get list of potential users for a transfer
    public User[] findUsersForTransfer(long userId) {
        User[] users = null;
        try {
            ResponseEntity<User[]> response;
            response = restTemplate.exchange(this.baseUrl + "users/" + userId + "/potentialTransferUsers",
                    HttpMethod.GET, makeAuthEntity(), User[].class);
            users = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return users;
    }

    // Get transfer details from transfer ID
    public Transfer getTransferByTransferId(long transferId) {
        Transfer transfer = null;
        try {
            ResponseEntity<Transfer> response = restTemplate.exchange(this.baseUrl + "transfers/" + transferId,
                    HttpMethod.GET,
                    makeAuthEntity(),
                    Transfer.class);
            transfer = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfer;
    }

    // Create a transfer record
    public Transfer createTransfer(Transfer newTransfer) {
        Transfer returnedTransfer = null;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Transfer> entity = new HttpEntity<>(newTransfer, headers);

        try {
            returnedTransfer = restTemplate.postForObject(this.baseUrl + "transfers", entity, Transfer.class);
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + ": " +e.getStatusText());
        } catch (ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return returnedTransfer;
    }

    // Get transactions by account ID and transfer status type
    public Transfer[] getTransfersByAccountId(long accountId, long transferStatusId) {
        Transfer[] transfers = null;
        try {
            ResponseEntity<Transfer[]> response =
                    restTemplate.exchange(this.baseUrl + "accounts/" + accountId
                                    + "/transfers?transferStatusType=" + transferStatusId,
                            HttpMethod.GET, makeAuthEntity(), Transfer[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    public User getUserIdByAccountID(long accountId) {
        User users = null;
        try {
            ResponseEntity<User> response =
                    restTemplate.exchange(this.baseUrl + "accounts/" + accountId + "/user", HttpMethod.GET, makeAuthEntity(), User.class);
            users = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return users;
}

    // Create the authentication entity using the token
    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

}
