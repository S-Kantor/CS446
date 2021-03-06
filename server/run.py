from datetime import datetime
from typing import Dict

from flask import Flask, request, send_file

from music import BeatTimestamp, FileOffsetRecording
from room import Room

app = Flask(__name__)

rooms: Dict[str, Room] = {}


# Accepts datetime in milliseconds and converts to microseconds
def parse_time(time_as_string):
    return datetime.strptime(time_as_string + '000', "%j:%H:%M:%S:%f")


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
    room_id = Room.gen_room_id(rooms.keys())
    new_room = Room(room_id)
    rooms[new_room.id] = new_room
    app.logger.debug('room created: %s', str(new_room))
    return str(new_room.id)


# Allows a new user to validate their room ID
@app.route("/<string:room_id>/is-valid-room-id", methods=['POST'])
def is_valid_room_id(room_id):
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
    return str(rooms[room_id].start_recording())


# Informs the server that a user is finished recording and provides
#   the FileOffsetRecordings as a json in the following format:
# {
#   'start_time': "%D:%H:%M:%S.%f" (f is milliseconds)
#   'end_time': "%D:%H:%M:%S.%f"
#   'events' : [
#       {
#           filename: string,   -- name of the audio file (uploaded and default)
#           time: "%D:%H:%M:%S.%f"
#           loopable: bool
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
    timestamps = [BeatTimestamp(bool(e['loopable']), parse_time(e['dateTime']), e['fileName'])
                  for e in json['recordingEntries']]
    offset_recording = FileOffsetRecording(
        parse_time(json['startTime']),
        parse_time(json['endTime']),
        timestamps)
    complete = rooms[room_id].stop_recording(offset_recording)
    app.logger.debug('last user: %s', complete)
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
    return str(rooms[room_id].is_recording_complete())


# Returns the generated composition as an mp3 file
# Produces composition if necessary with the FileOffsetRecordings
@app.route("/<string:room_id>/get-composition")
def get_composition(room_id):
    app.logger.debug('getting composition for room %s', room_id)
    file = rooms[room_id].get_composition_as_mp3()
    return send_file(file, mimetype="audio/mpeg")


# --------------------------------------------------------
# Misc.
# --------------------------------------------------------

# Simple health check to test connection to server
@app.route("/health-check")
def health_check():
    return "I'm Alive!"


# Can be called as an hourly chron job to clean expired data
@app.route("/cleanup", methods=['POST'])
def cleanup_rooms():
    app.logger.debug('server cleaning up')
    expired_rooms = [room_id for room_id, room in rooms if room.is_expired()]
    for key in expired_rooms:
        app.logger.debug('%s has expired', str(key))
        del rooms[key]
