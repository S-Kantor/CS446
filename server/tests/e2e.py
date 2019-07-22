import run
from music import BeatTimestamp, FileOffsetRecording

simple_json_body = {'startTime': '201:21:51:00:000',
                    'endTime': '201:21:51:06:000',
                    'recordingEntries': [
                        {'dateTime': '201:21:51:01:000', 'fileName': 'prototype_loop1', 'loopable': True},
                        {'dateTime': '201:21:51:01:800', 'fileName': 'prototype_loop1', 'loopable': True},
                        {'dateTime': '201:21:51:03:000', 'fileName': 'prototype_loop1', 'loopable': True},
                        {'dateTime': '201:21:51:05:000', 'fileName': 'prototype_loop1', 'loopable': True}
                    ], }

overlap_json_body = {'startTime': '201:21:51:00:000',
                     'endTime': '201:21:51:06:000',
                     'recordingEntries': [
                         {'dateTime': '201:21:51:01:000', 'fileName': 'prototype_loop1', 'loopable': True},
                         {'dateTime': '201:21:51:01:800', 'fileName': 'prototype_loop2', 'loopable': True},
                         {'dateTime': '201:21:51:03:000', 'fileName': 'prototype_loop2', 'loopable': True},
                         {'dateTime': '201:21:51:05:000', 'fileName': 'prototype_loop1', 'loopable': True}
                     ], }

single_json_body_1 = {'startTime': '201:21:51:00:000',
                      'endTime': '201:21:51:06:000',
                      'recordingEntries': [
                          {'dateTime': '201:21:51:01:000', 'fileName': 'prototype_loop1', 'loopable': True},
                          {'dateTime': '201:21:51:05:000', 'fileName': 'prototype_loop1', 'loopable': True}
                      ], }

single_json_body_2 = {'startTime': '201:21:50:59:000',
                      'endTime': '201:21:51:08:000',
                      'recordingEntries': [
                          {'dateTime': '201:21:51:01:800', 'fileName': 'prototype_loop2', 'loopable': True},
                          {'dateTime': '201:21:51:03:000', 'fileName': 'prototype_loop2', 'loopable': True},
                      ], }

single_json_body_3 = {'endTime': '202:22:36:01:999', 'recordingEntries': [
    {'dateTime': '202:22:35:50:097', 'fileName': 'prototype_loop1', 'loopable': True},
    {'dateTime': '202:22:35:53:634', 'fileName': 'prototype_loop1', 'loopable': True},
    {'dateTime': '202:22:35:54:066', 'fileName': 'prototype_loop2', 'loopable': True},
    {'dateTime': '202:22:35:58:519', 'fileName': 'prototype_loop2', 'loopable': True}], 'startTime': '202:22:35:44:202'}

single_json_body_4 = {'endTime': '202:22:35:39:695', 'recordingEntries': [
    {'dateTime': '202:22:35:25:430', 'fileName': 'prototype_loop1', 'loopable': True},
    {'dateTime': '202:22:35:27:958', 'fileName': 'prototype_loop1', 'loopable': True},
    {'dateTime': '202:22:35:32:643', 'fileName': 'prototype_loop1', 'loopable': True},
    {'dateTime': '202:22:35:35:117', 'fileName': 'prototype_loop1', 'loopable': True}], 'startTime': '202:22:35:20:105'}


def singleCompositionE2E(json_body):
    room_id = run.create_room()

    run.start_recording(room_id)

    timestamps = [BeatTimestamp(bool(e['loopable']), run.parse_time(e['dateTime']), e['fileName'])
                  for e in json_body['recordingEntries']]
    fosr = FileOffsetRecording(
        run.parse_time(json_body['startTime']),
        run.parse_time(json_body['endTime']),
        timestamps)

    complete = run.rooms[room_id].stop_recording(fosr)

    run.rooms[room_id].music_producer.play()


def doubleCompositionE2E(json_body_1, json_body_2):
    room_id = run.create_room()

    run.start_recording(room_id)
    run.start_recording(room_id)

    timestamps = [BeatTimestamp(bool(e['loopable']), run.parse_time(e['dateTime']), e['fileName'])
                  for e in json_body_1['recordingEntries']]
    fosr = FileOffsetRecording(
        run.parse_time(json_body_1['startTime']),
        run.parse_time(json_body_1['endTime']),
        timestamps)
    complete = run.rooms[room_id].stop_recording(fosr)

    timestamps = [BeatTimestamp(bool(e['loopable']), run.parse_time(e['dateTime']), e['fileName'])
                  for e in json_body_2['recordingEntries']]
    fosr = FileOffsetRecording(
        run.parse_time(json_body_2['startTime']),
        run.parse_time(json_body_2['endTime']),
        timestamps)
    complete = run.rooms[room_id].stop_recording(fosr)

    run.rooms[room_id].music_producer.play()


doubleCompositionE2E(single_json_body_3, single_json_body_4)
