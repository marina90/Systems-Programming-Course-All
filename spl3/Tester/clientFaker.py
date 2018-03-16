# Echo server program
import socket
import sys
from packets import *

HOST = '10.0.0.57'  # Symbolic name meaning all available interfaces
PORT = 4150  # Arbitrary non-privileged port
s = None

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((HOST,PORT))
while 1:
    pckt_type = raw_input("enter pckt type")
    pckt_type = pckt_type.strip()
    pckt_type = pckt_type.upper()
    msg = PacketFromStringFacotry[pckt_type]()
    msg_encoded = msg.encode()
    s.send(msg_encoded)
    pckt = packetFromSocket(s)
    print pckt
    print
