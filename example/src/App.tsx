import * as React from 'react';

import { StyleSheet, View, Text, AppState, FlatList, Button  } from 'react-native';
import {
  discoverPeers,
  subscribeDiscoverPeer,
  stopDiscoverPeers,
  receiveMessage,
  sendMessageTo,
  receiveFile,
  sendFileTo,
  subscribeOnFileCopyEnd,
  subscribeOnProgressUpdates } from 'react-native-transfer-big-files';

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();
  const [peers, setPeers] = React.useState([]);
  const [message, setMessage] = React.useState <string>('');
  const [progress, setProgressFile] = React.useState<string>('0');
  const [received, setReceived] = React.useState('');

  React.useEffect(() => {

    const handleAppStateChange = (nextAppState: string) => {
      if (nextAppState === 'background' || nextAppState === 'inactive') {
        stopDiscoverPeers()
        setPeers([])
      } else {
        console.log('nextAppState: ', nextAppState);
        discoverPeers().then(setResult);
      }
    };

    const progressUpdatesSubscription = subscribeOnProgressUpdates(
      setProgress,
    );
    const copyFileEndSubscription = subscribeOnFileCopyEnd(
      setCopyFileEnd,
    );

    const appStateSubscription = AppState.addEventListener('change', handleAppStateChange);
    const discoverPeerSubscription = subscribeDiscoverPeer((peers)=>{
      setPeers(JSON.parse(peers))
    })
    discoverPeers('ip').then(setResult);
    console.log(result)
    return () => {
      appStateSubscription.remove();
      discoverPeerSubscription.remove();
      copyFileEndSubscription.remove();
      progressUpdatesSubscription.remove();
    };
  }, []);

  const setProgress = async (progress: string) => {
    setProgressFile(progress) //If the file is small, the progress will not be greater than 0.
  }
  const setCopyFileEnd = async (data: string) => {
    console.log(data)
    setReceived('File received successfully')
  }

  const sendMessage = (ipAddress: string) => {
    sendMessageTo('Hello World', ipAddress)
  }

  const catchMessage = async () => {
    const message:string = await receiveMessage() as string;
    setMessage(message)
  }

  const sendFile = (address: string) => {
    sendFileTo('/data/data/com.transferbigfilesexample/cache/document.pdf', address)
  }

  const catchFile = () => {
    receiveFile(`/data/data/com.transferbigfilesexample/cache/`, 'document.pdf', true)
  }

  interface ItemType {
    deviceName: string;
    ipAddress: string;
    // Otros campos si los hay
  }

  const ListItem = ({item}: { item: ItemType }) => (
    <View style={{width: '100%', flexDirection: 'row',
    justifyContent: 'space-evenly', alignItems: 'center',
    backgroundColor: '#00000022', padding: 10}}>
      <Text style={{color: 'black'}}>{item?.deviceName}</Text>
      <Button title='Send message' onPress={() => sendMessage(item?.ipAddress)}></Button>
      <Button title='Send file' onPress={() => sendFile(item?.ipAddress)}></Button>
    </View>
  );

  return (
    <View style={styles.container}>
      <Button title='Receive message' onPress={catchMessage}></Button>
      <View style={{paddingTop: 10}}>
        <Button title='Receive file' onPress={catchFile}></Button>
      </View>

      <View style={{paddingTop: 10}}>
        <Text style={{color: 'black'}}>Message Received</Text>
        <Text style={{color: 'green', fontWeight: 'bold'}}>{message}</Text>
      </View>

      <View style={{paddingTop: 10}}>
        <Text style={{color: 'black'}}>Progress File</Text>
        <Text style={{color: 'green', fontWeight: 'bold'}}>{progress}</Text>
        <Text style={{color: 'green', fontWeight: 'bold'}}>{received}</Text>
      </View>

      <Text style={{marginTop: 30, fontSize: 20, color: 'black'}}>Device list</Text>
      <FlatList
        data={peers}
        renderItem={({item}) => <ListItem item={item} />}
        keyExtractor={(index) => index}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
    backgroundColor: '#ffffff',
    color: '#000000'
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
