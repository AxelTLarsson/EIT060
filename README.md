# EIT060
Repo for the projects 1 and 2 in the course Computer Security EIT060

## src
This is the folder for development.

### client
Package for client development.

### server
Package for server development.

### setup_server.sh
Creates a new root CA and server. This file you would typically only execute once and it copies
the root CA and server stores etc into build/server (not version tracked), it also copies over
setup_new_client.sh to build/server.

### setup_new_client.sh
This script should only be executed from build (not version tracked). It creates a new client, 
with corresponding certs signed by the CA created by setup_server.sh.
It is now very smooth to create new clients. In the future we should add some feature to
distinguish between doctors, nurses etc. 

## project1
This is kept for archiving purposes, it contains the stuff we did in project 1.
