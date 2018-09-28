import RPi.GPIO as GPIO
import math
import os
from datetime import datetime
from time import sleep

# This is for revision 1 of the Raspberry Pi, Model B
# This pin is also referred to as GPIO23
INPUT_WIRE = 23

GPIO.setmode(GPIO.BCM)
GPIO.setup(INPUT_WIRE, GPIO.IN)

def fletcher16(data, count):
    """"""
    sum1 = 0
    sum2 = 0

    for i in range(0, count):
        sum1 = (sum1 + data[i]) % 255
        sum2 = (sum2 + sum1) % 255

    return (sum2 << 8) | sum1

# providedChecksum = bla bla bla
# calculatedChecksum = fletcher32([ord(note), amplitude, duration], 3)
# checkSumMatches = providedChecksum == calculatedChecksum

while True:
    value = 1
    # Loop until we read a 0
    while value:
        value = GPIO.input(INPUT_WIRE)

        # Grab the start time of the command
    startTime = datetime.now()

        # Used to buffer the command pulses
    command = []

        # The end of the "command" happens when we read more than
        # a certain number of 1s (1 is off for my IR receiver)
    numOnes = 0

        # Used to keep track of transitions from 1 to 0
    previousVal = 0

    while True:
        if value != previousVal:
        # The value has changed, so calculate the length of this run
            now = datetime.now()
            pulseLength = now - startTime
            startTime = now

            command.append((previousVal, pulseLength.microseconds))

        if value:
            numOnes = numOnes + 1
        else:
            numOnes = 0

        # 10000 is arbitrary, adjust as necessary
        if numOnes > 10000:
            break

        previousVal = value
        value = GPIO.input(INPUT_WIRE)

    message = ''
    '''
    print("----------Start---------")
    for (val, pulse) in command:
        print val, pulse
    print("----------End-----------")
    '''
    for i in range(0, len(command)-1, 2):
        if (command[i][1] > 8000) and (command[i+1][1] > 3000 and command[i+1][1] < 5000): #Start command
            for j in range(i+2, len(command)-1, 2):
                #both have same starting pulse
                if command[j+1][1] > 1000:
                    message = message + '1'
                elif command[j+1][1] < 1000:
                    message  = message +'0'
            break
    if len(message) > 24: 
        print("Message {}".format(message))
        note = chr(int(message[0:8], 2))
        amp = int(message[8:16], 2)
        duration = int(message[16:24], 2)
        checksum = int(message[24:40], 2)

        print("Note:" + note)
        print("Amp: " + str(amp))
        print("Dur: " + str(duration))
        print("Check received " + str(checksum))

        new_check = fletcher16([ord(note), amp, duration], 3)
        print("new checksum: " + str(new_check))
       
