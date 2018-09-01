# file: rfcomm-server.py
# auth: Albert Huang <albert@csail.mit.edu>
# desc: simple demonstration of a server application that uses RFCOMM sockets
#
# $Id: rfcomm-server.py 518 2007-08-10 07:20:07Z albert $

from bluetooth import *
import time
import bitarray
import pyslinger

protocol = "NEC"
protocol_config = {}
gpio_pin = 22
ir = pyslinger.IR(gpio_pin, protocol, protocol_config)

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
                   
def transmit_message(message):
    wf = []
    parts = data.split(",")
    for i in parts:
        if i.isdigit():
            binary = format(int(i), '08b')
            for j in binary:
                if(int(j)):
                    wf.append("1")
                else:
                    wf.append("0")
        else:
            ba.frombytes(i.encode('utf-8'))
            for j in ba:
                if(j):
                    wf.append("1")
                else:
                    wf.append("0")
    ir.send_code(wf)

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
            transmit_message(data) 
    except IOError as e:
        print(e)
        pass

    except KeyboardInterrupt:
        print("Keyboard interrupt")
        client_sock.close()
        server_sock.close()
        ir.terminate()

