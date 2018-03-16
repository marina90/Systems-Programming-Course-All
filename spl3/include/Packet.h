#ifndef PACKET_PARSER__
#define PACKET_PARSER__
#include <string>
#include <cstdint>
#include <ostream>
#include <map>

enum class PACKET_TYPES : std::uint16_t
{
	RRQ=1, // Read Request 
	WRQ, //Write Request
	DATA, //Data
	ACK, //Acknowledgement
	ERRORmsg, //exception due to C++
	DIRQ, //Directory Listing Request 
	LOGRQ, // Login Request
	DELRQ, //Delete Request
	BCAST, //Broadcast
	DISC, //Disconnect
	INVALID
};

PACKET_TYPES stringToCommand(const std::string&);

typedef std::map<PACKET_TYPES, const std::string> pcktTypeToString_type;

static pcktTypeToString_type map_PacketTypeToString{
	{ PACKET_TYPES::LOGRQ,"LOGRQ"},
	{ PACKET_TYPES::DELRQ,"DELRQ"},
	{ PACKET_TYPES::RRQ,"RRQ"},
	{ PACKET_TYPES::WRQ,"WRQ"},
	{ PACKET_TYPES::DIRQ,"DIRQ"},
	{ PACKET_TYPES::ACK,"ACK"},
	{ PACKET_TYPES::BCAST,"BCAST"},
	{ PACKET_TYPES::ERRORmsg,"Error"},
	{ PACKET_TYPES::DISC,"DISC"},
	{ PACKET_TYPES::INVALID,"INVALID"},
};


class Packet
{
protected:

	size_t getOpcodeSize() const
	{
		return sizeof(opcode);
	}

	/**
	 * \brief Allocates a packet of the appropiate overloaded size
	 * \return A new buffer that *must* be freed
	 */
	char * allocatePacket() const
	{
		return new char[packetSize()];
	}

	const char * serializePacket() const;



public:
	Packet();

	explicit Packet(PACKET_TYPES opcode);

	virtual const char * serialize() const = 0;

	virtual size_t packetSize() const {
		return getOpcodeSize();
	}

	PACKET_TYPES opcode;

	friend std::ostream& operator<<(std::ostream& os, const Packet& pckt) {
		os << map_PacketTypeToString[pckt.opcode];
		return os;
	}

	virtual ~Packet();
};

class DirRequestPacket : public Packet {
public:
	DirRequestPacket() : Packet(PACKET_TYPES::DIRQ) {}

	const char * serialize() const override {
		return serializePacket();
	}


};

class DisconnectPacket : public Packet {
public:
	DisconnectPacket() : Packet(PACKET_TYPES::DISC) {}
	const char * serialize() const override {
		return serializePacket();
	}
};

class SingleStringPacket : public Packet {

protected:
	SingleStringPacket(PACKET_TYPES type, const std::string& str);
public:
	std::string someStr;
	const char * serialize() const override;

	virtual size_t packetSize() const override;
};

class LoginRequestPacket : public SingleStringPacket {
public:
	LoginRequestPacket(const std::string& loginUsername);
};

class DeleteRequestPacket : public SingleStringPacket {

public:
	DeleteRequestPacket(const std::string& filenameToDelete);
};

class ReadWritePacket : public SingleStringPacket {

public:
	ReadWritePacket(PACKET_TYPES type, const std::string& fileToMove);
};

class DataPacket : public Packet { //Special type of packet!
public:
	static const size_t MAXIMUM_DATA_PCKT_SIZE = 512;

	std::uint16_t pcktSize; // Max is 512, protocol defined
	std::uint16_t blockNumber;
	std::uint8_t* data;
	//WARNING, CTOR MIGHT THROW
	DataPacket(std::uint16_t size, std::uint16_t blckNumber, const void * dataPkt);

	DataPacket(const DataPacket& other); //CopyCtor
	DataPacket& operator=(const DataPacket& other); //CopyAssignment

	const char * serialize() const override;

	virtual size_t packetSize() const override;

	virtual ~DataPacket();
};

class AckPacket : public Packet {
public:
	std::uint16_t blockNumber;

	explicit AckPacket(unsigned short block);

	const char * serialize() const override;

	virtual size_t packetSize() const override;
};

enum class ErrorTypes : std::uint16_t
{
	UnDef,
	FileNotFound,
	AccessViolation,
	DiskFull,
	IllegalTFTP,
	FileAlreadyExists,
	UserNotLoggedOn,
	UserAlreadyLoggedIn,
	UNKNOWN,
	FAKE_SUCESS //used to simplify my life, not a real error code
};

std::ostream& operator<<(std::ostream& os, ErrorTypes al);


static std::map<ErrorTypes, const std::string> ErrorMessages{
	{ ErrorTypes::UnDef,"Not defined, see error message (if any)."},
	{ ErrorTypes::FileNotFound,"File not found – RRQ of non-existing file" },
	{ ErrorTypes::AccessViolation,"Access violation – File cannot be written, read or deleted" },
	{ ErrorTypes::DiskFull,"Disk full or allocation exceeded – No room in disk." },
	{ ErrorTypes::IllegalTFTP,"Illegal TFTP operation – Unknown Opcode." },
	{ ErrorTypes::FileAlreadyExists,"File already exists – File name exists on WRQ." },
	{ ErrorTypes::UserNotLoggedOn,"User not logged in – Any opcode received before Login completes." },
	{ ErrorTypes::UserAlreadyLoggedIn,"User already logged in – Login username already connected." }
};

class ErrorPacket : public Packet {
public:
	ErrorTypes errorNumber;
	std::string errorMessage;

	ErrorPacket(ErrorTypes type, const std::string& message);

	virtual const char * serialize() const override;

	virtual size_t packetSize() const override;
};

enum class BCastType : std::uint8_t {
	DELETED,
	ADDED
};

std::ostream& operator<<(std::ostream& os, BCastType al);


class BCastPacket : public Packet {
public:
	BCastType messageType;
	std::string filename;
	BCastPacket(BCastType messageType, const std::string& fileChanged);

	virtual const char * serialize() const override;

	virtual size_t packetSize() const override;
};

Packet * deserializePacket(const char * networkBuffer);

#endif
