import { NativeModule, requireNativeModule } from 'expo';

import { SampleNativeBridgeModuleEvents } from './SampleNativeBridge.types';

declare class SampleNativeBridgeModule extends NativeModule<SampleNativeBridgeModuleEvents> {
  play(): Promise<void>;
  pause(): Promise<void>;
  resume(): Promise<void>;
  requestAds(): Promise<boolean>;
  observe(): Promise<void>;
}

export default requireNativeModule<SampleNativeBridgeModule>('SampleNativeBridge');
