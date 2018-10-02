import RPi.GPIO as GPIO
import hardware_modulation
import time
from bluetooth import *
import bitarray

server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

advertise_service( server_sock, "SampleServer",
                   service_id = uuid,
                   service_classes = [ uuid, SERIAL_PORT_CLASS ],
                   profiles = [ SERIAL_PORT_PROFILE ], 
#                   protocols = [ OBEX_UUID ] 
                    )
                   
GPIO.setmode(GPIO.BCM)

led_pin = 23

GPIO.setup(led_pin, GPIO.OUT)

def transmit_message(message):
    modulator = hardware_modulation.modulator()
    modulator.start()
    try:
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
    except KeyboardInterrupt:
        GPIO.cleanup()
        modulator.stop()

def fletcher16(data, count):
    sum1 = 0
    sum2 = 0

    for i in range(0, count):
        sum1 = (sum1 + data[i]) % 255
        sum2 = (sum1 + sum2) % 255

    return (sum2 << 8) | sum1

while True:
    print "Waiting for connection on RFCOMM channel %d" % port
    ba = bitarray.bitarray()
    time.sleep(0.01)
    try:
        client_sock, client_info = server_sock.accept()
    except Exception as e:
        print(e)
        pass
    time.sleep(0.01)
    print "Accepted connection from ", client_info
    try:
        data = client_sock.recv(1024)
        if len(data) == 0: break
        print "received [%s]" % data

        if len(data) > 1:
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
                    checksum = fletcher16(fletcher, len(fletcher))
                    message = message + format(checksum, '016b')
                    fletcher = []
            #end signal
            message = message + '000'
            transmit_message(message) 
    except IOError as e:
        print(e)
        pass

    except KeyboardInterrupt:
        print("Keyboard interrupt")
        client_sock.close()
        server_sock.close()

