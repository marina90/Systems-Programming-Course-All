#ifndef __TFTP_CONNECTION_HANDLER__
#define __TFTP_CONNECTION_HANDLER__
#include <string>
#include "connectionHandler.h"
#include "Packet.h"

class TFTPconnectionHandler : public ConnectionHandler
{
public:
	TFTPconnectionHandler(const std::string& host, unsigned short port);
	TFTPconnectionHandler(const TFTPconnectionHandler& other);
	/**
	 * Sends a generic packet serialized over the network
	 */
	bool sendPacket(const Packet& toSend);

	/**
	 * Reads a serialized packet from the network.
	 * The reason for this horrid interface is we want the ability to signal
	 * success or failure without using exceptions.
	 * so our return value must be a ptr to ptr. Since we can't pass in a generic packet.
	 */
	bool readPacket(Packet** toRead);

	virtual ~TFTPconnectionHandler();
};
#endif
