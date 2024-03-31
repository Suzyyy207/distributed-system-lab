# About The Project
The project aims to implement a distributed file system using Java Corba, inspired by Hadoop. The main functionalities include:
1. Implementing the file management functionality of the NameNode, referencing Hadoop Distributed File System (HDFS).
2. Implementing the file storage functionality of DataNode.
3. Utilizing the concept of FsImage to achieve localized file storage.
# How to Start
```
# terminal 1
cd ./DistributedSystemLab1/lab1/src && mkdir bin
idlj -fall api.idl
javac src/*.java src/api/*.java src/impl/*.java src/utils/*java -d bin/
orbd -ORBInitialPort 1050 -ORBInitialHost localhost

# terminal 2 start the NameNode
java -cp bin/ NameNodeLauncher -ORBInitialPort 900 -ORBInitialHost localhost

# terminal 3 start the DataNode1
java -cp bin/ DataNodeLauncher -ORBInitialPort 900 -ORBInitialHost localhost

# terminal 4 start the DataNode2
java -cp bin/ DataNodeLauncher -ORBInitialPort 900 -ORBInitialHost localhost

# terminal 5 start Client1
java -cp bin/ ClientLauncher -ORBInitialPort 1050 -ORBInitialHost localhost
```
# How to use
After you start the client, you can run these commands below.
```
open test.txt w         # open file
append 1 hello world       # append more content by using fd
read 2                  # read file by using fd
close 1             # close file by using fd
```
You can see more details in ./DistributedSystemLab1/lab1.pdf
