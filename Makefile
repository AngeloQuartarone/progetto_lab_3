SRC = src
LIB = lib/gson.jar
BIN = bin

JFLAGS = -cp
JARFLAGS = cvfm
JC = javac

SMANIFEST = SERVER.MF
CMANIFEST = CLIENT.MF


all: server_jar client_jar

run-all: server_run client_run

server:
	$(JC) $(JFLAGS) $(LIB) $(SRC)/server/*.java -d $(BIN)

client:
	$(JC) $(JFLAGS) $(LIB) $(SRC)/client/*.java -d $(BIN)
#Esegue il server
server_run:
	java $(JFLAGS) $(BIN):$(LIB) server.ServerMain 1800000
#Esegue il client
client_run:
	java $(JFLAGS) $(BIN):$(LIB) client.ClientMain
#Crea il file jar per il server
server_jar:
	jar $(JARFLAGS) Server.jar $(SMANIFEST) -C $(BIN)/ Server
#Crea il file jar per il client
client_jar:
	jar $(JARFLAGS) Client.jar $(CMANIFEST) -C $(BIN)/ Client
#Esegue il file jar per il server
server_run_jar:
	java -jar Server.jar
#Esegue il file jar per il client
client_run_jar:
	java -jar Client.jar
#Pulisce la directory bin
clean:
	rm -rf $(BIN)/*
#===========