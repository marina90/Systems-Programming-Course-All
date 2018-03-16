#include <cstring>
#include "Packet.h"

std::string name = "AcePace";

bool checkLogRqPacket()
{
	char expectedResult[] = { 0x00,0x07, 'A','c','e','P','a','c','e', '\0' };
	LoginRequestPacket reqPacket(name);
	const char * result = reqPacket.serialize();
	auto res = !memcmp(expectedResult, result, sizeof(expectedResult));
	delete[] result;
	return res;
}

bool checkReadPacket()
{
	char expectedResult[] = { 0x00,0x01, 'A','c','e','P','a','c','e', '\0' };
	ReadWritePacket reqPacket(PACKET_TYPES::RRQ, name);
	const char * result = reqPacket.serialize();
	auto res = !memcmp(expectedResult, result, sizeof(expectedResult));
	delete[] result;
	return res;
}

bool checkWritePacket()
{
	char expectedResult[] = { 0x00,0x08, 'A','c','e','P','a','c','e', '\0' };
	DeleteRequestPacket reqPacket(name);
	const char * result = reqPacket.serialize();
	auto res = !memcmp(expectedResult, result, sizeof(expectedResult));
	delete[] result;
	return res;
}

bool checkDeletePacket()
{
	char expectedResult[] = { 0x00,0x02, 'A','c','e','P','a','c','e', '\0' };
	ReadWritePacket reqPacket(PACKET_TYPES::WRQ, name);
	const char * result = reqPacket.serialize();
	auto res = !memcmp(expectedResult, result, sizeof(expectedResult));
	delete[] result;
	return res;
}

bool checkDataPacket()
{
	char expectedResult[] = { 0x00,0x03, 0x00,0x7,0x00,0x05, 'A','c','e','P','a','c','e', };
	DataPacket reqPacket(7, 5, "AcePace");
	const char * result = reqPacket.serialize();
	auto res = !memcmp(expectedResult, result, sizeof(expectedResult));
	delete[] result;
	return res;
}

bool checkAckPacket()
{
	char expectedResult[] = { 0x00,0x04, 0x00,0x7 };
	AckPacket reqPacket(7);
	const char * result = reqPacket.serialize();
	auto res = !memcmp(expectedResult, result, sizeof(expectedResult));
	delete[] result;
	return res;
}

bool checkBcastPacket()
{
	char expectedResult[] = { 0x00,0x09, 0x00, 'A','c','e','P','a','c','e', '\0' };
	BCastPacket reqPacket(BCastType::DELETED, name);
	const char * result = reqPacket.serialize();
	auto res = !memcmp(expectedResult, result, sizeof(expectedResult));
	delete[] result;
	return res;
}

bool checkErrorPacket()
{
	char expectedResult[] = { 0x00,0x05, 0x00,0x06, 'A','c','e','P','a','c','e', '\0' };
	ErrorPacket reqPacket(ErrorTypes::UserNotLoggedOn, name);
	const char * result = reqPacket.serialize();
	auto res = !memcmp(expectedResult, result, sizeof(expectedResult));
	delete[] result;
	return res;
}


bool testSerialization()
{
	auto res = true;

	res &= checkLogRqPacket();

	res &= checkReadPacket();

	res &= checkWritePacket();

	res &= checkDeletePacket();

	res &= checkDataPacket();

	res &= checkAckPacket();

	res &= checkBcastPacket();

	res &= checkErrorPacket();

	return res;
}

bool testDeserialization()
{
	auto res = true;

	return res;
}