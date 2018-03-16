#include "utils.h"
#include <algorithm>
#include <cctype>
#include <sstream>

std::string trim(const std::string &s)
{
	auto wsfront = std::find_if_not(s.begin(), s.end(), [](int c) {return std::isspace(c); });
	auto wsback = std::find_if_not(s.rbegin(), s.rend(), [](int c) {return std::isspace(c); }).base();
	return (wsback <= wsfront ? std::string() : std::string(wsfront, wsback));
}

void stringSplit(const std::string &s, char delim, std::vector<std::string> &elems) {
	std::stringstream stringStream(s);
	std::string item;
	while (std::getline(stringStream, item, delim)) {
		elems.push_back(item);
	}
}

void rawSplit(const char* data, size_t size, char delim, std::vector<std::string>& elems)
{
	size_t lastMarker = 0;
	for (size_t i = 0; i < size; i++)
	{
		if ((*(data+i))== delim)
		{
			std::string newElement(data + lastMarker, i-lastMarker);
			lastMarker = i+1;
			elems.push_back(newElement);
		}
	}
}
