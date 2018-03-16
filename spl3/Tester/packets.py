from struct import pack, unpack


def readShort(s):
    data_size_bytes = s.recv(2)
    data = unpack(">H", data_size_bytes)[0]
    return data


def readString(s, delimiter='\0'):
    result = ""
    while (True):
        c = s.recv(1)
        if (c == delimiter):
            return result
        result += c


class DiscPacket(object):
    def encode(self):
        return pack(">H", 10)

    def __str__(self):
        return "DISC"

    @staticmethod
    def build():
        return DiscPacket()

    @staticmethod
    def decode(s):
        return DiscPacket()


class DirPacket(object):
    def encode(self):
        return pack(">H", 6)

    def __str__(self):
        return "DIRQ"

    @staticmethod
    def decode(s):
        return DirPacket()

    @staticmethod
    def build():
        return DirPacket()


class DataPacket(object):
    def __init__(self, size, block_num, data):
        self.size = size
        self.block_number = block_num
        self.data = data

    def __str__(self):
        return "Data Packet (size: %d, num: %d)" % (self.size, self.block_number)

    def encode(self):
        fmt_data = '%ds' % self.size
        fmt = ">HHH" + fmt_data
        return pack(fmt, 3, self.size, self.block_number, self.data)

    @staticmethod
    def decode(s):
        data_size = readShort(s)
        block_num = readShort(s)
        data = s.recv(data_size)
        return DataPacket(data_size, block_num, data)

    @staticmethod
    def build():
        #data_size = int(raw_input("enter data size:"))
        #block_num = int(raw_input("enter block number:"))
        #data = raw_input("enter data:")
        data = "abcd\x00aaaa\x00wasabi\0"
        data_size = len(data)
        block_num = 1
        return DataPacket(data_size, block_num, data)


class AckPacket(object):
    def __init__(self, num):
        self.num = num

    def __str__(self):
        return "ACK (num: %d)" % (self.num)

    def encode(self):
        fmt = ">HH"
        return pack(fmt, 4, self.num)

    @staticmethod
    def decode(s):
        ack_number = readShort(s)
        return AckPacket(ack_number)

    @staticmethod
    def build():
        block_num = int(raw_input("enter block number:"))
        return AckPacket(block_num)


class BCastPacket(object):
    def __init__(self, bcast_type, name):
        self.type = bcast_type
        self.name = name

    def __str__(self):
        res = "del" if (0 == self.type) else "add"
        return "BCast (type: %B, name: %s)" % (res, self.name)

    def encode(self):
        fmt = ">HB%ds" % len(self.name)
        return pack(fmt, 9, self.type, self.name.encode('utf-8')) + '\0'

    @staticmethod
    def decode(s):
        bcst_type = s.recv(1)
        bcast_name = readString(s)
        return BCastPacket(bcst_type, bcast_name)

    @staticmethod
    def build():
        bcst_type = int(raw_input("enter bcast type in 0/1:"))
        bcast_name = (raw_input("enter bcast filename:"))
        return BCastPacket(bcst_type, bcast_name)


class ErrorPacket(object):
    def __init__(self, error_type, name):
        self.type = error_type
        self.name = name

    def __str__(self):
        return "ERROR (type: %d, name: %s)" % (self.type, self.name)

    def encode(self):
        fmt = ">HH%ds" % len(self.name)
        return pack(fmt, 5, self.type, self.name.encode('utf-8')) + '\0'

    @staticmethod
    def decode(s):
        err_type = readShort(s)
        err_msg = readString(s)
        return ErrorPacket(err_type, err_msg)

    @staticmethod
    def build():
        error_type = int(raw_input("enter error type in int:"))
        error_str = (raw_input("enter error:"))
        return ErrorPacket(error_type, error_str)


class FilePacket(object):
    def __init__(self, name, opcode=0):
        self.name = name
        self.opcode = opcode

    def __str__(self):
        return "(name: %s)" % (self.name)

    def encode(self):
        fmt = ">H%ds"
        return pack(fmt % len(self.name), self.opcode, self.name.encode('utf-8')) + '\0'

    @staticmethod
    def decode(s, opcode=0, cls=None):
        filename = readString(s)
        return cls(filename)


class LoginPacket(FilePacket):
    def __init__(self, name):
        super(LoginPacket, self).__init__(name, 7)

    def __str__(self):
        return "LOGRQ" + super(LoginPacket, self).__str__()

    @staticmethod
    def decode(s):
        return FilePacket.decode(s, 7, LoginPacket)

    @staticmethod
    def build():
        filename = (raw_input("enter filename:"))
        return LoginPacket(filename)


class DelPacket(FilePacket):
    def __init__(self, name):
        super(DelPacket, self).__init__(name, 8)

    def __str__(self):
        return "DELRQ" + super(DelPacket, self).__str__()

    @staticmethod
    def decode(s):
        return FilePacket.decode(s, 8, DelPacket)

    @staticmethod
    def build():
        filename = (raw_input("enter filename:"))
        return LoginPacket(filename)


class RRQPacket(FilePacket):
    def __init__(self, name):
        super(RRQPacket, self).__init__(name, 1)

    def __str__(self):
        return "RRQ" + super(RRQPacket, self).__str__()

    @staticmethod
    def decode(s):
        return FilePacket.decode(s, 1, RRQPacket)

    @staticmethod
    def build():
        filename = (raw_input("enter filename:"))
        return LoginPacket(filename)


class WRQPacket(FilePacket):
    def __init__(self, name):
        super(WRQPacket, self).__init__(name, 2)

    def __str__(self):
        return "WQR" + super(WRQPacket, self).__str__()

    @staticmethod
    def decode(s):
        return FilePacket.decode(s, 2, WRQPacket)

    @staticmethod
    def build():
        filename = (raw_input("enter filename:"))
        return LoginPacket(filename)


PacketFromOpcodeFactory = {
    1: RRQPacket.decode,
    2: WRQPacket.decode,
    3: DataPacket.decode,
    4: AckPacket.decode,
    5: ErrorPacket.decode,
    6: DirPacket.decode,
    7: LoginPacket.decode,
    8: DelPacket.decode,
    9: BCastPacket.decode,
    10: DiscPacket.decode,
}

PacketFromStringFacotry = {
    'RRQ': RRQPacket.build,
    'WRQ': WRQPacket.build,
    'DATA': DataPacket.build,
    'ACK': AckPacket.build,
    'ERROR': ErrorPacket.build,
    'DIRQ': DirPacket.build,
    'LOGRQ': LoginPacket.build,
    'DELRQ': DelPacket.build,
    'BCAST': BCastPacket.build,
    'DISC': DiscPacket.build,
}


def packetFromSocket(s):
    opcode = readShort(s)
    return PacketFromOpcodeFactory[opcode](s)
