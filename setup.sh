#! /bin/bash

# Text colours
NORMAL=$(tput sgr0)
GREEN=$(tput setaf 2)

print_green() {
	printf '%s%s' "$GREEN" "$1" "$NORMAL"
	echo
}

# 2.1 Certificates, keystores and truststores

# 1. X.509 certificate with openssl
print_green "* Creating root certificate \"CA\" with openssl..."
openssl req -x509 -newkey rsa:2048 -keyout rootCAkey.pem -out rootCert.pem -subj "/C=/ST=/L=/O=/OU=/CN=CA" -passout pass:password
# 2.
print_green "* Adding root certificate \"CA\" to new trustore..."
keytool -import -file rootCert.pem -alias CA -keystore clienttruststore -storepass password -noprompt
# 3.
print_green "* Creating end-user keypair in client-side keystore \"clientkeystore\""
keytool -alias end-user-key -dname "CN=dat11al1 (Axel Larsson)/dat12pbr (Patrik Brosell)/dic12aha (Adrian Hansson)/dat12cti (Carl Tidelius) " -genkeypair -keystore clientkeystore -storepass password -keypass password
# 4.
print_green "* Creating Certificate Signing Request (CSR) \"certSignReq.csr\""
keytool -alias end-user-key -certreq -file certSignReq.csr -keystore clientkeystore -storepass password
# 5.
print_green "* Signing the Certificate Signing Request..."
openssl x509 -CAcreateserial -in certSignReq.csr -req -CA rootCert.pem -CAkey rootCAkey.pem -out signedCert -passin pass:password
# 6.
print_green "* Importing certificate chain into keystore \"clientkeystore\""
keytool -import -file rootCert.pem -alias CA -keystore clientkeystore -storepass password -noprompt
keytool -trustcacerts -alias end-user-key -import -file signedCert -keystore clientkeystore -storepass password

# Visa keystore i slutet:
print_green "* \"clientkeystore\" contents:"
keytool -list -v -keystore clientkeystore -storepass password

# 9.
print_green "* Generating server-side keypair in server-side keystore \"serverkeystore\"..."
keytool -alias srvKey -dname "CN=myserver" -genkeypair -keystore serverkeystore -storepass password -keypass password


print_green "* Create CSR \"srvKeyStore.csr\"..."
keytool -alias srvKey -certreq -file srvKeyStore.csr -keystore serverkeystore -storepass password


print_green "* Signing the CSR..."
openssl x509 -CAcreateserial -in srvKeyStore.csr -req -CA rootCert.pem -CAkey rootCAkey.pem -out signedSrvCert -passin pass:password


print_green "* Importing certificate chain into keystore \"serverkeystore\"..."
keytool -import -file rootCert.pem -alias CA -keystore serverkeystore -storepass password -noprompt
keytool -trustcacerts -alias srvKey -import -file signedSrvCert -keystore serverkeystore -storepass password

# Visa serverkeystore
print_green "* \"serverkeystore\" contents:"
keytool -list -v -keystore serverkeystore -storepass password

# 10.
print_green "* Create server-side truststore \"servertruststore\"..."
keytool -import -file rootCert.pem -alias CA -keystore servertruststore -storepass password -noprompt

print_green "* Moving stores into \"src/\"..."
mv *store src/
print_green "* Run \"reset.sh\" to clean up (optional)"
