PROGRAM = TFTPclient

SRCS = utils.cpp \
	connectionHandler.cpp \
	Packet.cpp \
	TFTPconnectionHandler.cpp \
	TFTPController.cpp \
	Client.cpp

SRCDIR = src
BINDIR = bin

OBJS = $(SRCS:%.cpp=$(BINDIR)/%.o)

CC = g++
INCLUDES = -I./include
CFLAGS:=-c -Wall -Weffc++ -g -std=c++11
LDFLAGS:= -lboost_system -lboost_locale -lboost_thread

.PHONY: all
all: $(PROGRAM)

$(PROGRAM): $(OBJS)
	$(CC) -o $(BINDIR)/$(PROGRAM) $+ $(LDFLAGS)

$(BINDIR)/%.o: $(SRCDIR)/%.cpp
	@mkdir -p $(BINDIR)
	$(CC) $(CFLAGS) $(INCLUDES) -o $@ $<

.PHONY: clean
clean:
	rm -f $(BINDIR)/*
