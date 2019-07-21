from pydub import AudioSegment
from pydub.playback import play
import os

data_dir = os.path.join(os.path.dirname(__file__), '../sounds')
path = os.path.join(data_dir, 'prototype_loop1.mp3')

sound = AudioSegment.from_file(path, format="mp3")
# song = AudioSegment.from_mp3("./prototype_loop9.mp3")

play(sound)
