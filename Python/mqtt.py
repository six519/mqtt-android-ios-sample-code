#!/usr/bin/env python
import paho.mqtt.client as mqtt
import RPi.GPIO as GPIO

SERVER_URI = "test.mosquitto.org"
SERVER_PORT = 1883
TOPIC_SWITCH = "led_switch"
TOPIC_STATUS = "led_status"
TOPIC_GET_STATUS = "led_get_status"
PIN = 21

isLedOn = False

def on_connect(client, userdata, flags, rc):
    client.subscribe(TOPIC_GET_STATUS)
    client.subscribe(TOPIC_SWITCH)

def on_message(client, userdata, msg):
    #print "Topic: %s\nPayload: %s" % (msg.topic, msg.payload)
    global isLedOn
    if msg.topic == TOPIC_GET_STATUS:
        if isLedOn:
            client.publish(TOPIC_STATUS, "1")
        else:
            client.publish(TOPIC_STATUS, "0")
    else:
        if msg.payload == "0":
            isLedOn = False
            client.publish(TOPIC_STATUS, "0")
            GPIO.output(21, GPIO.LOW)
        else:
            isLedOn = True
            client.publish(TOPIC_STATUS, "1")
            GPIO.output(21, GPIO.HIGH)

if __name__ == "__main__":

    GPIO.setmode(GPIO.BCM)
    GPIO.setup(PIN, GPIO.OUT)

    client = mqtt.Client()
    client.on_connect = on_connect
    client.on_message = on_message

    client.connect(SERVER_URI, SERVER_PORT, 60)

    try:
        client.loop_forever()
    except KeyboardInterrupt:
        pass