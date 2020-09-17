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
import os
import base64
from cryptography.hazmat.primitives.ciphers import Cipher, algorithms, modes


# import Adafruit_DHT


# Try to grab a sensor reading.  Use the read_retry method which will retry up
# to 15 times to get a sensor reading (waiting 2 seconds between each retry).
# sensor = Adafruit_DHT.DHT11
# pin = '22'

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


def encrypt(key, string):
    string_as_bytes = string.encode('utf-8')

    block_size = 16

    if len(string_as_bytes) % block_size != 0:
        padding = block_size - len(string_as_bytes) % block_size
        string_as_bytes = string_as_bytes + (bytes([padding]) * padding)

    iv = os.urandom(block_size)
    cipher = Cipher(algorithms.AES(key), modes.CBC(iv))
    encryptor = cipher.encryptor()
    ct = encryptor.update(string_as_bytes) + encryptor.finalize()
    return base64.b64encode(iv+ct).decode('utf-8')


def read_and_send():
    global humidity
    global temperature
    # humidity, temperature = Adafruit_DHT.read_retry(sensor, pin)

    c, addr = s.accept()
    print("got connection from"), addr
    if humidity is not None and temperature is not None:
        message = ('{0:0.1f} {1:0.1f}'.format(temperature, humidity))

        key = b"franskhotdog1234"

        ct = encrypt(key, message)

        print("Encrypted text sent:")
        print(ct)

        c.sendall(ct.encode('utf-8'))

        # Dummy data inkrementerer.
        humidity += 1.0
        temperature += 1.0
    else:
        print('Failed to get reading. Try again!')
        sys.exit(1)
    c.close()
    # time.sleep(60)
    time.sleep(1)
    schedule()


def schedule():
    x = datetime.datetime.now()
    # y = datetime.datetime.now().replace(microsecond=0, second=0, minute = 0) + datetime.timedelta(hours=1)
    y = datetime.datetime.now().replace(microsecond=0, second=0) + datetime.timedelta(minutes=1)

    delta = y - x
    scheduler = sched.scheduler(time.time(), time.sleep)
    scheduler.enter(delta.seconds, 1, read_and_send())


read_and_send()
