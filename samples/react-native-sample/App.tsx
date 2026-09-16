import { StatusBar } from 'expo-status-bar';
import { useEffect, useRef, useState } from 'react';
import { Button, ScrollView, StatusBar as RNStatusBar, StyleSheet, Text, View } from 'react-native';

import { SampleNativeBridgeModule, SampleNativeBridgeView } from './modules/sample-native-bridge/src';

export default function App() {
  const [events, setEvents] = useState<string[]>([]);
  const consoleRef = useRef<ScrollView>(null);

  useEffect(() => {
    const subscription = SampleNativeBridgeModule.addListener('onAdEvent', (event) => {
      setEvents((prev) => [...prev, `${event.type}: ${event.message}`]);
    });

    return () => subscription.remove();
  }, []);

  const handleRequestAds = async () => {
    const result = await SampleNativeBridgeModule.requestAds();
    if (result) {
      await SampleNativeBridgeModule.observe();
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.topHalf}>
        <Text style={styles.heading}>Native bridge sample</Text>

        <View style={styles.controls}>
          <Button title="Request Ads" onPress={handleRequestAds} />
          <Button title="Play" onPress={() => SampleNativeBridgeModule.play()} />
          <Button title="Pause" onPress={() => SampleNativeBridgeModule.pause()} />
          <Button title="Resume" onPress={() => SampleNativeBridgeModule.resume()} />
        </View>

        <Text style={styles.label}>Native AdCompanionView:</Text>
        <SampleNativeBridgeView style={styles.nativeView} />
      </View>

      <View style={styles.bottomHalf}>
        <ScrollView
          ref={consoleRef}
          style={styles.console}
          contentContainerStyle={styles.consoleContent}
          onContentSizeChange={() => consoleRef.current?.scrollToEnd({ animated: true })}>
          {events.length === 0 ? (
            <Text style={styles.consoleLine}>Waiting for ad events...</Text>
          ) : (
            events.map((line, i) => (
              <Text key={i} style={styles.consoleLine}>
                {line}
              </Text>
            ))
          )}
        </ScrollView>
      </View>

      <StatusBar style="auto" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    paddingTop: RNStatusBar.currentHeight ?? 0,
  },
  topHalf: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
    gap: 8,
  },
  bottomHalf: {
    flex: 1,
    width: '100%',
  },
  heading: {
    fontSize: 18,
    fontWeight: '600',
    marginBottom: 12,
  },
  label: {
    fontSize: 13,
    color: '#666',
    marginTop: 12,
  },
  controls: {
    flexDirection: 'row',
    gap: 8,
    marginTop: 8,
  },
  nativeView: {
    flex: 1,
    width: '100%',
    borderRadius: 8,
    overflow: 'hidden',
  },
  console: {
    flex: 1,
    width: '100%',
    backgroundColor: '#111',
  },
  consoleContent: {
    paddingHorizontal: 16,
    paddingVertical: 16,
  },
  consoleLine: {
    color: '#0f0',
    fontFamily: 'monospace',
    fontSize: 12,
  },
});
