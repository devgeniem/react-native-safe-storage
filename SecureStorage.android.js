import React from 'react-native';

const SafeStorage = React.NativeModules.SafeStorage;
console.log("SafeStorage", SafeStorage);

export default {
  
	setEntry: (key, value) => {
		return SafeStorage.setEntry(key, value);
	},
	getEntry: (key, defaultValue, callback) => {
		return SafeStorage.getEntry(key, defaultValue, callback);
	},

	getEntryAsync: (key, defaultValue ) => new Promise((resolve, reject) => {
		SafeStorage.getEntry(key, defaultValue, ( receivedValue ) => {
			resolve(receivedValue);
		});
	})
};