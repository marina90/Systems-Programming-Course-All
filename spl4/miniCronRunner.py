import sqlite3
import time
import os
from hotelWorker import dohoteltask

DB_NAME = 'cronhoteldb.db'

db_exists = os.path.isfile(DB_NAME)
if db_exists:
    con = sqlite3.connect(DB_NAME)
    con.row_factory = sqlite3.Row
    cur = con.cursor()


class Task(object):
    def __init__(self, task_id, name, parameter, do_every, times):
        """
        :rtype: Task object with 6 parameters.
        """
        self.id = task_id
        self.name = name
        self.parameter = parameter
        self.do_every = do_every
        self._times = times
        self.last_execution_time = None

    @property
    def times(self):
        return self._times

    @times.setter
    def times(self, value):
        """
        Updates the times remaining
        :param value: New value
        """
        update_query = 'UPDATE TaskTimes SET NumTimes=:numTimes WHERE TaskId=:taskId'
        cur.execute(update_query, {'numTimes': value, 'taskId': self.id})
        con.commit()
        self._times = value


def get_all_tasks():
    """
    Returns all existing tasks as a sequence of objects
    :return: Sequence of objects, each with the following format
    id - Task Id
    name - Task name
    parameter - Task parameter
    do_every - Integer on once how many seconds must the task be performed
    times - How many times remaining to perform the task
    last_execution_time - Time object format. Initialized to None
    """
    SQL_query = "SELECT TaskTimes.TaskId,TaskName,Parameter,DoEvery,NumTimes FROM Tasks INNER JOIN TaskTimes ON " \
                "Tasks.TaskId=TaskTimes.TaskId "
    cur.execute(SQL_query)
    SQL_results = cur.fetchall()
    results = [Task(result['TaskId'], result['TaskName'], result['Parameter'], result['DoEvery'], result['NumTimes'])
               for result in SQL_results]
    return results


def should_run(task):
    """
    Checks if a task should run, based on prior execution time
    :param task: Task object
    :return: T/F
    """
    if not task.last_execution_time:
        return True
    seconds_diff = (time.time() - task.last_execution_time)
    if seconds_diff < task.do_every:
        return False
    return True


def main():
    if not db_exists:
        return
    possible_tasks = get_all_tasks()
    possible_tasks = filter(lambda task: task.times != 0, possible_tasks)

    while len(possible_tasks) != 0:
        # execute only the tasks whose time to run has come
        eligible_tasks = filter(should_run, possible_tasks)
        for to_execute in eligible_tasks:
            to_execute.last_execution_time = dohoteltask(to_execute.name, to_execute.parameter)
            to_execute.times -= 1

        # we'll remove from possible_tasks all the tasks who have finished their run (same objects as we just ran)
        possible_tasks = filter(lambda task: task.times != 0, possible_tasks)
        time.sleep(1)


if __name__ == "__main__":
    main()
