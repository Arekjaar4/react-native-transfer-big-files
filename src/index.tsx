import { NativeModules, Platform, DeviceEventEmitter } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-transfer-big-files' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const TransferBigFiles = NativeModules.TransferBigFiles
  ? NativeModules.TransferBigFiles
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

// ACTIONS
const COPY_FILE_PROGRESS = 'COPY_FILE_PROGRESS';
const COPY_FILE_END = 'COPY_FILE_END';

// CONSTS
const MODULE_NAME = 'WiFi_TransferFiles';

const subscribeOnEvent = (event, callback) => {
    return DeviceEventEmitter.addListener(`${MODULE_NAME}:${event}`, callback);
};


const subscribeOnProgressUpdates = (callback) => subscribeOnEvent(COPY_FILE_PROGRESS, callback);

const subscribeOnFileCopyEnd = (callback) => subscribeOnEvent(COPY_FILE_END, callback);

const sendFileTo = (pathToFile, address) => TransferBigFiles.sendFileTo(pathToFile, address);

const receiveFile = (ip, folder, fileName, forceToScanGallery) => new Promise((resolve, reject) => {
    TransferBigFiles.receiveFile(ip, folder, fileName, forceToScanGallery, (pathTofile) => {
        resolve(pathTofile);
    });
});

const sendMessageTo = (message, address) => TransferBigFiles.sendMessageTo(message, address);

const receiveMessage = (ip, props) => new Promise((resolve, reject) => {
    TransferBigFiles.receiveMessage(ip, props, (message) => {
        resolve(message);
    });
});

const stopReceivingMessage = () => TransferBigFiles.stopReceivingMessage()


export {
    // public methods

    subscribeOnProgressUpdates,
    subscribeOnFileCopyEnd,
    sendFileTo,
    receiveFile,
    sendMessageTo,
    receiveMessage,
    stopReceivingMessage,

    // system methods
    subscribeOnEvent,

    // const
    COPY_FILE_PROGRESS,
    COPY_FILE_END,
};

