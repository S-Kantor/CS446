import run
from music import BeatTimestamp, FileOffsetRecording

# def singleCompositionE2E():

room_id = run.create_room()

run.start_recording(room_id)

json_body = {'startTime': '201:21:51:00:000',
             'endTime': '201:21:51:06:000',
             'recordingEntries': [{'dateTime': '201:21:51:01:000', 'fileName': 'prototype_loop1.mp3', 'loopable': True},
                                  {'dateTime': '201:21:51:01:800', 'fileName': 'prototype_loop1.mp3', 'loopable': True},
                                  {'dateTime': '201:21:51:03:000', 'fileName': 'prototype_loop1.mp3', 'loopable': True},
                                  {'dateTime': '201:21:51:05:000', 'fileName': 'prototype_loop1.mp3', 'loopable': True}
                                  ], }

timestamps = [BeatTimestamp(bool(e['loopable']), run.parse_time(e['dateTime']), e['fileName'])
              for e in json_body['recordingEntries']]
fosr = FileOffsetRecording(
    run.parse_time(json_body['startTime']),
    run.parse_time(json_body['endTime']),
    timestamps)

complete = run.rooms[room_id].stop_recording(fosr)

run.rooms[room_id].music_producer.play()
