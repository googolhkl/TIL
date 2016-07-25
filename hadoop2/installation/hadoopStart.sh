#!/bin/bash

ssh zookeeper@googolhkl1 "/home/zookeeper/zookeeper-3.4.6/bin/zkServer.sh start"
ssh zookeeper@googolhkl2 "/home/zookeeper/zookeeper-3.4.6/bin/zkServer.sh start"
ssh zookeeper@googolhkl3 "/home/zookeeper/zookeeper-3.4.6/bin/zkServer.sh start"

/home/hkl/hadoop-2.6.0/bin/hdfs zkfc -formatZK 

ssh googolhkl1	"/home/hkl/hadoop-2.6.0/sbin/hadoop-daemon.sh start journalnode"
ssh googolhkl2	"/home/hkl/hadoop-2.6.0/sbin/hadoop-daemon.sh start journalnode"
ssh googolhkl3	"/home/hkl/hadoop-2.6.0/sbin/hadoop-daemon.sh start journalnode"

/home/hkl/hadoop-2.6.0/sbin/hadoop-daemon.sh start namenode
/home/hkl/hadoop-2.6.0/sbin/hadoop-daemon.sh start zkfc
/home/hkl/hadoop-2.6.0/sbin/hadoop-daemons.sh start datanode

ssh googolhkl2 	"/home/hkl/hadoop-2.6.0/bin/hdfs namenode -bootstrapStandby"
ssh googolhkl2 	"/home/hkl/hadoop-2.6.0/sbin/hadoop-daemon.sh start namenode"
ssh googolhkl2 	"/home/hkl/hadoop-2.6.0/sbin/hadoop-daemon.sh start zkfc"

/home/hkl/hadoop-2.6.0/sbin/start-yarn.sh
/home/hkl/hadoop-2.6.0/sbin/mr-jobhistory-daemon.sh start historyserver
