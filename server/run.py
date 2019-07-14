from flask import Flask, request, send_file
from room import Room
from music import FileOffsetRecording

from typing import Dict
import uuid

app = Flask(__name__)

rooms: Dict[str, Room] = {}


@app.route("/")
def hello():
    return "Hello World!"


# --------------------------------------------------------
# Rooms
# --------------------------------------------------------

# Adds a new room to the rooms dictionary and returns it's ID
# The room ID is necessary for all future interactions
@app.route("/create-room", methods=['POST'])
def create_room():
    # new_user = User
    # new_room = Room(new_user)
    # return {
    #     'user_id': str(new_user.id),
    #     'room_id': str(new_room.id),
    # }
    room_id = Room.gen_room_id(rooms.keys())
    new_room = Room(room_id)
    rooms[new_room.id] = new_room
    app.logger.debug('room created: %s', str(new_room))
    return str(new_room.id)


# Allows a new user to validate their room ID
@app.route("/<string:room_id>/is-valid-room-id", methods=['POST'])
def is_valid_room_id(room_id):
    # new_user = User
    # rooms[uuid.UUID(room_id)].join(new_user)
    app.logger.debug('validating room: %s', room_id)
    return str(room_id in rooms)


# --------------------------------------------------------
# Recording
# --------------------------------------------------------

# Informs the server that a user has begun recording
# All users who start recording in a room must stop recording for
#   a composition to be produced
@app.route("/<string:room_id>/start-recording", methods=['POST'])
def start_recording(room_id):
    app.logger.debug('a user started recording: %s', room_id)
    rooms[room_id].start_recording()
    return 0


# Informs the server that a user is finished recording and provides
#   the FileOffsetRecordings as a json in the following format:
# {
#   'start_time': "%H:%M:%S.%f" (f is microseconds)
#   'end_time': "%H:%M:%S.%f"
#   'offsets' : [
#       {
#           filename: string,   -- name of the audio file (uploaded and default)
#           offset: int,        -- offset in milliseconds
#           times: int,         -- number of times to repeat the audio file
#       },
#       ...
#   ]
# }
# Returns whether the recording session is complete
@app.route("/<string:room_id>/stop-recording", methods=['POST'])
def stop_recording(room_id):
    app.logger.debug('a user stopped recording: %s', room_id)

    json = request.get_json()
    app.logger.debug(json)
    new_timing = FileOffsetRecording(json['start_time'],
                                     json['end_time'],
                                     json['offsets'], )
    complete = rooms[room_id].stop_recording(new_timing)
    app.logger.debug('last user: %b', complete)
    return str(complete)


# Upload a sound file to current recording session
# Must be in .mp4 format and with the filename that will be used to
#   reference the file in the offsets of the FileOffsetRecordings
@app.route("/<string:room_id>/upload-sound", methods=['PUT'])
def upload_sound(room_id):
    filename = request.files.keys[0]
    app.logger.debug('new file %s uploaded to room %s', filename, room_id)
    file = request.files[filename]
    return str(rooms[room_id].add_new_sound(filename, file))


# --------------------------------------------------------
# Getting Composition
# --------------------------------------------------------

# Returns whether a is recording complete, meaning the same number of users
#   who started recording have stopped
# Allows users to poll when they should call get-composition
@app.route("/<string:room_id>/is-recording-complete")
def is_recording_complete(room_id):
    app.logger.debug('checking whether recording is complete for room %s', room_id)
    return str(not rooms[room_id].is_recording())


# Returns the generated composition as an mp3 file
# Produces composition if necessary with the FileOffsetRecordings
@app.route("/<string:room_id>/get-composition")
def get_composition(room_id):
    app.logger.debug('getting composition for room %s', room_id)
    file = rooms[room_id].get_composition_as_mp3()
    return send_file(file)


# --------------------------------------------------------
# Server Cleanup
# --------------------------------------------------------

# Can be called as an hourly chron job to clean expired data
@app.route("/cleanup", methods=['POST'])
def cleanup_rooms():
    app.logger.debug('server cleaning up')
    expired_rooms = [room_id for room_id, room in rooms if room.is_expired()]
    for key in expired_rooms:
        app.logger.debug('%s has expired', str(key))
        del rooms[key]
