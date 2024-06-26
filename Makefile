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
#Esegue il server
server_run:
	java $(JFLAGS) $(BIN):$(LIB) server.ServerMain 30000
#Esegue il client
client_run:
	java $(JFLAGS) $(BIN):$(LIB) client.ClientMain
#Crea il file jar per il server
server_jar:
	jar $(JARFLAGS) jar/Server.jar $(SMANIFEST) -C $(BIN)/ server -C $(JLIB)
#Crea il file jar per il client
client_jar:
	jar $(JARFLAGS) ./jar/Client.jar $(CMANIFEST) -C $(BIN)/ client -C $(JLIB)
#Esegue il file jar per il server
server_run_jar:
	java -jar Server.jar 30000
#Esegue il file jar per il client
client_run_jar:
	java -jar Client.jar
#Pulisce la directory bin
clean:
	rm -rf $(BIN)/*
#===========