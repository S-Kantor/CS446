import logging
import datetime

from pydub import AudioSegment
from pydub.playback import play
import os

SOUND_DIR = os.path.join(os.path.dirname(__file__), 'sounds')


class MusicProducer:
    composition: AudioSegment

    def __init__(self):
        self.new_sounds = {}

        self.file_offset_recordings = []
        self.recording_start_time = datetime.datetime.now()

    def produce(self):
        logging.getLogger(__name__).debug('producing file')
        earliest_timing = min(self.file_offset_recordings, lambda t: t.start_time)

        offsets = {}
        for timing in self.file_offset_recordings:
            offsets[timing] = timing.start_time - earliest_timing
        duration = max(offsets, lambda o, t: o + len(t))

        base_segment = AudioSegment.silent(duration=duration)
        for offset, timing in offsets:
            timing.produce(self.new_sounds)
            base_segment.overlay(timing.get_composition(), position=offset)
        self.composition = base_segment

    def add_timing(self, new_timing):
        self.file_offset_recordings.append(new_timing)

    def get_composition_as_mp3(self):
        if not self.composition:
            self.produce()

        return self.composition.export(format='mp3')

    def play(self):
        play(self.composition)

    def add_new_sound(self, filename, file):
        self.new_sounds[filename] = AudioSegment.from_file(file, format="mp4")
        return True


class FileOffsetRecording:
    composition: AudioSegment

    def __init__(self, start_time, end_time, offsets):
        self.start_time = datetime.datetime.strptime(start_time, "%H:%M:%S.%f")
        self.end_time = datetime.datetime.strptime(end_time, "%H:%M:%S.%f")

        # todo: convert offsets to list(filename, offset, times)
        self.offsets = offsets

    def produce(self, new_sounds):
        base_segment = AudioSegment.silent(duration=len(self))
        for filename, offset, times in self.offsets:
            if filename in new_sounds:
                sound = new_sounds[filename]
            else:
                path = os.path.join(SOUND_DIR, filename)
                sound = AudioSegment.from_file(path, format="mp3")
            base_segment.overlay(sound, position=offset, times=times)
        self.composition = base_segment

    def get_composition(self):
        return self.composition

    def play(self):
        play(self.composition)

    def __len__(self):
        self.end_time - self.start_time
