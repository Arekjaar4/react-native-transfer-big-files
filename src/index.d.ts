import type { EmitterSubscription } from 'react-native'

export let subscribeOnProgressUpdates: (callback: (progress: string) => void) => EmitterSubscription

export let subscribeOnFileCopyEnd: (callback: (data: string) => void) => EmitterSubscription

export let sendFileTo: (pathToFile: string, address: string) => Promise<{ time: number, file: string }>
export let receiveFile: (ip: string, folder: string, fileName: string, forceToScanGallery?: boolean) => Promise<string>
export let sendMessageTo: (message: string, address: string) => Promise<{ time: number, message: string }>
export let receiveMessage: (ip: string, props: { meta: boolean }) => Promise<string>
export let stopReceivingMessage: () => void

// system methods
export let subscribeOnEvent: (event: string, callback: Function) => EmitterSubscription

