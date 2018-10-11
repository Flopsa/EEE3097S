import RPi.GPIO as GPIO
import hardware_modulation
import time
from timeit import default_timer as timer
import csv

GPIO.setmode(GPIO.BCM)

led_pin = 23
GPIO.setup(led_pin, GPIO.OUT)

modulator = hardware_modulation.modulator()
modulator.start(38000, 500000)

def write(length, speed):
    f = open("test_transmitter_speeds.csv", "a")
    f.write(length+","+speed+"\n")
    f.close()

def transmit_message(message):
    try:
        start = timer()
        GPIO.output(led_pin, GPIO.HIGH)
        time.sleep(0.0090000) # 9 ms
        GPIO.output(led_pin, GPIO.LOW)
        time.sleep(0.0045000) # 4.5 ms
        
        for i in message:
            if i == "1":
                #modulator.start()
                GPIO.output(led_pin, GPIO.HIGH)
                time.sleep(0.0005625) #562.5 us
                #modulator.clear()
                GPIO.output(led_pin, GPIO.LOW)
                time.sleep(0.0016875) #1.6875 ms
            else:
                #modulator.clear()
                GPIO.output(led_pin, GPIO.HIGH)
                time.sleep(0.0005625) #562.5 us
                GPIO.output(led_pin, GPIO.LOW)
                time.sleep(0.0005625) #562.5 us
        end = timer()
        #print("Bit length: " + str(len(message)))
        #print("Time taken: " + str(end-start))
        write(str(len(message)), str(end-start))
    except KeyboardInterrupt:
        GPIO.cleanup()
        modulator.stop()

def fletcher8(data, count):
    sum1 = 0
    sum2 = 0

    for i in range(0, count):
        sum1 = (sum1 + data[i]) % 15
        sum2 = (sum1 + sum2) % 15

    return (sum2 << 4) | sum1

def build_message(data):
    message = ""
    fletcher = []
    parts = data.split(",")
    for i in parts:
        if i.isdigit():
            binary = format(int(i), '08b')
            fletcher.append(int(i))
            message = message + binary
        else:
            int_val = ord(i)
            fletcher.append(int_val)
            message = message + format(int_val, '08b')

        if (len(fletcher) == 3):
            checksum = fletcher8(fletcher, len(fletcher))
            message = message + format(checksum, '08b')
            fletcher = []
    message = message + '0'
    transmit_message(message)

data = "a,10,10"
build_message(data)

for i in range(0, 1000, 100):
    data += ",a,10,10"
    build_message(data)
