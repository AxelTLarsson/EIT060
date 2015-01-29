#! /bin/bash
# TODO: Eventuellt fixa så att vi konfigurerar openssl-kommandot i början så att vi slipper
# manuellt på kommandoraden skriva in "CA" som Common Name och blankt i de andra fältet.
# TODO: Skriva in våra id och fulla namn enligt steg 3 (på rad 18 just nu (men kan ju ändras om folk lägger till/ tar bort rader))

# 2.1 Certificates, keystores and truststores

# 1. X.509 certificate with openssl
echo "* Creating root certificate \"CA\" with openssl..."
openssl req -x509 -newkey rsa:2048 -keyout rootCAkey.pem -out rootCert.pem
# 2.
echo
echo "* Adding root certificate \"CA\" to new trustore..."
keytool -import -file rootCert.pem -alias CA -keystore truststore -storepass password -noprompt
# 3.
echo
echo "* Creating end-user keypair in client-side keystore \"clientstore\""
keytool -alias end-user-key -dname "CN=dat11al1 (Axel Larsson) etc." -genkeypair -keystore clientkeystore -storepass password -keypass password
# 4.
echo
echo "* Creating Certificate Signing Request (CSR) \"certSignReq.csr\""
keytool -alias end-user-key -certreq -file certSignReq.csr -keystore clientkeystore -storepass password
# 5.
echo
echo "* Signing the Certificate Signing Request..."
openssl x509 -CAcreateserial -in certSignReq.csr -req -CA rootCert.pem -CAkey rootCAkey.pem -out signedCert
# 6.
echo
echo "* Importing certificate chain into keystore \"clientkeystore\""
keytool -import -file rootCert.pem -alias CA -keystore clientkeystore -storepass password
keytool -trustcacerts -alias end-user-key -import -file signedCert -keystore clientkeystore -storepass password

# Visa keystore i slutet:
keytool -list -v -keystore clientkeystore -storepass password

# Fråga A: with this option the CA serial number file is created if it does not exist: it will contain the serial number "02" and the certificate being signed will have the 1 as its serial number. Normally if the -CA option is specified and the serial number file does not exist it is an error.
