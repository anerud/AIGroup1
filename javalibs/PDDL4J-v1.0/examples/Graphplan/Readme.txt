PDDL4J example: Graphplan 
Date: 06/03/2008

1. Contact 

Damien Pellier (damien.pellier@math-info.univ-paris5.fr)
htt://www-math-info.univ-paris5.fr/~pellier

2. Description 

This project is a simple implementation of the graphlan planner based on the 
PDDL4J. PDDL4 is an open source library under licence CECILL (http://www.cecill.info/). 
The purpose of the PDDL4J is to facilitate the  java implementation of planners 
based on the PDDL (Planning Description Language). The library contains a 
parser on the last version of PDDL 3.0 and all the classes need to manipulate its 
concepts. The parser can be configured to accept only specified requirements of 
PDDL langage. 

3. How to use the launch JGraphplan ?

Usage: graphplan [domain] [problem]

Example:
% java -server -jar graphplan.jar pddl/gripper/gripper.pddl pddl/gripper/pb1.pddl

NB: somes planning domains and problems are available in the "pddl" directory.
