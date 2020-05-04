package com.element.element_web_ui;

import android.util.Base64;
import android.util.Log;

import com.element.element_web_ui_lib.ElementApi;
import com.element.element_web_ui_lib.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import static java.net.HttpURLConnection.HTTP_OK;

public class ElementApiImplement implements ElementApi {
    private static final String TAG = ElementApiImplement.class.getSimpleName();
    private String clientId;
    private String apiKey;
    private String userId;
    private String serverUrl;

    private static ElementApiImplement instance;

    private ElementApiImplement() {}

    public static ElementApiImplement getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Please call init() first");
        }
        return instance;
    }

    public static void init(String clientId, String apiKey, String serverUrl) {
        if(instance == null) {
            instance = new ElementApiImplement();
        }
        instance.clientId = clientId;
        instance.apiKey = apiKey;
        instance.serverUrl = serverUrl;
    }

    private HttpResponse post(String url, String json) throws Exception {
        Log.d(TAG, "POST " + url);
        HttpResponse response = new HttpResponse();

        try {
            URL httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("CLIENTID", clientId);

            long timestamp = new Date().getTime();
            conn.setRequestProperty("TIMESTAMP", "" + timestamp);

            String payload = apiKey + timestamp + userId;
            String payload2 = apiKey + timestamp + userId + json.length();
            hash512(payload2);

            conn.setRequestProperty("HASHTOKEN", hash256(payload));

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes("UTF-8"));
            os.close();

            Long startTimestamp = System.currentTimeMillis();
            Log.d(TAG, "start post at: " + startTimestamp);
            if(conn.getResponseCode() != HTTP_OK &&
                    conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                response.responseCode = conn.getResponseCode();
                String jsonStr = readResponse(conn.getErrorStream());
                JsonObject jsonObject = JsonUtils.fromJson(jsonStr, JsonObject.class);
                response.error = jsonObject.get("message").getAsString();
                return response;
            }
            Long endTimestamp = System.currentTimeMillis();
            Log.d(TAG, "end post at: " + endTimestamp);
            Log.d(TAG, String.format("use %d seconds.",
                    (endTimestamp - startTimestamp) / 1000));


            response.responseCode = conn.getResponseCode();
            // read the response
            response.response = readResponse(conn.getInputStream());
            conn.disconnect();

            return response;
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private HttpResponse get(String url, String key) throws Exception {
        Log.d(TAG, "GET " + url);
        HttpResponse response = new HttpResponse();

        try {
            URL httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("CLIENTID", clientId);

            long timestamp = new Date().getTime();
            conn.setRequestProperty("TIMESTAMP", "" + timestamp);

            String payload = apiKey + timestamp + key;

            conn.setRequestProperty("HASHTOKEN", hash256(payload));

            conn.setRequestMethod("GET");
            if(conn.getResponseCode() != HTTP_OK &&
                    conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                response.responseCode = conn.getResponseCode();
                String jsonStr = readResponse(conn.getErrorStream());
                JsonObject jsonObject = JsonUtils.fromJson(jsonStr, JsonObject.class);
                response.error = jsonObject.get("message").getAsString();
                return response;
            }

            response.responseCode = conn.getResponseCode();
            response.response = readResponse(conn.getInputStream());
            conn.disconnect();

            return response;
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private String readResponse(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStream in = new BufferedInputStream(inputStream);
        InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8);
        try (Reader reader = new BufferedReader(isr)) {
            int c;
            while ((c = reader.read()) != -1) {
                sb.append((char) c);
            }
        } finally {
            in.close();
            isr.close();
        }
        return sb.toString();
    }

    private static String hash256(String payload) {
        Log.d(TAG, "payload: " + payload);
        MessageDigest messageDigest = null;
        String signatureMethod = "SHA-256";
        try {
            messageDigest = MessageDigest.getInstance(signatureMethod);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] signatureBytes = messageDigest.digest(payload.getBytes());
        String hashToken2 = new String(Base64.encode(signatureBytes, Base64.DEFAULT));
        return hashToken2;
    }

    private String hash512(String payload) {
        long l = new Date().getTime();
        MessageDigest messageDigest = null;
        String signatureMethod = "SHA-512";
        try {
            messageDigest = MessageDigest.getInstance(signatureMethod);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] signatureBytes = messageDigest.digest(payload.getBytes());
        String hashToken2 = new String(Base64.encode(signatureBytes, Base64.DEFAULT));
        Log.d(TAG, hashToken2 + ". time: " + (new Date().getTime() - l) + ". " + payload.length());
        return hashToken2;
    }

    public HttpResponse verify(ElementFMRequest fmRequest) {
        userId = fmRequest.userId;

        HttpResponse response = null;

        try {
            Gson gson = new Gson();
            String sjson = gson.toJson(fmRequest);
            Log.d(TAG, "Request Json:\n" + gson.toJson(fmRequest));
            response = post(serverUrl + "/api/ers/matching", sjson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    public HttpResponse enroll(ElementTrainRequest trainRequest) {
        userId = trainRequest.userId;

        HttpResponse response = null;

        try {
            Gson gson = new Gson();
            String sjson = gson.toJson(trainRequest);
            Log.d(TAG, "Request Json:\n" + gson.toJson(trainRequest));
            response = post(serverUrl + "/api/ers/enroll", sjson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    public HttpResponse checkUserExist(String userId) {
        HttpResponse response = null;

        try {
            response = get(serverUrl + "/api/ers/getUser?userId=" + userId, userId);
            Log.d(TAG, String.format("Get ok response from getUser. Response code = %d, response = %s.",
                        response.responseCode, response.response));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

}