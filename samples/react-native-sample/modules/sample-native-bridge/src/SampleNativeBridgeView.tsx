import { requireNativeViewManager } from 'expo-modules-core';
import * as React from 'react';

import { SampleNativeBridgeViewProps } from './SampleNativeBridge.types';

const NativeView: React.ComponentType<SampleNativeBridgeViewProps> =
  requireNativeViewManager('SampleNativeBridge');

export default function SampleNativeBridgeView(props: SampleNativeBridgeViewProps) {
  return <NativeView {...props} />;
}
