GS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        Agent.java \
	Bounty.java \
	MyState.java \
	Orientation.java \
	Position.java \
	Resource.java \
	StateComparator.java \
	TourGuide.java \
	WorldMap.java \
	 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

