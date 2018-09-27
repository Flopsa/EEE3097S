import RPi.GPIO as GPIO
import hardware_modulation
import time

GPIO.setmode(GPIO.BCM)

led_pin = 23

GPIO.setup(led_pin, GPIO.OUT)

test_transmit = [1, 1, 0, 1, 1]
modulator = hardware_modulation.modulator()

try:
    while True:
        for i in test_transmit:
            if i == 1:
                modulator.start()
                GPIO.output(led_pin, GPIO.HIGH)
                time.sleep(0.0005625) #562.5 us
                modulator.clear()
                GPIO.output(led_pin, GPIO.LOW)
                time.sleep(0.0016875) #1.6875 ms
            else:
                modulator.clear()
                GPIO.output(led_pin, GPIO.LOW)
                time.sleep(0.00125) #1.125 ms
except KeyboardInterrupt:
    GPIO.cleanup()
    modulator.stop()

