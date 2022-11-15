# Before you start

This guide is addressed to the Android developers who want to integrate AdswizzSDK in their apps. Here is a quick overview. More details will be provided for each scenario in their respective section.

## Thread safety
AdswizzSDK is not thread safe, so it is expected that all the calls to the SDK are done on the main thread. AdswizzSDK does all the heavy lifting on other threads, so that the main one remains responsive.

## What is Client-Side Insertion

'Client-Side Insertion' represents the insertion of ads into the audio/video stream done by the integrator. Your app is responsible for the built-in logic to decide when to start and end an ad break by fetching ads from the ad server and playing them with your apps's audio/video capabilities. The SDK will handle all communication during ad fetching with the ad server while being in charge with the display of companion banner and performing various event reporting (impressions & quartiles).

## What is Server-Side Insertion

‘Server-Side Insertion’ represents the insertion of ads in the audio stream done by the server in real time. It requires AIS as a streaming server. AIS is the acronym for Audio Injector for Servers and is an AdsWizz product that does 'server-side insertion'.

The responsibilities are split between streaming server and SDK as follows:
* Streaming Server:
   * detects ad breaks and inserts audio ads into the audio stream
   * sends metadata information to make possible for the SDK to synchronize companion banner with the audio content and to detect interactive ads
* SDK:
   * decorates audio stream URL to increase targetability of ads
   * detects all events associated with an ad break (start, stop, change of ad)
   * retrieves, displays, synchronizes companion banner with audio content based on metadata information
   * may handle the display area for companion banners outside of an ad break
   * retrieves and processes interactivity information based on metadata

## What is ‘Podcasting‘

For podcasting scenario your app will be able to stream or download podcasts from an AIS (Ads Insertion Server) server with the audio ads being automatically inserted on the server-side. The podcasting server and the SDK split their responsibilities as follows:

**Podcasting Server:**
*  "stiches" ad breaks and inserts audio ads into the podcast creative audio file

**SDK:**
*  decorates podcast stream URL to increase targetability of ads
*  detects all events associated with an ad break (start, stop, change of ad)
*  retrieves, displays, synchronizes companion banner with the audio content based on metadata information
*  retrieves and process interactivity metadata information based on metadata information
*  processes metadata to make it possible for the SDK to synchronize companion banner with the audio content, to detect interactive ads and / or enable Remote Audio Data measurement

## Prerequisites for ‘Client-Side Insertion’

In order to successfully do the ‘Client-Side Insertion’ you will need to set/provide the following information for your **_AdswizzAdRequest_** object within **_AdRequestConnection_**:
*  adServer = the name of AdServer used to fetch ads from
*  zones = one or more ```AdswizzAdZone``` to identify the zone/zones used to retrieve audio/video ads from
*  companionZones = (optional) identifier of zone to retrieve creatives to be displayed by the companion banner
*  (optional) list of custom site variables used for ads selection (e.g referrer)

The following can be set as general AdswizzSDK parameters:
*  (optional) GDPR consent value
*  (optional) CCPA (U.S. privacy string) consent value
*  (optional) enable/disable data collection
*  (optional) integrator context. This contain information that the integrator should provide and will be used by the SDK (for example for VAST macro expansion)

For the client-side scenario, you may use any streaming server provided by your AdsWizz Integration Manager.

## Prerequisites for ‘Server-Side Insertion’

In order to successfully integrate the ‘Server-Side Insertion’, you will need to set the following information:
* in afrConfig:
   *  server = the name of Adserver used to insert ads from
   *  endpoint = contains the identifier of zone to retrieve creatives to be displayed by the companion banner
* in **_AdswizzAdStreamManager_**:
   *  adStreamURL = URL of audio stream. This is given in the play method.

The following can be set as general AdswizzSDK parameters:
*  (optional) GDPR consent value
*  (optional) CCPA (U.S. privacy string) consent value
*  (optional) enable/disable data collection
*  (optional) integrator context. This contain information that the integrator should provide and will be used by the SDK (for example for VAST macro expansion)


## Prerequisites for ‘Podcasting’

In order to successfully integrate the ‘Podcasting’ scenario, you will need to set/provide the following information for your **_AdswizzAdPodcastManager_** object:
*  adPodcastURL = URL of audio stream or downloaded audio file path (pass it to **_AdswizzAdPodcastManager_**'s _play()_ method)
*  conditions = (optional) a set of download conditions when using the download functionality of 'AdswizzAdPodcastManager'
* in afrConfig:
   *  server = the name of Adserver used to insert ads from
   *  endpoint = contains the identifier of zone to retrieve creatives to be displayed by the companion banner

The following can be set as general AdswizzSDK parameters:
*  (optional) GDPR consent value
*  (optional) CCPA (U.S. privacy string) consent value
*  (optional) location accuracy
*  (optional) location enable significant changes
