#include "TFTPconnectionHandler.h"
#include <iostream>
#include "utils.h"

TFTPconnectionHandler::TFTPconnectionHandler(const std::string& host, unsigned short port) : ConnectionHandler(host, port)
{
}

TFTPconnectionHandler::TFTPconnectionHandler(const TFTPconnectionHandler& other) : ConnectionHandler(other)
{
}

bool TFTPconnectionHandler::sendPacket(const Packet & pcktToSend)
{
	auto toSend = pcktToSend.serialize();
	auto res = sendBytes(toSend, pcktToSend.packetSize());
	delete toSend;
	return res;
}

bool TFTPconnectionHandler::readPacket(Packet ** toRead)
{
	if (nullptr == toRead) { return false; }
	*toRead = nullptr;
	try {
		char opcodeBytes[2] = { 0 };
		//yuck C style cast
		if (!getBytes(opcodeBytes, sizeof(opcodeBytes)))
		{
			return false;
		}
		auto opcode = static_cast<PACKET_TYPES>(bytesToShort(opcodeBytes));
		switch (opcode)
		{
		case PACKET_TYPES::DISC: { //Nope, not gonna happen
			*toRead = new DisconnectPacket();
		}
		case PACKET_TYPES::RRQ: //Should never happen!
		case PACKET_TYPES::WRQ: {//should never happen as well!
			std::string fileName;
			if(!getFrameAscii(fileName, '\0'))
			{
				return false;
			}
			*toRead = new ReadWritePacket(opcode, fileName);
		}
		case PACKET_TYPES::LOGRQ: { //should also never happen!
			std::string userName;
			if (!getFrameAscii(userName, '\0'))
			{
				return false;
			}
			*toRead = new LoginRequestPacket(userName);
		}
		case PACKET_TYPES::DELRQ: {//should never happen as well!
			std::string fileName;
			if (!getFrameAscii(fileName, '\0'))
			{
				return false;
			}		
			*toRead = new DeleteRequestPacket(fileName);
			return true;
		}
		case PACKET_TYPES::DIRQ: { //Still shouldn't happen
			*toRead = new DirRequestPacket();
			return true;
		}
		case PACKET_TYPES::ACK: { //Now this is nice!
			char readBytes[2] = {0};
			if (!getBytes(readBytes, sizeof(readBytes)))
			{
				return false;
			}
			auto ackNumber = (bytesToShort(readBytes));
			*toRead = new AckPacket(ackNumber);
			return true;
		}
		case PACKET_TYPES::DATA: {
			char readBytes[2] = { 0 };
			if (!getBytes(readBytes, sizeof(readBytes)))
			{
				return false;
			}
			auto packetSize = (bytesToShort(readBytes));
			if (!getBytes(readBytes, sizeof(readBytes)))
			{
				return false;
			}
			auto blockNumber = (bytesToShort(readBytes));
			auto dataPkt = new char[packetSize];
			if (!getBytes(dataPkt, packetSize))
			{
				return false;
			}
			*toRead = new DataPacket(packetSize, blockNumber, dataPkt);
			delete[] dataPkt;
			return true;
		}
		case PACKET_TYPES::ERRORmsg: {
			char readBytes[2] = { 0 };
			if (!getBytes(readBytes, sizeof(readBytes)))
			{
				return false;
			}
			auto errorCode = static_cast<ErrorTypes>(bytesToShort(readBytes));
			std::string errorMessage;
			if (!getFrameAscii(errorMessage, '\0'))
			{
				return false;
			}
			*toRead = new ErrorPacket(errorCode, errorMessage);
			return true;
		}
		case PACKET_TYPES::BCAST: {
			auto bCastType = BCastType::ADDED;
			if (!getBytes(reinterpret_cast<char*>(&bCastType), sizeof(bCastType)))
			{
				return false;
			}
			std::string filename;
			if (!getFrameAscii(filename, '\0'))
			{
				return false;
			}
			*toRead = new BCastPacket(bCastType, filename);
			return true;
		}
		case PACKET_TYPES::INVALID:
		default:
			break;
		}
	}
	catch (std::exception& e) {
		std::cerr << "we failed recv or new (Error: " << e.what() << ')' << std::endl;
		return false;
	}
	return false;
}

TFTPconnectionHandler::~TFTPconnectionHandler()
{
}
