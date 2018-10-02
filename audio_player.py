# Generate a 440 Hz square waveform in Pygame by building an array of samples and play
# it for 5 seconds.  Change the hard-coded 440 to another value to generate a different
# pitch.
#
# Run with the following command:
#   python pygame-play-tone.py

from array import array
from time import sleep

import pygame
from pygame.mixer import Sound, get_init, pre_init,get_busy

c = 523
d = 587
e = 659
f = 698
g = 783
a = 880
b = 987

class Note(Sound):

    def __init__(self, frequency,vol):
        self.frequency = frequency
        Sound.__init__(self, self.build_samples())
        self.set_volume(vol)

    def build_samples(self):
        period = int(round(get_init()[0] / self.frequency))
        #print("self.frequency: ",self.frequency)
        #print("period: ",period)
        samples = array("h", [0] * period)
        amplitude = 2 ** (abs(get_init()[1]) - 1) - 1
        for time in range(period):
            if time < period / 2:
                samples[time] = amplitude
            else:
                samples[time] = -amplitude
        return samples

def playNote(note,amplitude,duration):
    new_note = Note(note, amplitude)
    new_note.play(-1, duration)
    while(get_busy()):
        #print(get_busy())
        #pass
        print()
    new_note.stop()
    #print(get_busy)
    #print("Finished")
    #Note(note,amplitude).play(duration)
    #sleep(2)

if __name__ == "__main__":
    
    pre_init(44100, -16, 1, 1024)
    pygame.init()
    
    playNote(e,1,250)
    playNote(e,1,500)
    playNote(e,1,500)
    playNote(c,1,250)
    playNote(e,1,500)
    playNote(g,1,500)
    sleep(0.25)
    #playNote(g,1,250)
    #sleep(2)
    #playNote(e,1,750)
    #playNote(g,1,1000)
    #playNote(a,1,1000)
    #playNote(f,1,1000)
    #playNote(g,1,1000)

    pygame.quit()

    print("done 2")
    
    
