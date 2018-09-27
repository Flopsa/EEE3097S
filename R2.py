import binascii

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

print(fletcher16([97, 10, 10], 3))

message = "011000010000101000001010" + "100001001110101"

print(message[8:16])
note = binascii.unhexlify('%x' % int(message[0:8], 2))
amplitude = int(message[8:16], 2)
duration = int(message[16:24], 2)
print("Note: ", note)
print("Amplitude: ", amplitude)
print("Duration: ", duration)

# Verify the checksum (16-bit)
providedChecksum = int(message[24:], 2)
print("Provided Checksum: ", providedChecksum)
calculatedChecksum = fletcher16([int(message[0:8], 2), amplitude, duration], 3)
print("Calculated Checksum: ", calculatedChecksum)
print("Checksum Matches!" if calculatedChecksum == providedChecksum else "Checksum fails")