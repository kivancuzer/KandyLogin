package com.example.kandylogin;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

class AccessAndIdTokenTask extends AsyncTask<HashMap<String, String>, Void, String> {

    @Override
    protected String doInBackground(HashMap<String, String>... params) {
        HashMap<String, String> _requestMap = params[0];
        String requestUrl = "https://oauth-cpaas.att.com/cpaas/auth/v1/token";

        return requestAccessAndIdToken(requestUrl, _requestMap.get("username"), _requestMap.get("password"), _requestMap.get("client_id"), _requestMap.get("client_secret"), _requestMap.get("scope"));
    }

    @Override
    protected void onPostExecute(String result) {

        if (result != null) {
            parseAccessAndIDTokenResult(result);
        }

    }

    private String requestAccessAndIdToken(String requestUrl, String username, String password,
                                           String client_id, String client_secret, String scope) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(requestUrl);
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            urlConnection.setDoOutput(true);

            OutputStream outputStream = null;
            try {
                StringBuilder bodyBuilder = new StringBuilder();
                bodyBuilder.append("grant_type=password&");
                bodyBuilder.append("username=").append(username).append("&");
                bodyBuilder.append("password=").append(password).append("&");
                bodyBuilder.append("client_id=").append(client_id).append("&");
                bodyBuilder.append("client_secret=").append(client_secret).append("&");
                bodyBuilder.append("scope=").append(scope);

                outputStream = urlConnection.getOutputStream();
                outputStream.write(bodyBuilder.toString().getBytes());
                outputStream.flush();
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));

            StringBuilder result = new StringBuilder();
            String line;
            if ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (Exception exception) {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            return null;
        }
    }

    public void parseAccessAndIDTokenResult(String result) {

        try {
            JSONObject tokenJSONObject = new JSONObject(result);
            String YOUR_ACCESS_TOKEN = tokenJSONObject.getString("access_token");
            String YOUR_ID_TOKEN = tokenJSONObject.getString("id_token");

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}


