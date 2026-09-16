import { registerWebModule, NativeModule } from 'expo';

import { SampleNativeBridgeModuleEvents } from './SampleNativeBridge.types';

class SampleNativeBridgeModule extends NativeModule<SampleNativeBridgeModuleEvents> {
  async play(): Promise<void> {}
  async pause(): Promise<void> {}
  async resume(): Promise<void> {}
  async requestAds(): Promise<boolean> {
    return false;
  }
  async observe(): Promise<void> {}
}

export default registerWebModule(SampleNativeBridgeModule, 'SampleNativeBridgeModule');
