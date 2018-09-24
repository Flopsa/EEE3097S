from datetime import datetime
import time

def test_time(amount):
    start = datetime.now()
    time.sleep(amount)
    end = datetime.now()
    delta = end-start
    return delta.total_seconds()

