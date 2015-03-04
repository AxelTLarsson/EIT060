#!/bin/bash
# Text colours
NORMAL=$(tput sgr0)
GREEN=$(tput setaf 2)

print_green() {
	printf '%s%s' "$GREEN" "$1" "$NORMAL"
	echo
}




usage()
{
cat << EOF
usage: $0 <user name for new client>

Sets up a new client for use with the server. The output is a folder with the same
name as the new user containing a truststore with the root CA certificate and a
keystore containing the root CA certificate and the new user's certificate chain.
EOF
}

# Check that user name is supplied as arg, else show usage and exit.
if [[ -z $1 ]]
then
	usage
	exit 1
fi

USER_NAME=$1

print_green "* Adding root certificate \"CA\" to new trustore..."
keytool -import -file rootCert.pem -alias CA -keystore clienttruststore -storepass password -noprompt

print_green "* Creating end-user keypair in client-side keystore \"clientkeystore\" for $1"
keytool -alias $USER_NAME -dname "CN=$USER_NAME" -genkeypair -keystore clientkeystore -storepass password -keypass password

# Sign the key-pair
print_green "* Creating Certificate Signing Request (CSR) \"certSignReq.csr\""
keytool -alias $USER_NAME -certreq -file certSignReq.csr -keystore clientkeystore -storepass password
print_green "* Signing the Certificate Signing Request..."
openssl x509 -CAcreateserial -in certSignReq.csr -req -CA rootCert.pem -CAkey rootCAkey.pem -out signedCert -passin pass:password

# Save certificate chain tn keystore
print_green "* Importing certificate chain into keystore \"clientkeystore\""
keytool -import -file rootCert.pem -alias CA -keystore clientkeystore -storepass password -noprompt
keytool -trustcacerts -alias $USER_NAME -import -file signedCert -keystore clientkeystore -storepass password

# Move files to new folder $USER_NAME
print_green "* Moving output to folder $USER_NAME"
mkdir $USER_NAME
mv clienttruststore ./$USER_NAME/
mv clientkeystore ./$USER_NAME/

# Copy Client.java and deps for convenience
cp client/Client.java $USER_NAME/Client.java
cp client/ResponseGenerator.java $USER_NAME/ResponseGenerator.java
cp client/Utils.java  $USER_NAME/Utils.java

# Clean up
print_green "* Cleaning up..."
rm certSignReq.csr signedCert rootCert.srl

print_green "* To run the client, compile Client.java and run it in the newly created directory, e.g. \"java Client localhost 9876\""