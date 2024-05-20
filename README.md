Cron Expression Parser - Homework
------------------------------------
Task description: [TASK.md](./TASK.md)

Cron man page: https://man7.org/linux/man-pages/man5/crontab.5.html 

Building
----------
Just run:

    mvn clean package

Running:

    java -jar cron-parser.jar "*/15 0 1,15 * 1-5 /usr/bin/find"

Author's comments:
------------------
1. Tried to keep simple, but it has grown while writing...
    Now I would split create separate parser class (extending an common abstract) for each field (minute, hour...).
    In such way it will be easier to test parsing particular fields - current approach is rather integration testing.
    There are private methods (applyDivisor, expandRanges) which should be tested directly.
2. Many comments in code. TODO's left intentionally - a place to improvement



