from run import app
from flask import url_for

# --------------------------------------------------------
# Testing
# --------------------------------------------------------
with app.test_request_context():
    print(url_for('join_room', room_id=123))
