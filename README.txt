1. config etc/
2. ./bin/Panda.sh  -i    $index
3  ./bin/Panda.sh  -e    $search and evaluation


a. models
   Panda/src/uk/ac/ucl/panda/retrieval/Searcher.java
   change model by RawMaterial(index, 0) 0 for simple TF-IDF
b. parameters
   Panda/src/uk/ac/ucl/panda/retrieval/models   

If you run want to run in IDE or on commandline, please set the paths to etc/ var/ home/  folders
either in Panda.java
ex:
        Change the paths in Panda.java: 
 	private final static String PANDA_ETC  = System.getProperty("panda.etc", "/home/administrator/finalTestAssignment/IRCourse/Panda/etc/");
	private final static String PANDA_VAR  = System.getProperty("panda.var", "/home/administrator/finalTestAssignment/IRCourse/Panda/var/");
	private final static String PANDA_HOME = System.getProperty("panda.home", "/home/administrator/finalTestAssignment/IRCourse/Panda");

or 

if you are running in IDE, set up the arguments in properties.
Example in NETBEANS : 
properties-> VM arguments : 
-Xmx3000M -Xms3000M -Dpanda.etc=/home/administrator/finalTestAssignment/IRCourse/Panda/etc -Dpanda.var=/home/administrator/finalTestAssignment/IRCourse/Panda/var -Dpanda.home=/home/administrator/finalTestAssignment/IRCourse/Panda






