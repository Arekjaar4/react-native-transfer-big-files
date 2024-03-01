import type { EmitterSubscription } from 'react-native'

export const subscribeOnProgressUpdates: (callback: (progress: string) => void) => EmitterSubscription

export const subscribeOnFileCopyEnd: (callback: (data: string) => void) => EmitterSubscription

export const sendFileTo: (pathToFile: string, address: string) => Promise<{ time: number, file: string }>
export const receiveFile: (ip: string, folder: string, fileName: string, forceToScanGallery?: boolean) => Promise<string>
export const sendMessageTo: (message: string, address: string) => Promise<{ time: number, message: string }>
export const receiveMessage: (ip: string, props: { meta: boolean }) => Promise<string>
export const stopReceivingMessage: () => void

// system methods
export const subscribeOnEvent: (event: string, callback: Function) => EmitterSubscription

