#!/bin/bash

# Text colours
NORMAL=$(tput sgr0)
GREEN=$(tput setaf 2)

print_green() {
	printf '%s%s' "$GREEN" "$1" "$NORMAL"
	echo
}

server_setup_warning()
{
cat << EOF
This script creates a new root CA and a new "server" with its own signed certificate.
Are you sure you want to create a new CA? If you proceed, all clients created with the
old CA will no longer be able to communicate with the new server.

If you wish to create a new server, delete the file "rootCert.pem" from the current folder
and this message will cease to appear.
EOF
}

if [ -f "rootCert.pem" ];
then
   server_setup_warning
   exit 1
fi

# Create root CA with openssl
print_green "* Creating root certificate \"CA\" with openssl..."
openssl req -x509 -newkey rsa:2048 -keyout rootCAkey.pem -out rootCert.pem -subj "/C=/ST=/L=/O=/OU=/CN=CA" -passout pass:password

# Generate server key pair and save in serverkeystore
print_green "* Generating server-side keypair in server-side keystore \"serverkeystore\"..."
keytool -alias srvKey -dname "CN=myserver" -genkeypair -keystore serverkeystore -storepass password -keypass password

# Sign the server key pair with root CA
print_green "* Create CSR \"srvKeyStore.csr\"..."
keytool -alias srvKey -certreq -file srvKeyStore.csr -keystore serverkeystore -storepass password
print_green "* Signing the CSR..."
openssl x509 -CAcreateserial -in srvKeyStore.csr -req -CA rootCert.pem -CAkey rootCAkey.pem -out signedSrvCert -passin pass:password

# Save the signed certificate chain into serverkeystore
print_green "* Importing certificate chain into keystore \"serverkeystore\"..."
keytool -import -file rootCert.pem -alias CA -keystore serverkeystore -storepass password -noprompt
keytool -trustcacerts -alias srvKey -import -file signedSrvCert -keystore serverkeystore -storepass password

# Save root CA in servertruststore
print_green "* Create server-side truststore \"servertruststore\"..."
keytool -import -file rootCert.pem -alias CA -keystore servertruststore -storepass password -noprompt

# Cleanup
print_green "* Cleaning up..."
rm rootCert.srl srvKeyStore.csr signedSrvCert

# Copy setup_new_client.sh to folder
cp ../../src/setup_new_client.sh ./
print_green "* To run the server, compile server.java and run it in this directory, e.g. \"java server 9876\""