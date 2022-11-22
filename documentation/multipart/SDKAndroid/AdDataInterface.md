# AdData interface

This interface exposes to the host app the following informations:

- id: the ad id from the VAST response
- duration: the duration of the ad taken from VAST response
- mediaUrlString: the media file that was chosen be used for the ad
- adFormat: the ad format: normal, extension or silence extension
- skipOffset: the minimum time that has to pass until we can skip an ad. If null the ad can be skipped immediately
- extensions: ad extensions from VAST response
- adParameters: ad parameters from VAST response
- creativeExtensions: creative extensions from VAST response
- width: the media file width field value from VAST response
- height: the media file height field value from VAST response
- getAllCompanions(): the list of all companions from the VAST response
- hasCompanion: expose if the ad has a companion banner information or not
