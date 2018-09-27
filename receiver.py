import RPi.GPIO as GPIO
import math
import os
import binascii
from datetime import datetime
from time import sleep

# This is for revision 1 of the Raspberry Pi, Model B
# This pin is also referred to as GPIO23
INPUT_WIRE = 23

GPIO.setmode(GPIO.BCM)
GPIO.setup(INPUT_WIRE, GPIO.IN)

def fletcher32(data, count):
    """
    Calculates the 32-bit Fletcher Checksum.

    :param data: A list of values we need to calculate a checksum for.
    :param count: The number of values within the list to use for the calculation.
    :return: The Fletcher Checksum for the given values.
    """
    sum1 = 0
    sum2 = 0

    for i in range(0, count):
        sum1 = (sum1 + data[i]) % 65535
        sum2 = (sum2 + sum1) % 65535

    return (sum2 << 16) | sum1

def fletcher16(data, count):
    """
    Calculates the 16-bit Fletcher Checksum.

    :param data: A list of values we need to calculate a checksum for.
    :param count: The number of values within the list to use for the calculation.
    :return: The Fletcher Checksum for the given values.
    """
    sum1 = 0
    sum2 = 0

    for i in range(0, count):
        sum1 = (sum1 + data[i]) % 255
        sum2 = (sum2 + sum1) % 255

    return (sum2 << 8) | sum1

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

        # Translate the signal and write the input into a String.
        message = ''
        for (val, pulse) in command:
            # Assume lack of noise. Since both Logic 0 and Logic 1 share pulse size, only consider differences in gaps.
            # if pulse > 1000
            if val == 1:
                # Add up the 2 approximate values, and place a threshold in the middle.
                message += '1' if pulse > 1125 else '0'
                # print "----------Start----------"
                # print val, pulse
                # print "-----------End-----------\n"
                # print "Size of array is " + str(len(command))

        # Display the message:
        print "Message: ", message

        # Extract the note:
        note = binascii.unhexlify('%x' % int(message[0:8], 2))
        amplitude = int(binascii.unhexlify('%x' % int(message[8:16], 2)))
        duration = int(binascii.unhexlify('%x' % int(message[16:24], 2)))
        print "Note: ", note
        print "Amplitude: ", str(amplitude)
        print "Duration: ", str(duration)

        # Verify the checksum (16-bit)
        providedChecksum = binascii.unhexlify('%x' % int(message[24:], 2))
        calculatedChecksum = fletcher16([note, amplitude, duration], 3)
        print "Checksum Matches!" if calculatedChecksum == providedChecksum else "Checksum fails"
