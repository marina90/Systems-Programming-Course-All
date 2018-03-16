import sqlite3
import os
import sys

databaseexisted = os.path.isfile('cronhoteldb.db')
dbcon = sqlite3.connect('cronhoteldb.db')


def main(args):
    with dbcon:
        cursor = dbcon.cursor()
        if not databaseexisted:  # First time creating the database. Create the tables
            cursor.execute(
                "CREATE TABLE TaskTimes(TaskId INTEGER PRIMARY KEY NOT NULL , DoEvery INTEGER NOT NULL,NumTimes INTEGER NOT NULL)")  # create table TaskTimes
            cursor.execute(
                "CREATE TABLE Tasks(TaskId INTEGER PRIMARY KEY NOT NULL , TaskName TEXT NOT NULL,Parameter INTEGER, FOREIGN KEY(TaskId) REFERENCES TaskTimes(TaskId))")  # create table Tasks
            cursor.execute("CREATE TABLE Rooms(RoomNumber INTEGER PRIMARY KEY NOT NULL)")  # create table Rooms
            cursor.execute(
                "CREATE TABLE Residents(RoomNumber INTEGER NOT NULL, FirstName TEXT NOT NULL,LastName TEXT NOT NULL,FOREIGN KEY(RoomNumber) REFERENCES Rooms(RoomNumber))")  # create table Residents


    inputfilename = args[1]
    index = 0
    with open(inputfilename) as inputfile:
        for line in inputfile:

            if len(line.split(",")) == 4:
                if 'room' in line:
                    (room, num, first, last) = line.split(",")
                    cursor.execute("INSERT INTO Rooms (RoomNumber) VALUES(?)", (num,))
                    cursor.execute("INSERT INTO Residents(RoomNumber,FirstName,LastName) VALUES(?,?,?)",
                                   (int(num), str(first).strip(), str(last).strip()))
                else:
                    (task, doEvery, room, numTimes) = line.split(",")
                    cursor.execute("INSERT INTO TaskTimes (TaskId,DoEvery,NumTimes) VALUES(?,?,?)",
                                   (index, int(doEvery), int(numTimes)))
                    cursor.execute("INSERT INTO Tasks (TaskId,TaskName,Parameter) VALUES(?,?,?)",
                                   (index, str(task).strip(), int(room)))
                    index += 1



            elif len(line.split(",")) == 3:
                (task, doEvery, numTimes) = line.split(",")
                cursor.execute("INSERT INTO Tasks(TaskId,TaskName,Parameter) VALUES(?,?,?)", (index, str(task).strip(), 0))
                cursor.execute("INSERT INTO TaskTimes(TaskId,DoEvery,NumTimes) VALUES(?,?,?)",
                               (index, int(doEvery), int(numTimes)))
                index += 1


            elif len(line.split(",")) == 2:
                (room, num) = line.split(",")
                cursor.execute("INSERT INTO Rooms VALUES(?)", (int(num),))
    dbcon.commit()
    dbcon.close()


if __name__ == '__main__':
    main(sys.argv)
