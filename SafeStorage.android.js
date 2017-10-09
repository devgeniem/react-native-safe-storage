import React from 'react-native';

const SecureStorage = React.NativeModules.SecureStorage;

export default {
	setEntry: (key, value) => {
		return SecureStorage.setEntry(key, value);
	},
	getEntry: (key, defaultValue, callback) => {
		return SecureStorage.getEntry(key, defaultValue, callback);
	},

	getEntryAsync: (key, defaultValue ) => new Promise((resolve, reject) => {
		SecureStorage.getEntry(key, defaultValue, ( receivedValue ) => {
			resolve(receivedValue);
		});
	})
};