#include <connectionHandler.h>
#include <iostream>
using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

ConnectionHandler::ConnectionHandler(const string& host, unsigned short port) : host_(host), port_(port),
io_service_(), socket_(new tcp::socket(io_service_))
{}

ConnectionHandler::ConnectionHandler(const ConnectionHandler& other) :
	host_(other.host_), port_(other.port_),
	io_service_(), socket_(other.socket_)
{
}

ConnectionHandler::~ConnectionHandler() {
	//close();
}

bool ConnectionHandler::connect() {
	try {
		tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
		boost::system::error_code error;
		socket_->connect(endpoint, error);
		if (error) {
			throw boost::system::system_error(error);
		}
	}
	catch (std::exception& e) {
		std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
		return false;
	}
	return true;
}

bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
	boost::system::error_code error;
	try {
		size_t tmp = 0;
		while (!error && bytesToRead > tmp) {
			tmp += socket_->read_some(boost::asio::buffer(bytes + tmp, bytesToRead - tmp), error);
		}
		if (error) {
			throw boost::system::system_error(error);
		}
	}
	catch (std::exception& e) {
		std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
		return false;
	}
	return true;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
	boost::system::error_code error;
	try {
		int tmp = 0;
		while (!error && bytesToWrite > tmp) {
			tmp += socket_->write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
		}
		if (error) {
			throw boost::system::system_error(error);
		}
	}
	catch (std::exception& e) {
		std::cerr << "send failed (Error: " << e.what() << ')' << std::endl;
		return false;
	}
	return true;
}

bool ConnectionHandler::getLine(std::string& line) {
	return getFrameAscii(line, '\n');
}

bool ConnectionHandler::sendLine(const std::string& line) {
	return sendFrameAscii(line, '\n');
}

bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
	char ch;
	// Stop when we encounter the null character. 
	// Notice that the null character is not appended to the frame string.
	try {
		do {
			if (!getBytes(&ch, 1))	{
				return false;
			}
			frame.append(1, ch);
		} while (delimiter != ch);
	}
	catch (std::exception& e) {
		std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
		return false;
	}
	return true;
}

bool ConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
	bool result = sendBytes(frame.c_str(), frame.length());
	if (!result) return false;
	return sendBytes(&delimiter, 1);
}

// Close down the connection properly.
void ConnectionHandler::close() {
	try {
		socket_->close();
		std::cerr << "Closing socket" << std::endl;
	}
	catch (...) {
		std::cerr << "closing failed: connection already closed" << std::endl;
	}
}