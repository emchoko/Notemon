package com.notemon.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.notemon.models.FirebaseToken;
import com.notemon.rest.RestMethods;

import static android.content.ContentValues.TAG;

/**
 * Created by emil on 01.05.17.
 */

public class FirebaseIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.

        FirebaseToken firebaseToken = new FirebaseToken();
        firebaseToken.setDeviceToken(token);

        RestMethods.addTokenToUser(this, firebaseToken);
    }
}
