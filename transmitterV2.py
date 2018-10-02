import RPi.GPIO as GPIO
import hardware_modulation
import time

GPIO.setmode(GPIO.BCM)

led_pin = 23

GPIO.setup(led_pin, GPIO.OUT)

#test_transmit = [1, 1, 0, 1, 1]
modulator = hardware_modulation.modulator()

def fletcher16(data, count):
    sum1 = 0
    sum2 = 0

    for i in range(0, count):
        sum1 = (sum1 + data[i]) % 255
        sum2 = (sum1 + sum2) % 255

    return (sum2 << 8) | sum1

data = [97, 10, 10]
checksum = fletcher16(data, len(data))
message = ""
for i in data:
    message = message + format(i, '08b')
message = message + format(checksum, '016b')
print("")
print(checksum)
print(message)

modulator.start()
try:
    #while True:  
    GPIO.output(led_pin, GPIO.HIGH)
    time.sleep(0.0090000) # 9 ms
    GPIO.output(led_pin, GPIO.LOW)
    time.sleep(0.0045000) # 4.5 ms
    
    for i in message:
        if i == "1":
            print("one")
            #modulator.start()
            GPIO.output(led_pin, GPIO.HIGH)
            time.sleep(0.0005625) #562.5 us
            #modulator.clear()
            GPIO.output(led_pin, GPIO.LOW)
            time.sleep(0.0016875) #1.6875 ms
        else:
            print("zero")
            #modulator.clear()
            GPIO.output(led_pin, GPIO.HIGH)
            time.sleep(0.0005625) #562.5 us
            GPIO.output(led_pin, GPIO.LOW)
            time.sleep(0.0005625) #562.5 us
except KeyboardInterrupt:
    GPIO.cleanup()
    modulator.stop()

