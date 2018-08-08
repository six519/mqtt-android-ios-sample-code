package com.ferdinandsilva.mqtt.mqttandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    boolean isLedOn = false;
    Button buttonSwitch;
    MqttAndroidClient client;
    final String SERVER_URI = "tcp://test.mosquitto.org:1883";
    final String CLIENT_ID = "AndroidClient";
    final String TOPIC_SWITCH = "led_switch";
    final String TOPIC_STATUS = "led_status";
    final String TOPIC_GET_STATUS = "led_get_status";
    final String TAG = "MQTT_ANDROID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSwitch = (Button) findViewById(R.id.buttonSwitch);
        client = new MqttAndroidClient(getApplicationContext(), SERVER_URI, CLIENT_ID);
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if(reconnect) {
                    topicSubscribe();
                }
            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String msg = new String(message.getPayload());

                if(msg.equals("0")) {
                    isLedOn = false;
                    buttonSwitch.setText("TURN LED ON");
                } else {
                    isLedOn = true;
                    buttonSwitch.setText("TURN LED OFF");
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        try {
            client.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    topicSubscribe();
                    publishMessage("GET_STAT", TOPIC_GET_STATUS);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        } catch (MqttException e) {
            Log.d(TAG, e.getMessage());
        }

        buttonSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(client.isConnected() && !buttonSwitch.getText().equals("Connecting...")) {
                    String strMessage;

                    if(isLedOn) {
                        strMessage = "0";
                    } else {
                        strMessage = "1";
                    }

                    publishMessage(strMessage, TOPIC_SWITCH);
                }
            }
        });

    }

    public void publishMessage(String pub_msg, String topic)  {
        MqttMessage msg = new MqttMessage();
        msg.setPayload(pub_msg.getBytes());

        try {
            client.publish(topic, msg);
        } catch (MqttException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void topicSubscribe() {
        try {
            client.subscribe(TOPIC_STATUS, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
        } catch (MqttException e) {
            Log.d(TAG, e.getMessage());
        }
    }
}
