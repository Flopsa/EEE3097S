import RPi.GPIO as GPIO
import math
import os
from datetime import datetime
from time import sleep
import binascii

# This is for revision 1 of the Raspberry Pi, Model B
# This pin is also referred to as GPIO23
INPUT_WIRE = 23

GPIO.setmode(GPIO.BCM)
GPIO.setup(INPUT_WIRE, GPIO.IN)

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
            
        #Translate signal and write input into string
        message =''
        for (val, pulse) in command:
            #Assume no noise and consider gap difference
            if val ==1:
                #add the 2 approx vals and place threshold in middle
                message+='1' if pulse>1125 else '0'
            #if pulse > 1000:
                #print ("----------Start----------")
                #print (val, pulse)
                #print ("-----------End-----------\n")
                #print ("Size of array is " + str(len(command)))
        #Display message
        
        if len(message)>0:
            print("Message: ", message)
            
            #find note,
            note = binascii.unhexlify('%x' % int(message[0:8] , 2))
            print("Note: ", note)
        
            #Frequency
            frequency = int(message[8:16],2)
            print("Frequency: ", frequency)
            
            #Amplitude
            amplitude = int(message[16:33],2)
            print("Amplitude: ", amplitude)                
