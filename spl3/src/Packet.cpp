#include <cstring>
#include <algorithm>
#include "Packet.h"
#include "utils.h"

using namespace std;

Packet::Packet() : Packet(PACKET_TYPES::INVALID) {}

Packet::Packet(PACKET_TYPES opcode) : opcode(opcode) {}

PACKET_TYPES stringToCommand(const std::string& command)
{
	auto key = PACKET_TYPES::INVALID;
	for (auto const& entry : map_PacketTypeToString)
	{
		if (command == entry.second)
		{
			key = entry.first;
			break;
		}
	}
	return key;
}

const char * Packet::serializePacket() const
{
	auto toSend = allocatePacket();
	shortToBytes(static_cast<short>(opcode), toSend);
	return toSend;
}

Packet::~Packet() {}

SingleStringPacket::SingleStringPacket(PACKET_TYPES type, const std::string& str) : Packet(type), someStr(str)
{
}

const char * SingleStringPacket::serialize() const
{
	auto toSend = allocatePacket();
	auto currentPtr = toSend;
	shortToBytes(static_cast<short>(opcode), toSend);
	currentPtr += sizeof(short);
	strncpy(currentPtr, someStr.c_str(), packetSize() - getOpcodeSize());
	return toSend;
}

size_t SingleStringPacket::packetSize() const
{
	return getOpcodeSize() + someStr.size() + 1; //+1 for null terminator
}


LoginRequestPacket::LoginRequestPacket(const std::string& loginUsername) : SingleStringPacket(PACKET_TYPES::LOGRQ, loginUsername)
{}

DeleteRequestPacket::DeleteRequestPacket(const std::string& filenameToDelete) : SingleStringPacket(PACKET_TYPES::DELRQ, filenameToDelete)
{}

ReadWritePacket::ReadWritePacket(PACKET_TYPES type, const std::string& fileToMove) : SingleStringPacket(type, fileToMove)
{}


AckPacket::AckPacket(unsigned short block) : Packet(PACKET_TYPES::ACK), blockNumber(block)
{}

const char * AckPacket::serialize() const
{
	auto toSend = allocatePacket();
	auto currentPtr = toSend;
	shortToBytes(static_cast<short>(opcode), toSend);
	currentPtr += sizeof(short);
	shortToBytes(static_cast<std::uint16_t>(blockNumber), currentPtr);
	return toSend;
}

size_t AckPacket::packetSize() const
{
	return getOpcodeSize() + sizeof(blockNumber);
}

std::ostream& operator<<(std::ostream& os, ErrorTypes al)
{
	return os << static_cast<std::uint16_t>(al);
}

ErrorPacket::ErrorPacket(ErrorTypes type, const std::string& message) : Packet(PACKET_TYPES::ERRORmsg), errorNumber(type), errorMessage(message)
{
}

const char * ErrorPacket::serialize() const
{
	auto toSend = allocatePacket();
	auto currentPtr = toSend;
	shortToBytes(static_cast<short>(opcode), toSend);
	currentPtr += sizeof(short);
	shortToBytes(static_cast<std::uint16_t>(errorNumber),
		currentPtr);
	currentPtr += sizeof(short);
	strncpy(currentPtr, errorMessage.c_str(),
		packetSize() - getOpcodeSize() - sizeof(std::uint16_t));
	return toSend;
}

size_t ErrorPacket::packetSize() const
{
	return getOpcodeSize() + sizeof(ErrorTypes) + errorMessage.size() + 1;
}

std::ostream& operator<<(std::ostream& os, BCastType al)
{
	switch (al)
	{
	case BCastType::DELETED:
	{
		return os << "del";
	}
	case BCastType::ADDED:
	{
		return os << "add";
	}
	default:
	{
		return os;
	}
	}
}

BCastPacket::BCastPacket(BCastType messageType, const std::string& fileChanged) : Packet(PACKET_TYPES::BCAST), messageType(messageType), filename(fileChanged)
{

}

