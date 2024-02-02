# list my keyStore entries
keytool -list -keystore keystore.p12 -storepass changeme
# delete an entry from my key store
keytool -delete -alias myAlias -keystore keystore.p12
# change the aliases of a key
keytool -changealias -alias "1" -destalias "localhost" -keypass changeme -keystore server.p12 -storepass changeme