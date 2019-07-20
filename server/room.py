import uuid
import datetime

from music import MusicProducer

ROOM_EXPIRY_DELTA = datetime.timedelta(days=10)
ID_LENGTH = 4


# class User:
#     id: uuid.UUID
#     recording: bool
#
#     def __init__(self):
#         self.id = uuid.uuid4()
#         self.recording = True

class Room:
    # creator: User
    # guests: {}

    id: str
    recording: bool
    music_producer: MusicProducer

    def __init__(self, room_id):
        self.id = room_id
        self.creation_time = datetime.datetime.now()

        self.total_users = 1
        self.recording_users = 0
        self.finished_users = 0

        self.music_producer = MusicProducer()

        # self.creator = creator
        # self.guests = {}

    def start_recording(self):
        self.recording_users += 1

    def stop_recording(self, new_timing):
        self.recording_users -= 1
        self.music_producer.add_timing(new_timing)
        return self.recording_users == 0

    def get_composition_as_mp3(self):
        return self.music_producer.get_composition_as_mp3()

    def is_recording(self):
        return self.recording_users == 0

    def is_expired(self):
        return (datetime.datetime.now() - self.creation_time) > ROOM_EXPIRY_DELTA

    def __str__(self):
        return str({'id': str(self.id)})

    def add_new_sound(self, filename, file):
        self.music_producer.add_new_sound(filename, file)

    @staticmethod
    def gen_room_id(existing_ids):
        result = str(uuid.uuid4())[:ID_LENGTH]
        while result in existing_ids:
            result = str(uuid.uuid4())[:ID_LENGTH]
        return result
