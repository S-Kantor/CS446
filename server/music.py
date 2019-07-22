import datetime
import logging
import os

from pydub import AudioSegment
from pydub.playback import play

SOUND_DIR = os.path.join(os.path.dirname(__file__), 'sounds')


def delta_to_milli(x: datetime.timedelta):
    return x.total_seconds() * 1000


class MusicProducer:
    composition: AudioSegment

    def __init__(self):
        self.new_sounds = {}

        self.file_offset_recordings = []
        self.recording_start_time = datetime.datetime.now()
        self.composition = None

    def produce(self):
        logging.getLogger(__name__).debug('producing file')
        earliest_timing = min(self.file_offset_recordings, key=lambda t: t.start_time).start_time

        # find longest duration by adding offset of the recording and its duration
        offsets = {}
        for file_offset_recording in self.file_offset_recordings:
            offsets[file_offset_recording] = file_offset_recording.start_time - earliest_timing
        longest_fosr = max(offsets, key=lambda t: offsets[t] + t.time_delta())

        base_segment = AudioSegment.silent(duration=delta_to_milli(offsets[longest_fosr] + longest_fosr.time_delta()))
        for fosr in offsets.keys():
            base_segment = base_segment.overlay(fosr.get_composition(), position=delta_to_milli(offsets[fosr]))
        self.composition = base_segment

    def add_offset_recording(self, offset_recording):
        self.file_offset_recordings.append(offset_recording)
        offset_recording.produce(self.new_sounds)

    def get_composition_as_mp3(self):
        if not self.composition:
            self.produce()

        return self.composition.export(format='mp3')

    def play(self):
        play(self.composition)

    def add_new_sound(self, filename, file):
        self.new_sounds[filename] = AudioSegment.from_file(file, format="mp4")
        return True


class BeatOffset:
    filename: str
    loopable: bool
    offset: float
    duration: int

    def __init__(self, filename, loopable, offset, duration=None):
        self.filename = filename
        self.loopable = loopable
        self.offset = offset
        self.duration = duration


class BeatTimestamp:
    filename: str
    loopable: bool
    time: datetime.datetime

    def __init__(self, loopable, time, filename):
        self.filename = filename
        self.loopable = loopable
        self.time = time


class FileOffsetRecording:
    composition: AudioSegment

    def __init__(self, start_time, end_time, timestamps):
        self.start_time = start_time
        self.end_time = end_time

        self.timestamps = timestamps

    def gen_offsets(self):
        loopable_start_times = {}
        result = []
        for timestamp in self.timestamps:
            if timestamp.loopable:
                if timestamp.filename in loopable_start_times:
                    result.append(BeatOffset(
                        timestamp.filename,
                        timestamp.loopable,
                        delta_to_milli(loopable_start_times[timestamp.filename] - self.start_time),
                        delta_to_milli(timestamp.time - loopable_start_times[timestamp.filename])))
                    loopable_start_times.pop(timestamp.filename)
                else:
                    loopable_start_times[timestamp.filename] = timestamp.time
            else:
                result.append(BeatOffset(timestamp.filename,
                                         timestamp.loopable,
                                         delta_to_milli(timestamp.time - self.start_time)))
        return result

    def produce(self, new_sounds):
        base_segment = AudioSegment.silent(duration=delta_to_milli(self.time_delta()))
        for beat_offset in self.gen_offsets():
            if beat_offset.filename in new_sounds:
                sound = new_sounds[beat_offset.filename]
            else:
                path = os.path.join(SOUND_DIR, beat_offset.filename)
                sound = AudioSegment.from_file(path + '.mp3', format="mp3")

            # if the sound is loopable create empty segment of correct length
            if beat_offset.loopable:
                beat_base = AudioSegment.silent(duration=beat_offset.duration)
                sound = beat_base.overlay(sound, loop=True)

            base_segment = base_segment.overlay(sound, position=beat_offset.offset)
        self.composition = base_segment

    def get_composition(self):
        return self.composition

    def play(self):
        play(self.composition)

    def time_delta(self):
        return self.end_time - self.start_time
