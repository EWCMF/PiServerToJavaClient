#!/usr/bin/python
# Copyright (c) 2014 Adafruit Industries
# Author: Tony DiCola

# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:

# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.

# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
import sys
import socket
import time
import datetime
import sched

# import Adafruit_DHT

# Script ændrede så command line argumenter ikke er nødvendige.

# Parse command line parameters.
# sensor_args = { '11': Adafruit_DHT.DHT11,
#                 '22': Adafruit_DHT.DHT22,
#                 '2302': Adafruit_DHT.AM2302 }
# if len(sys.argv) == 3 and sys.argv[1] in sensor_args:
#     sensor = Adafruit_DHT.DHT11
#     pin = '22'
# else:
#     print('Usage: sudo ./Adafruit_DHT.py [11|22|2302] <GPIO pin number>')
#     print('Example: sudo ./Adafruit_DHT.py 2302 4 - Read from an AM2302 connected to GPIO pin #4')
#     sys.exit(1)

# Try to grab a sensor reading.  Use the read_retry method which will retry up
# to 15 times to get a sensor reading (waiting 2 seconds between each retry).

# Rigtig data.
# humidity, temperature = Adafruit_DHT.read_retry(sensor, pin)

# Dummy data.
humidity, temperature = 58.0, 28.0

# Un-comment the line below to convert the temperature to Fahrenheit.
# temperature = temperature * 9/5.0 + 32

# Note that sometimes you won't get a reading and
# the results will be null (because Linux can't
# guarantee the timing of calls to read the sensor).
# If this happens try again!
s = socket.socket()
print("Socket successfully created")

port = 12346

s.bind(('', port))
print("socket binded to %s" % port)

s.listen(5)
print("socket is listening")


def read_and_send():
    global humidity
    global temperature

    c, addr = s.accept()
    print("got connection from"), addr
    if humidity is not None and temperature is not None:
        message = ('{0:0.1f} {1:0.1f}'.format(temperature, humidity))

        print(message)
        c.sendall(message.encode('utf-8'))

        # Dummy data inkrementerer.
        humidity += 1.0
        temperature += 1.0
    else:
        print('Failed to get reading. Try again!')
        sys.exit(1)
    c.close()
    time.sleep(60)
    schedule()


def schedule():
    x = datetime.datetime.now()
    y = datetime.datetime.now().replace(microsecond=0, second=0, minute=0) + datetime.timedelta(hours=1)

    delta = y - x
    print(delta)
    print(delta.seconds)
    scheduler = sched.scheduler(time.time(), time.sleep)
    scheduler.enter(delta.seconds, 1, read_and_send())


schedule()
