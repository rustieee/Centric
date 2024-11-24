package com.finals.centric;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BackblazeB2Auth {

    private static final String ACCOUNT_ID = "blackblazeStorage";  // Replace with your Backblaze B2 Account ID
    private static final String APPLICATION_KEY = "005a32e6b83ad0d0000000001";  // Replace with your Application Key
    private static final String B2_API_URL = "https://api.backblazeb2.com/b2api/v2";  // Backblaze B2 API URL

    private static String authorizationToken;

    // Method to get B2 Authorization Token
    public static void getAuthorizationToken(final AuthorizationCallback callback) {
        OkHttpClient client = new OkHttpClient();

        // API endpoint for Backblaze B2 authorization
        String authUrl = B2_API_URL + "/b2_authorize_account";

        // Make an HTTP request to authorize and get the token
        Request request = new Request.Builder()
                .url(authUrl)
                .header("Authorization", "Basic " + okhttp3.Credentials.basic(ACCOUNT_ID, APPLICATION_KEY))
                .build();

        // Send the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("BackblazeB2Auth", "Authentication failed", e);
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Parse the response to extract the authorization token
                    String responseBody = response.body().string();
                    authorizationToken = parseTokenFromResponse(responseBody);
                    callback.onSuccess(authorizationToken);
                } else {
                    Log.e("BackblazeB2Auth", "Failed to authenticate: " + response.message());
                    callback.onFailure(response.message());
                }
            }
        });
    }

    // Parse the token from the API response JSON
    private static String parseTokenFromResponse(String response) {
        // You can use any JSON parsing library here (like Gson or Jackson) to parse the response
        // Assuming the response is in JSON format like: {"authorizationToken":"your-token", ...}
        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString("authorizationToken");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Callback interface to handle the result of the authentication
    public interface AuthorizationCallback {
        void onSuccess(String token);
        void onFailure(String error);
    }
}
