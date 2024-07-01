SRC = src
LIB = lib/gson.jar
JLIB = lib gson.jar
BIN = bin

JFLAGS = -cp
JARFLAGS = cvfm
JC = javac

SMANIFEST = Server.mf
CMANIFEST = Client.mf


all: server_jar client_jar

run-all: server_run client_run

server:
	$(JC) $(JFLAGS) $(LIB) $(SRC)/server/*.java -d $(BIN)

client:
	$(JC) $(JFLAGS) $(LIB) $(SRC)/client/*.java -d $(BIN)

server_run:
	java $(JFLAGS) $(BIN):$(LIB) server.ServerMain 30000

client_run:
	java $(JFLAGS) $(BIN):$(LIB) client.ClientMain

server_jar:
	jar $(JARFLAGS) jar/Server.jar $(SMANIFEST) -C $(BIN)/ server -C $(JLIB)

client_jar:
	jar $(JARFLAGS) ./jar/Client.jar $(CMANIFEST) -C $(BIN)/ client -C $(JLIB)

server_run_jar:
	java -jar Server.jar 30000

client_run_jar:
	java -jar Client.jar

clean:
	rm -rf $(BIN)/*