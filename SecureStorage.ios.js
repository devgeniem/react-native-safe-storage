import React from 'react-native';

const SafeStorage = React.NativeModules.SafeStorage;

export default {
  setEntry : (key, value) => {
    return SafeStorage.setEntry(key, value);
  },
  getEntry : (key, defaultValue, callback) => {
    return SafeStorage.getEntry(key, defaultValue, callback);
  },

  getEntryAsync : (key, defaultValue) => {
    const p = new Promise((resolve, reject) => {
    
      SafeStorage.getEntry(key, defaultValue, (receivedValue) => {
        resolve(receivedValue);
      });
    }) // end of promsie
    return p;
  }
};