# Antenna-Analyzer
Host software for running a K6BEZ antenna analyzer.

This program is a simple, cross-platform front-end for the K6BEZ antenna analyzer, written to make the analyzer a little easier to use, and more functional.

The basic features of the program are:
- The user can select one or more bands to sweep, or specify a custom frequency range.
- New tabs are opened for each swept band, containing a graph of the SWR across the entire band.
- After a sweep is complete, summary data - by band - is populated in a table on a "Summary" tab.
- The summary data is accumulated across sweeps, and the user has the option of labelling each sweep.
- Additionally, the table of summary data may be sorted by any combination of columns, ascending or descending.

## Did you say "cross-platform"?

Yes. This is a JavaFX application using a cross-platform serial communications library called jSerialComm. In theory it should build and run on any system supported by jSerialComm, including any reasonably modern version of Linux, Windows, and MacOSX. That said, as of this time I have only built and tested it on Ubuntu 18.10, and Windows 10 Professional.

## Do I have to build the program myself?

No, but if you want to, you can. You'll need Intellij IDEA, and the Oracle Java 1.8 JDK. If you're a normal person though, installers for Linux (a .deb package) and Windows are available in this repository. An installable for MacOSX should be coming soon.

## What is the K6BEZ antenna analyzer?

It's a simple, open-source antenna analyzer project you can build yourself for a little less than $50 in parts. The analyzer was designed by Beric Dunn (K6BEZ), with a little help from George (KJ6VU), and was first published on the website of the Ham Radio Workbench podcast, hosted by George and Jeremy (KF7IJZ). If you like building things, especially ham radio related things, this is a podcast you should listen to.

Here's a link to the analyzer's project page on the podcast website: [K6BEZ Antenna Analyzer Project Page](https://www.hamradioworkbench.com/k6bez-antenna-analyzer.html "Ham Radio Workbench website")

You'll find everything there you need to build one of these things yourself.
