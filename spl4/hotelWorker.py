import sqlite3
import time
import os

DB_NAME = 'cronhoteldb.db'

db_exists = os.path.isfile(DB_NAME)
if db_exists:
    con = sqlite3.connect(DB_NAME)
    cur = con.cursor()


def read_room(room_id):
    """
    Reads a room definition
    :param room_id:
    :return: Sequence of firstname,lastname
    """
    SQL_query = "SELECT FirstName,LastName FROM Residents WHERE RoomNumber=:roomNumber"
    cur.execute(SQL_query, {"roomNumber": room_id})
    # should return only one piece of data
    return cur.fetchone()


def get_empty_rooms():
    """
    Finds all the empty rooms
    :return: Sequence of room IDs.
    """
    getAllEmptyRoomsQuery = 'SELECT RoomNumber FROM Rooms WHERE Rooms.RoomNumber NOT IN (SELECT RoomNumber FROM Residents) ORDER BY RoomNumber ASC'
    cur.execute(getAllEmptyRoomsQuery)
    # ugly flattening due to fetchall syntax returning a list of tuples
    cursor_results = [item for sublist in cur.fetchall() for item in sublist]
    return cursor_results


def wakeup(parameter):
    """
    Prints
    [firstname] [lastname] in room [roomnumber] received a wakeup call at [time]
    :param parameter:  RoomId
    :return:  time ti that this task has actually been done
    """
    firstName, lastName = read_room(parameter)
    curTime = time.time()
    print "{} {} in room {} received a wakeup call at {}".format(firstName, lastName, parameter, curTime)
    return curTime


def breakfast(parameter):
    """
        Prints
    [firstname] [lastname] in room [roomnumber] has been served breakfast at [time]
    :param parameter:  RoomId
    :return:  time ti that this task has actually been done
    """
    firstName, lastName = read_room(parameter)
    curTime = time.time()
    print "{} {} in room {} has been served breakfast at {}".format(firstName, lastName, parameter, curTime)
    return curTime


def clean(parameter=0):
    """
    Prints
    Rooms [roomNum1,...,roomNumk] were cleaned at [time]
    :param parameter: Parameter is ignored
    :return:  time ti that this task has actually been done
    """
    roomIds = get_empty_rooms()
    roomIdsString = [str(x) for x in roomIds]
    curTime = time.time()
    roomIdsToPrint = ", ".join(roomIdsString)
    print "Rooms {} were cleaned at {}".format(roomIdsToPrint, curTime)
    return curTime


missions = {
    "wakeup": wakeup,
    "breakfast": breakfast,
    "clean": clean
}


def dohoteltask(taskname, parameter):
    """
    Executes the specific tasks and returns when it was done
    :param taskname: Task to execute, may be wakeup,breakfast,lean
    :param parameter: Room to execute the task on
    :return: The time (time.time()) when the task was performed
    """
    if not db_exists:
        return  # nothing to do in this case
    return missions[taskname](parameter)
