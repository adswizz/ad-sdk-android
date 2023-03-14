# AdData interface

This interface exposes to the host app the following information:

- id: the ad ID from the VAST response
- duration: the duration of the ad taken from the VAST response
- mediaUrlString: the media file that was chosen to be used for the ad
- adFormat: the ad format - normal, extension or silence extension
- skipOffset: the minimum time that has to pass until we can skip an ad. If null the ad can be skipped immediately
- extensions: ad extensions from the VAST response
- adParameters: ad parameters from the VAST response
- creativeExtensions: creative extensions from the VAST response
- width: the media file width field value from the VAST response
- height: the media file height field value from the VAST response
- getAllCompanions(): the list of all companions from the VAST response
- hasCompanion: expose whether or not the ad has companion banner information
