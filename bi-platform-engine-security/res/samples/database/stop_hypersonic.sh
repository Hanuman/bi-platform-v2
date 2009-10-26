#!/bin/sh
java -cp lib/hsqldb-1.8.1.1.jar org.hsqldb.util.ShutdownServer -url "jdbc:hsqldb:hsql://localhost:9002/userdb" -user "sa" -password ""
