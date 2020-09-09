package com.example.kandylogin;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.rbbn.cpaas.mobile.authentication.api.ConnectionCallback;
import com.rbbn.cpaas.mobile.utilities.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.rbbn.cpaas.mobile.CPaaS;
import com.rbbn.cpaas.mobile.utilities.exception.MobileError;
import com.rbbn.cpaas.mobile.utilities.logging.LogLevel;
import com.rbbn.cpaas.mobile.utilities.services.ServiceInfo;
import com.rbbn.cpaas.mobile.utilities.services.ServiceType;
import com.rbbn.cpaas.mobile.utilities.webrtc.ICEServers;

public class MainActivity extends Activity {

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
            }
        });


    }

}