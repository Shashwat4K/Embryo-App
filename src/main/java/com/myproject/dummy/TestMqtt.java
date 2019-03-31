package com.myproject.dummy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class TestMqtt extends AppCompatActivity {
    private String TAG = "mqtt";

    MqttAndroidClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testing_page);
        String clientId = getString(R.string.mqtt_client_id);
         client = new MqttAndroidClient(TestMqtt.this, getString(R.string.mqtt_broker),
                clientId);

        findViewById(R.id.connectbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View ignored) {
              /* _myMqttService = new MyMqttService(getString(R.string.mqtt_broker), getString(R.string.mqtt_client_id));
               //_myMqttService.connect.run();
               Thread connect_thread = new Thread(_myMqttService.connect);
               connect_thread.start();
               .....*/
                MqttConnectOptions options = new MqttConnectOptions();
                options.setUserName(getString(R.string.mqtt_username));
                options.setPassword(getString(R.string.mqtt_password).toCharArray());


                try {
                    //IMqttToken token = client.connect();
                    IMqttToken token = client.connect(options);
                    token.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            // We are connected
                            Toast.makeText(TestMqtt.this, "connected!!", Toast.LENGTH_LONG).show();

                            Log.d(TAG, "onSuccess");
                            try {
                                client.subscribe(getString(R.string.mqtt_topic), 0);
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            // Something went wrong e.g. connection timeout or firewall problems
                            Toast.makeText(TestMqtt.this, "connection failed!!", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "onFailure");

                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {

                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        String payload = new String(message.getPayload());
                        // do something
                        Log.d("MessageArrived! ", payload);
                        ((TextView) findViewById(R.id.rec_msg)).setText(payload);
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {

                    }
                });
            }
        });
       /* findViewById(R.id.publish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pub();
            }
        });*/
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                ((TextView) findViewById(R.id.rec_msg)).setText(new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }

    public void pub(View v)
    {
        Log.d(TAG, "here!!!!!!!!!");
        String topic = "jeevantshash/feeds/welcome-feed";
        String payload = "ON";
        //byte[] encodedPayload;// = new byte[0];
        try {
            //encodedPayload = payload.getBytes("UTF-8");
            //MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic,payload.getBytes(),0,true);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}

/**/