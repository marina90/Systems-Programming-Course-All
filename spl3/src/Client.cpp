#include <iostream>
#include <vector>
#include <boost/thread.hpp>  
#include "utils.h"
#include "Packet.h"
#include "TFTPController.h"


int main(int argc, char *argv[]) {
	if (argc < 3) {
		std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
		return -1;
	}
	std::string host = argv[1];
	unsigned short port = static_cast<unsigned short>(atoi(argv[2]));

	TFTPController controller(host, port);
	if (!controller.connect()) {
		std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
		return 1;
	}

	boost::thread th1(std::ref(controller));

	auto shouldKill = false;
	while (!shouldKill) {
		std::string line;
		std::getline(std::cin, line);
		line = trim(line);
		if (0 == line.length()) { continue; } //blank line

		std::vector<std::string> command_strings;
		stringSplit(line, ' ', command_strings);
		if (0 == command_strings.size()) { continue; }

		auto command = command_strings.at(0); //get the command
		auto commandType = stringToCommand(command);
		auto res = false;
		switch (commandType)
		{
		case PACKET_TYPES::DISC:
		{
			res = controller.disconnect();
			shouldKill = true;
			break;

		}
		case PACKET_TYPES::LOGRQ:
		{
			if (2 != command_strings.size())
			{
				//bad input, wtf?
				continue;
			}
			auto userName = command_strings.at(1);
			res = controller.login(userName);
			break;

		}
		case PACKET_TYPES::DELRQ: {
			if (2 != command_strings.size())
			{
				//bad input, wtf?
				continue;
			}
			auto fileName = command_strings.at(1);
			res = controller.DelFile(fileName);
			break;

		}

		case PACKET_TYPES::RRQ: {
			if (2 != command_strings.size())
			{
				//bad input, wtf?
				continue;
			}
			auto fileName = command_strings.at(1);
			res = controller.ReadFile(fileName);
			break;

		}
		case PACKET_TYPES::WRQ: {
			if (2 != command_strings.size())
			{
				//bad input, wtf?
				continue;
			}
			auto fileName = command_strings.at(1);
			res = controller.WriteFile(fileName);
			break;
		}
		case PACKET_TYPES::DIRQ: {
			res = controller.DirList();
			break;
		}

		default:
		{
			//wtf
			//try again
		}
		}
		if (!res)
		{
			//should print something?
			break;
		}

	}

	th1.join();

	controller.close();

	return 0;
}