const char * BCastPacket::serialize() const
{
	auto toSend = allocatePacket();
	auto currentPtr = toSend;
	shortToBytes(static_cast<short>(opcode), toSend);

	currentPtr += sizeof(short);
	currentPtr[0] = static_cast<std::uint8_t>(messageType);
	currentPtr += sizeof(messageType);
	strncpy(currentPtr, filename.c_str(),
		packetSize() - getOpcodeSize() - sizeof(std::uint16_t));
	return toSend;
}

size_t BCastPacket::packetSize() const
{
	return getOpcodeSize() + sizeof(messageType) + filename.size() + 1;
}


DataPacket::DataPacket(std::uint16_t size, std::uint16_t blckNumber, const void* dataPkt) : Packet(PACKET_TYPES::DATA), pcktSize(size), blockNumber(blckNumber), data(new std::uint8_t[size])
{
	memcpy(data, dataPkt, size);
}

DataPacket::DataPacket(const DataPacket & other) : DataPacket(other.pcktSize, other.blockNumber, other.data)
{

}

DataPacket & DataPacket::operator=(const DataPacket & other)
{
	opcode = other.opcode;
	pcktSize = other.pcktSize;
	blockNumber = other.blockNumber;

	std::uint8_t * new_data = new std::uint8_t[pcktSize]();
	std::copy_n(other.data, pcktSize, new_data);
	delete data;
	data = new_data;

	return *this;
}

const char * DataPacket::serialize() const
{
	auto toSend = allocatePacket();
	auto currentPtr = toSend;
	shortToBytes(static_cast<short>(opcode), toSend);
	currentPtr += sizeof(short);
	shortToBytes(static_cast<std::uint16_t>(pcktSize),
		currentPtr);
	currentPtr += sizeof(short);
	shortToBytes(static_cast<std::uint16_t>(blockNumber),
		currentPtr);
	currentPtr += sizeof(short);
	memcpy(currentPtr,
		data, pcktSize);
	return toSend;
}

size_t DataPacket::packetSize() const
{
	return getOpcodeSize() + sizeof(pcktSize) + sizeof(blockNumber) + pcktSize;
}

DataPacket::~DataPacket()
{
	delete data;
	data = nullptr;
}

Packet * deserializePacket(const char * networkBuffer)
{
	auto opcode = static_cast<PACKET_TYPES>(bytesToShort(const_cast<char*>(networkBuffer)));
	auto currentReadPoint = networkBuffer + sizeof(short);

	switch (opcode)
	{

	case PACKET_TYPES::RRQ:
	case PACKET_TYPES::WRQ: {
		std::string filename(currentReadPoint);
		return new ReadWritePacket(opcode, filename);
	}
	case PACKET_TYPES::DATA:
		return new DataPacket(bytesToShort(const_cast<char*>(currentReadPoint)),
			bytesToShort(const_cast<char*>(currentReadPoint + sizeof(short))),
			currentReadPoint + sizeof(short) * 2);
	case PACKET_TYPES::ACK: {
		return new AckPacket(bytesToShort(const_cast<char*>(currentReadPoint)));
	}
	case PACKET_TYPES::ERRORmsg: {
		ErrorTypes errorCode = static_cast<ErrorTypes>(bytesToShort(const_cast<char*>(currentReadPoint)));
		currentReadPoint += sizeof(short);
		std::string errorMessage(currentReadPoint);
		return new ErrorPacket(errorCode,
			errorMessage);
	}
	case PACKET_TYPES::DIRQ: {
		return new DisconnectPacket();
	}
	case PACKET_TYPES::LOGRQ: {
		std::string fileName(currentReadPoint);
		return new LoginRequestPacket(fileName);
	}
	case PACKET_TYPES::DELRQ: {
		std::string fileName(currentReadPoint);
		return new DeleteRequestPacket(fileName);
	}
	case PACKET_TYPES::BCAST: {
		BCastType errorCode = static_cast<BCastType>(*(currentReadPoint));
		currentReadPoint += sizeof(errorCode);
		std::string fileName(currentReadPoint);
		return new BCastPacket(errorCode,
			fileName);
	}
	case PACKET_TYPES::DISC: {
		return new DisconnectPacket();
	}
	case PACKET_TYPES::INVALID:
	default:
		break;
	}

	return nullptr;
}