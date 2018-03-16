# Echo server program
import socket
import sys
from packets import *

HOST = ""  # Symbolic name meaning all available interfaces
PORT = 4150  # Arbitrary non-privileged port
s = None

try:
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
except socket.error as msg:
    s = None
try:
    s.bind((HOST, PORT))
    s.listen(1)
except socket.error as msg:
    s.close()
    s = None
if s is None:
    print 'could not open socket'
    sys.exit(1)


while 1:
    conn, addr = s.accept()
    print 'Connected by', addr
    try:
        while 1:
            pckt = packetFromSocket(conn)
            print pckt
            print
            pckt_type = raw_input("enter reply type")
            pckt_type = pckt_type.strip()
            pckt_type = pckt_type.upper()
            reply = PacketFromStringFacotry[pckt_type]()
            reply_encoded = reply.encode()
            conn.send(reply_encoded)
    except:
        continue