#ifndef __UTILS__
#define __UTILS__
#include <string>
#include <vector>

inline short bytesToShort(char* bytesArr)
{
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}

inline void shortToBytes(short num, char* bytesArr)
{
	bytesArr[0] = ((num >> 8) & 0xFF);
	bytesArr[1] = (num & 0xFF);
}


std::string trim(const std::string &s);

void stringSplit(const std::string &s, char delim, std::vector<std::string> &elems);

void rawSplit(const char * data, size_t size, char delim, std::vector<std::string> &elems);

#endif // !__UTILS__
