package com.example.kandylogin;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.rbbn.cpaas.mobile.authentication.api.ConnectionCallback;
import com.rbbn.cpaas.mobile.utilities.Configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.rbbn.cpaas.mobile.CPaaS;
import com.rbbn.cpaas.mobile.utilities.exception.MobileError;
import com.rbbn.cpaas.mobile.utilities.exception.MobileException;
import com.rbbn.cpaas.mobile.utilities.logging.LogLevel;
import com.rbbn.cpaas.mobile.utilities.services.ServiceInfo;
import com.rbbn.cpaas.mobile.utilities.services.ServiceType;
import com.rbbn.cpaas.mobile.utilities.webrtc.ICEServers;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    String YOUR_ID_TOKEN, YOUR_ACCESS_TOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Configuration configuration = Configuration.getInstance();
        configuration.setUseSecureConnection(true);
        configuration.setRestServerUrl("oauth-cpaas.att.com");

        // Setting ICE Servers
        ICEServers iceServers = new ICEServers();
        iceServers.addICEServer("turns:turn-ucc-1.genband.com:443?transport=tcp");
        iceServers.addICEServer("turns:turn-ucc-2.genband.com:443?transport=tcp");
        iceServers.addICEServer("stun:turn-ucc-1.genband.com:3478?transport=udp");
        iceServers.addICEServer("stun:turn-ucc-2.genband.com:3478?transport=udp");
        configuration.setICEServers(iceServers);

        Button getTokenButton = findViewById(R.id.getTokenButton);
        getTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText usernameField = findViewById(R.id.username);
                String username = usernameField.getText().toString();
                EditText passwordField = findViewById(R.id.password);
                String password = passwordField.getText().toString();
                HashMap<String, String> requestMap = new HashMap<>();
                requestMap.put("username", username);
                requestMap.put("password", password);
                requestMap.put("client_id", "your_client_id");
                requestMap.put("grant_type", "password");
                requestMap.put("scope", "openid");
                requestMap.put("client_secret", "");
                new AccessAndIdTokenTask().execute(requestMap);

                Configuration configuration = Configuration.getInstance();
                // Set the log level to 'TRACE', it's the default option and you can prefer using more or less verbose LogLevels.
                configuration.setLogLevel(LogLevel.TRACE);
                configuration.setLogger(new CustomizedLogger());

            }
        });


        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int lifetime = 3600; //in seconds

                List<ServiceInfo> services = new ArrayList<>();
                services.add(new ServiceInfo(ServiceType.SMS, true));
                services.add(new ServiceInfo(ServiceType.CALL, true));

                CPaaS cpaas = new CPaaS(services);

                try {
                    cpaas.getAuthentication().connect(YOUR_ID_TOKEN, YOUR_ACCESS_TOKEN, lifetime, new ConnectionCallback() {
                        @Override
                        public void onSuccess(String connectionToken) {
                            Log.i("CPaaS.Authentication", "Connected to websocket successfully");
                        }

                        @Override
                        public void onFail(MobileError error) {
                            Log.i("CPaaS.Authentication", "Connection to websocket failed");
                        }
                    });
                } catch (MobileException e) {
                    e.printStackTrace();
                }
            }
        });


    }

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
                YOUR_ACCESS_TOKEN = tokenJSONObject.getString("access_token");
                YOUR_ID_TOKEN = tokenJSONObject.getString("id_token");

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}