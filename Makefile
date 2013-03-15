
CLASSPATH:="bin:lib/gson-2.2.2.jar"
CLASS=bin/com/conductrics/Conductrics.class bin/com/conductrics/Agent.class

all: $(CLASS)

test: $(CLASS) bin/SimpleTest.class
	java -cp $(CLASSPATH) SimpleTest

bin/%.class: src/%.java
	@mkdir -p bin
	javac -cp $(CLASSPATH) -sourcepath src -d bin $<

bin/%Test.class: test/%Test.java
	javac -cp $(CLASSPATH) -sourcepath test -d bin $<

clean:
	rm -f test/*.class
	rm -rf bin/*
