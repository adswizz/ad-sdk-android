import type { ViewProps } from 'react-native';

export type SampleNativeBridgeViewProps = ViewProps;

export type AdEventPayload = {
  type: string;
  message: string;
};

export type SampleNativeBridgeModuleEvents = {
  onAdEvent: (event: AdEventPayload) => void;
};

