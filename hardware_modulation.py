import pigpio

class modulator(object):
    """Hardware modulation class"""
    def __init__(self):
        """Connects to pi"""
        self.pi = pigpio.pi()

    def start(self, freq=38000, duty_cycle=500000):
        """
        Starts hardware modulation.
        Default modulation = 38kHz, 50% duty cycle
        """
        self.pi.hardware_PWM(18, freq, duty_cycle)

    def clear(self):
`       self.pi.hardware_PWM(18, 1, 0)

    def stop(self):
        self.pi.stop()
