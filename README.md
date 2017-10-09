Usage
-----
	This module can be used to store and encrypt Strings as "key, value"-pairs to platform specific storages.

	- import SafeStore module: 
		import safeStorage from 'react-native-safe-storage/SafeStorage';

	- encrypt:
		SafeStorage.setEntry(key, value);

	- decrypt:
		SafeStorage.getEntry(key, defaultValue, callback);

		e.g.
		safeStorage.getEntry("someKey", "default value", (s) => {alert(s);});

	- Asynchronous decrypt:
		await SafeStorage.getEntryAsync(key, defaultValue);

		e.g.
		await safeStorage.getEntryAsync("someKey", "default value");

	- decrypt method returns the default value if the key specified is not found

Android
-------
	In android keys are encrypted with RSA/ECB/PKCS1Padding keypair and stored to shared preferences.


iOS:
----
	In iOS data is stored to the iOS keychain.
