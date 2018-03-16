#include "TFTPController.h"
#include <iostream>
#include "Packet.h"
#include "utils.h"


TFTPController::TFTPController(const std::string& host, unsigned short port) : connection(host, port),
bDiscActivated(false), filenameBeingHandled(), expectedPacketNumber(0), ReadAckCallback(nullptr),
finishedReadingDataCallback(nullptr), accumulatorBuffer()
{
}

TFTPController::TFTPController(const TFTPController& other) : connection(other.connection),
bDiscActivated(other.bDiscActivated),
filenameBeingHandled(other.filenameBeingHandled),
expectedPacketNumber(other.expectedPacketNumber), ReadAckCallback(other.ReadAckCallback),
finishedReadingDataCallback(other.finishedReadingDataCallback), accumulatorBuffer(other.accumulatorBuffer)
{
}

TFTPController::~TFTPController()
{
}

bool TFTPController::login(const std::string& username)
{
	LoginRequestPacket loginRequest(username);
	if (!connection.sendPacket(loginRequest))
	{
		return false; //what to do?!
	}
	expectedPacketNumber = 0;
	return true;
}

bool TFTPController::DelFile(const std::string& filename)
{
	DeleteRequestPacket delRequest(filename);
	if (!connection.sendPacket(delRequest))
	{
		return false; //what to do?!
	}	
	expectedPacketNumber = 0;
	return true;
}

bool TFTPController::disconnect()
{
	DisconnectPacket discon;
	if (!connection.sendPacket(discon))
	{
		return false; //what to do?!
	}
	expectedPacketNumber = 0;
	bDiscActivated = true;
	return true;
}

bool TFTPController::WriteFile(const std::string& filename)
{
	filenameBeingHandled = filename;

	
	//Now build a set of packets, sending the file in chunks
	//First send the file notification
	ReadWritePacket WQR(PACKET_TYPES::WRQ, filename);
	ReadAckCallback = [this]() {
		std::ifstream * fileInput = new std::ifstream(filenameBeingHandled, std::ifstream::binary);
		if (!(*fileInput)) {
			delete fileInput;
			//failed to open
		}
		fileInput->exceptions(std::ifstream::goodbit); //no exceptions

		ReadAckCallback = [this,fileInput]() {
			this->sendNextDataPacket(fileInput);
		};
		sendNextDataPacket(fileInput);
	};

	expectedPacketNumber = 0; //start off the block game
	if (!connection.sendPacket(WQR))
	{
		return false; //what to do?!
	}
	return true;
}

bool TFTPController::ReadFile(const std::string& filename)
{
	ReadWritePacket RRQ(PACKET_TYPES::RRQ, filename);
	filenameBeingHandled = filename;
	finishedReadingDataCallback = [this](std::vector<char> result)
	{
		std::ofstream fileWriter(this->filenameBeingHandled, std::ofstream::out | std::ofstream::binary);
		fileWriter.write(
			result.data(), //get the array
			result.size() * sizeof(char));
		fileWriter.close();
		std::cout << "RRQ " << filenameBeingHandled << " complete" << std::endl;
	};
	expectedPacketNumber = 1; //start off the block game
	if (!connection.sendPacket(RRQ))
	{
		return false; //what to do?!
	}
	return true;
}

bool TFTPController::DirList()
{
	DirRequestPacket dirReq;

	finishedReadingDataCallback = [this](std::vector<char> result)
	{
		char * data = result.data();
		std::vector<std::string> fileStrings;
		rawSplit(data,result.size(), '\0', fileStrings);
		for (auto i : fileStrings) {
			std::cout << i << std::endl;
		}
	};
	expectedPacketNumber = 1; //start off the block game
	if (!connection.sendPacket(dirReq))
	{
		return false; //what to do?!
	}
	return true;
}

