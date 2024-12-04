package io.sourcesync.android;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ApiClient {
    private static final String TAG = "SourceSync.ApiClient";
    private static final String BASE_URL = "https://platform.sourcesync.io";
    private static final int CONNECT_TIMEOUT = 5000; // 5 seconds
    private static final int READ_TIMEOUT = 5000;    // 5 seconds
    private static final int DEFAULT_TIMEOUT = 5;    // 5 seconds for AsyncTask timeout

    private final String apiKey;

    public ApiClient(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Makes an async GET request to the API and returns the response as a JSONObject
     */
    private JSONObject get(String endpoint) throws IOException, JSONException {
        try {
            ApiResponse response = new ApiTask(endpoint).execute()
                .get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            if (response.error != null) {
                throw new IOException("API request failed", response.error);
            }
            return response.data;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new IOException("API request failed: " + e.getMessage(), e);
        }
    }

    private class ApiTask extends AsyncTask<Void, Void, ApiResponse> {
        private final String endpoint;

        ApiTask(String endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        protected ApiResponse doInBackground(Void... params) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(BASE_URL + endpoint);
                Log.d(TAG, "Making GET request to: " + url);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-API-Key", apiKey);
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("User-Agent", "SourceSync-SDK-Android");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String response = readResponse(conn);
                    Log.d(TAG, "Received response: " + response);
                    return new ApiResponse(new JSONObject(response), null);
                } else {
                    String errorResponse = readErrorResponse(conn);
                    Log.e(TAG, "API error response: " + errorResponse);
                    return new ApiResponse(null,
                        new IOException("API request failed with code " + responseCode + ": " + errorResponse));
                }
            } catch (Exception e) {
                Log.e(TAG, "API request failed", e);
                return new ApiResponse(null, e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }

    private static class ApiResponse {
        final JSONObject data;
        final Exception error;

        ApiResponse(JSONObject data, Exception error) {
            this.data = data;
            this.error = error;
        }
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private String readErrorResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    /**
     * Gets SDK settings from the API
     */
    public JSONObject getSettings() throws IOException, JSONException {
        JSONObject response = get("/key");
        // Extract sdk settings from response structure
        if (response.has("settings") && response.getJSONObject("settings").has("sdk")) {
            return response.getJSONObject("settings").getJSONObject("sdk");
        }
        return new JSONObject();
    }

    /**
     * Gets a distribution by ID
     */
    public JSONObject getDistribution(String id) throws IOException, JSONException {
        return get("/distributions/" + id);
    }

    /**
     * Gets an activation by ID
     */
    public JSONObject getActivation(String id) throws IOException, JSONException {
        JSONObject response = get("/activations/" + id);
        // Get first activation from data array
        if (response.has("data")) {
            JSONArray dataArray = response.getJSONArray("data");
            if (dataArray.length() > 0) {
                return dataArray.getJSONObject(0);
            }
        }
        throw new JSONException("No activation data found in response");
    }
}
