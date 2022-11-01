package d3e.core;

import org.springframework.beans.factory.annotation.Autowired;

import classes.VerificationDataByToken;
import lists.VerificationDataByTokenImpl;
import models.VerificationData;
import models.VerificationDataByTokenRequest;
import store.Database;

public abstract class VerificationControllerBase {
	private static final String SUCCESS = "<!DOCTYPE html><html><head><link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/mini.css/3.0.1/mini-default.min.css\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><title>Verifying...</title></head><body><div class=\"container\"><div class=\"row\"><h1>Verification Successful!</h1></div><div class=\"row\"><h1><small>You may close this page.</small></h1></div></div></body></html>";
	private static final String FAILED = "<!DOCTYPE html><html><head><link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/mini.css/3.0.1/mini-default.min.css\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><title>Verifying...</title></head><body><div class=\"container\"><div class=\"row\"><h1>That shouldn't have happened...</h1></div><div class=\"row\"><h1><small>Something went wrong while processing your verification. We are looking into it.</small></h1></div><div class=\"row\"><h1><small>Your link will remain active, so please retry after some time. You may close this page.</small></h1></div></div></body></html>";
	private static final String INVALID = "<!DOCTYPE html><html><head><link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/mini.css/3.0.1/mini-default.min.css\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><title>Verifying...</title></head><body><div class=\"container\"><div class=\"row\"><h1>You shouldn't be here</h1></div><div class=\"row\"><h1><small>This link is invalid. If you didn't already click on this link, please reach out to Customer Support.</small></h1></div><div class=\"row\"><h1><small>You may close this page.</small></h1></div></div></body></html>";

	@Autowired
	private VerificationDataByTokenImpl impl;

	protected String handleVerificationClick(String token) {
		try {
			if (token == null) {
				return INVALID;
			}

			// Get VerificationData object by token
			VerificationDataByTokenRequest req = new VerificationDataByTokenRequest();
			req.setToken(token);
			VerificationDataByToken dataByToken = impl.get(req);

			if (dataByToken == null || dataByToken.getItems().size() != 1) {
				return INVALID;
			}

			VerificationData data = dataByToken.getItems().get(0);
			if (data.isProcessed()) {
				// If link is already processed.
				return INVALID;
			}
			// Mark processed
			data.setProcessed(true);
			Database.get().save(data);
			
			// If validation success, call User's VerificationHandler method
			handleVerificationClick(data.getMethod(), data.getContext());
			return SUCCESS;
		} catch (Exception e) {
			e.printStackTrace();
			return FAILED;
		}
	}

	protected abstract void handleVerificationClick(String method, String context);
}