void TFTPController::handleDataPacket(DataPacket* pckt)
{
	AckPacket ackPckt(expectedPacketNumber);

	accumulatorBuffer.insert(accumulatorBuffer.end(),
		pckt->data,
		pckt->data + pckt->pcktSize);
	//if we inserted less than 512, we're done and can call callback
	if (DataPacket::MAXIMUM_DATA_PCKT_SIZE > pckt->pcktSize) {
		finishedReadingDataCallback(accumulatorBuffer);
		finishedReadingDataCallback = nullptr;
		expectedPacketNumber = 0; //reset
		accumulatorBuffer.clear();
	}
	else {
		expectedPacketNumber++;
	}

	connection.sendPacket(ackPckt);
}

void TFTPController::sendNextDataPacket(std::ifstream * fileInput)
{
	auto nextBlockNumber = ++expectedPacketNumber;
	//how much can we read
	char dataRead[DataPacket::MAXIMUM_DATA_PCKT_SIZE];
	fileInput->read(dataRead, sizeof(dataRead));
	std::streamsize count = fileInput->gcount();

	if (!(fileInput)) {
		delete fileInput;
		//do what what what
		return;
	}
	//maxed reading dataRead so can safely cast
	DataPacket dataPckt(static_cast<std::uint16_t>(count), nextBlockNumber, dataRead);
	//do we need another exact callback or a finishing move.
	if (count != DataPacket::MAXIMUM_DATA_PCKT_SIZE)
	{
		//finishing move
		//define printing callback
		ReadAckCallback = [this]()
		{
			ReadAckCallback = nullptr;
			expectedPacketNumber = 0;
			std::cout << "WRQ " << filenameBeingHandled << " complete" << std::endl;
		};
		fileInput->close();
		delete fileInput;
	}

	expectedPacketNumber = nextBlockNumber;
	connection.sendPacket(dataPckt);
}

void TFTPController::listen()
{
	do
	{
		Packet * readPacket = nullptr;
		if (!connection.readPacket(&readPacket)) {
			//failed reading from network
			return;
		}
		//check packet type
		switch (readPacket->opcode)
		{
		case PACKET_TYPES::ACK: {
			std::cout << (*readPacket) << " ";
			AckPacket * pckt = static_cast<AckPacket*>(readPacket);
			std::cout << pckt->blockNumber << std::endl;
			if (bDiscActivated) {
				//Check if it's the right ack
				if (expectedPacketNumber == pckt->blockNumber) {
					return; //we got the ACK for the disconnect
				} else
				{
					//we got a problem houston!
				}
			}
			if (ReadAckCallback &&
				(expectedPacketNumber == pckt->blockNumber)) {
				// Someone is waiting on a RRQ/WQR callback
				//if second condition fails, everything breaks
				ReadAckCallback();
			}
			break;
		}
		case PACKET_TYPES::ERRORmsg: {
			std::cout << (*readPacket) << " ";
			ErrorPacket * pckt = static_cast<ErrorPacket*>(readPacket);
			std::cout  << pckt->errorNumber << std::endl;
			//now need to cancel all ACK callbacks
			ReadAckCallback = nullptr;
			finishedReadingDataCallback = nullptr;
			expectedPacketNumber = 0;
			break;
		}
		case PACKET_TYPES::BCAST:
		{
			std::cout << (*readPacket) << " ";
			BCastPacket * pckt = static_cast<BCastPacket*>(readPacket);
			std::cout << pckt->messageType << " " << pckt->filename << std::endl;
			break;
		}
		case PACKET_TYPES::DATA:
		{
			DataPacket * pckt = static_cast<DataPacket*>(readPacket);
			if (expectedPacketNumber == pckt->blockNumber)
			{
				handleDataPacket(pckt);
			} else { //invalid pckt
			 //Do what??!
			}
			break;
		}
		default:
			{
				//we received a bad packet, send a message to the server.
			ErrorPacket err(ErrorTypes::IllegalTFTP, ErrorMessages[ErrorTypes::IllegalTFTP]);
			connection.sendPacket(err);
			break;
			}
			
		}
		delete readPacket;
	} while (true);

}
