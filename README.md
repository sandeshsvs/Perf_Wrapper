# Perf_Wrapper

This is a code that I've written for use at my work. 

When people run perf tests using jmeter multiple times, they usually see some issues like:
1) To edit settings like thread-count, ramp-up time etc. we have to open the jmx file in vi editor and edit the file everytime. 
2) Even though we have results file with timestamps, over time the number of files grow a lot and managing the results for different areas and different runs became difficult. Also, jtl’s end up taking a lot of space on the machines.
3) Only those with knowledge on jmeter commands would execute jmeter. We wanted to make it simpler for everyone to execute.

So, we thought of writing a simple program that would make it easier to execute the perf tests.
The program achieves the following:
1) Accepts a text file for providing commonly changing values like threads, duration etc.
2) Allows running across multiple hosts.
3) Consolidates the results and generates csv’s from jtl.
4) Inserts the records in a db and triggers a mail consisting of the results from previous and current runs.
4) Accepts a template value and selects the corresponding body file from resources.

