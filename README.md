# react-native-transfer-big-files

Module that enables the transfer of large files between two devices over Wi-Fi.

## Installation

```sh
npm install react-native-transfer-big-files
```

## Usage

Import the required methods from the library:
```js
import {
  discoverPeers,
  subscribeDiscoverPeer,
  stopDiscoverPeers,
  receiveMessage,
  sendMessageTo,
  receiveFile,
  sendFileTo,
  subscribeOnFileCopyEnd,
  subscribeOnProgressUpdates
} from 'react-native-transfer-big-files';

```

## Example

```js
import * as React from 'react';
import { StyleSheet, View, Text, AppState, FlatList, Button } from 'react-native';

// Import methods from the library
import {
  discoverPeers,
  subscribeDiscoverPeer,
  stopDiscoverPeers,
  receiveMessage,
  sendMessageTo,
  receiveFile,
  sendFileTo,
  subscribeOnFileCopyEnd,
  subscribeOnProgressUpdates
} from 'react-native-transfer-big-files';

export default function App() {
  // State variables
  const [peers, setPeers] = React.useState([]);
  const [message, setMessage] = React.useState('');
  const [progress, setProgress] = React.useState('');
  const [received, setReceived] = React.useState('');

  // Subscribe to discover peers event
  React.useEffect(() => {
    const discoverPeerSubscription = subscribeDiscoverPeer((peers) => {
      setPeers(JSON.parse(peers));
    });
    discoverPeers('ip');

    return () => {
      discoverPeerSubscription.remove();
    };
  }, []);

  // Handle message received
  const catchMessage = async () => {
    const message = await receiveMessage();
    setMessage(message);
  };

  // Handle file received
  const catchFile = () => {
    receiveFile(`/data/data/com.transferbigfilesexample/cache/`, 'document.pdf', true)
      .then(() => setReceived('File received successfully'))
      .catch(() => setReceived('Error receiving file'));
  };

  // Render method
  return (
    <View style={styles.container}>
      {/* UI components */}
    </View>
  );
}

// Styles
const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
    backgroundColor: '#ffffff',
    color: '#000000'
  },
});
```
## Methods

### discoverPeers(address: string): Promise<number>
Discover peers on the network.

### subscribeDiscoverPeer(callback: (peers: string) => void): EmitterSubscription
Subscribe to discover peer events.

### stopDiscoverPeers(): void
Stop discovering peers.

### sendMessageTo(message: string, address: string): Promise<{ time: number, message: string }>
Send a message to a specific device.

### receiveMessage(): Promise<string>
Receive a message from another device.

### sendFileTo(pathToFile: string, address: string): Promise<{ time: number, file: string }>
Send a file to a specific device.

### receiveFile(folder: string, fileName: string, forceToScanGallery?: boolean): Promise<string>
Receive a file from another device.

### subscribeOnProgressUpdates(callback: (progress: string) => void): EmitterSubscription
Subscribe to file transfer progress updates.

### subscribeOnFileCopyEnd(callback: (data: string) => void): EmitterSubscription
Subscribe to file copy end events.

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT
MIT License

Copyright (c) 2024 Arekjaar

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
