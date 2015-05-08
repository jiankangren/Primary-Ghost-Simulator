# Primary-Ghost-Simulator
A task scheduling simulator with back up to support failed tasks 

This is a task schedulor implemented in Java. Based on Criticality Based EDF Algorithm, a system can schedule tasks on multiple 
processors and all tasks will meet the deadline. And it could handle the event that some processor fails to schedule a primary
task with invoking a ghost task assigned on other processor. And the improvement of Dynamic Voltage Scheduling Algorithm make 
the whole system consume less energy.
