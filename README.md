# SecureStorage

	installation from gitlab
		
		npm install git+ssh://gitlab@gitlab.geniem.com:react-native-modules/react-native-secure-storage.git --save


Usage
-----
	This module can be used to store and encrypt Strings as "key, value"-pairs to platform specific storages.

	- import SecureStore module: 
		import secureStorage from 'react-native-secure-storage/SecureStorage';

	- encrypt:
		SecureStorage.setEntry(key, value);

	- decrypt:
		SecureStorage.getEntry(key, defaultValue, callback);

		e.g.
		secureStorage.getEntry("someKey", "default value", (s) => {alert(s);});

	- Asynchronous decrypt:
		await SecureStorage.getEntryAsync(key, defaultValue);

		e.g.
		await secureStorage.getEntryAsync("someKey", "default value");

	- decrypt method returns the default value if the key specified is not found

Android
-------
	In android keys are encrypted with RSA/ECB/PKCS1Padding keypair and stored to shared preferences.


iOS:
----
	In iOS data is stored to the iOS keychain.
