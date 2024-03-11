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
const DISCOVER_PEER = 'DISCOVER_PEER';

// CONSTS
const MODULE_NAME = 'WiFi_TransferFiles';

const subscribeOnEvent = (event: string, callback: (data: string) => void) => {
    return DeviceEventEmitter.addListener(`${MODULE_NAME}:${event}`, callback);
};


const subscribeOnProgressUpdates = (callback: (data: string) => void) => subscribeOnEvent(COPY_FILE_PROGRESS, callback);

const subscribeOnFileCopyEnd = (callback: (data: string) => void) => subscribeOnEvent(COPY_FILE_END, callback);
const subscribeDiscoverPeer = (callback: (data: string) => void) => subscribeOnEvent(DISCOVER_PEER, callback);

const stopDiscoverPeers = () => TransferBigFiles.stopDiscoverPeers();

const discoverPeers = () => TransferBigFiles.discoverPeers();

const sendFileTo = (pathToFile: string, address: string) => TransferBigFiles.sendFileTo(pathToFile, address);

const receiveFile = (folder: string, fileName: string, forceToScanGallery: boolean) => new Promise((resolve, reject) => {
    TransferBigFiles.receiveFile(folder, fileName, forceToScanGallery, (pathTofile: string) => {
        if(pathTofile) {
          resolve(pathTofile);
        } else {
          reject(pathTofile)
        }
    });
});

const sendMessageTo = (message: string, address: string) => TransferBigFiles.sendMessageTo(message, address);

const receiveMessage = () => new Promise((resolve, reject) => {
    TransferBigFiles.receiveMessage((message: string) => {
      if(message) {
        resolve(message);
      } else {
        reject(message)
      }

    });
});

const stopReceivingMessage = () => TransferBigFiles.stopReceivingMessage()


export {
    // public methods

    subscribeOnProgressUpdates,
    subscribeOnFileCopyEnd,
    subscribeDiscoverPeer,
    sendFileTo,
    receiveFile,
    sendMessageTo,
    receiveMessage,
    stopReceivingMessage,
    discoverPeers,
    stopDiscoverPeers,

    // system methods
    subscribeOnEvent,

    // const
    COPY_FILE_PROGRESS,
    COPY_FILE_END,
    DISCOVER_PEER
};

