import RPi.GPIO as GPIO
import hardware_modulation
import time
import socket
from bluetooth import *
from timeit import default_timer as timer

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
    try:
        start = timer()
        modulator.start(38000, 500000)
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
        print("Message length: " + str(len(message)))
        print("Time taken: " + str(end-start))
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

def recvall(sock):
    BUFF_SIZE = 1024 #this is just the max size of a buffer, not necessarily how much data is actually inside
    part = sock.recv(BUFF_SIZE)
    while True:
        part_end = part.find('\n')
        if part_end != -1:
            data = part[:part_end]
            break

        #part += sock.recv(BUFF_SIZE, socket.MSG_WAITALL)
        part += sock.recv(BUFF_SIZE)
        time.sleep(0.01)
    return data

while True:
    print ("Waiting for connection on RFCOMM channel %d" % port)
    try:
        client_sock, client_info = server_sock.accept()
#        client_sock.settimeout(2)
    except Exception as e:
        print(e)
        pass
    time.sleep(0.01)
    print ("Accepted connection from ", client_info)
    try:
        data = recvall(client_sock) 
        if len(data) == 0: break
        print ("received [%s]" % data)
        print(len(data))

        message = ""
        num_notes = 0 
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
    except KeyboardInterrupt:
        print("Keyboard interrupt")
        client_sock.close()
        server_sock.close()
        break
    except Exception as e:
        print(e.message)
        time.sleep(0.01)
        pass

