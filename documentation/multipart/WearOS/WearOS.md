# Integrate into Wear OS apps

If you have a companion Wear OS app for your Android app, you can also integrate our Wear OS SDK. This will allow you to use the smartwatch as a detector along side your phone.

To integrate AdsWizz Wear OS SDK into your Wear OS app simply write the following line into your Wear OS app ```build.gradle``` file.

```groovy
implementation 'com.adswizz:adswizz-wear-sdk:version'
```
where ```version``` is the latest version of adswizz-wear-sdk provided to you by an AdsWizz PIM.

Keep in mind that for the communication to work between phone and Wear OS app, the apps have to have the same ```aplicationId``` (i.e. ```com.example.sample.app```)

## ShakeMe on Wear OS

After the ```adswizz-wear-sdk``` integration is done, you don't need to do anything else for this to work. As long as there is a connection between your phone and your Wear OS device, if you receive an interactive ad
with ```shake``` as detection method, the detector will automatically start on both devices. When one device detects, the other is notified and stops its own detector.

In the following image you can see a complete flow of how the feature works.

<img src="img/SmartWatch-ShakeFlow.png" width="1000" />

## Voice Detector on Wear OS
The same as **shakeMe** the voice detector works on Wear OS too. After integrating the **Wear OS SDK**, the only thing you need to do
to have voice detection active on the watch is to make sure you give your Wear OS app the ```RECORD_AUDIO``` permission.