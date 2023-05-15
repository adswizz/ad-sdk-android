# Ad Base Manager adapters

You can use the **_AdBaseManager_** **_setAdapter(adBaseManagerAdapter)_** function to provide additional URLs to be fired when an impression, quartiles or errors are fired. This works for both client-side and server-side insertion.
Also, you can remove the specified adapter by calling `removeAdBaseManagerAdapter`.

```kotlin
var adBaseManagerAdapter = object : AdBaseManagerAdapter {
      override fun urlsForCompanionClickTracking(adBaseManager: AdBaseManager, adData: AdData): List<String> {
          return listOf(
              "http://artemis.adswizz.com/1",
              "http://artemis.adswizz.com/2"
          )
      }

      override fun urlsForVideoClickTracking(adBaseManager: AdBaseManager, adData: AdData): List<String> {
          return listOf(
              "http://artemis.adswizz.com/3",
              "http://artemis.adswizz.com/4"
          )
      }

    override fun urlsForTracking(event: Tracking.EventType, adBaseManager: AdBaseManager, adData: AdData): List<String> {
        return listOf(
                "http://adswizz.com/5",
                "http://adswizz.com/6"
        )
    }

    override fun urlsForImpression(adBaseManager: AdBaseManager, adData: AdData): List<String> {
        return listOf(
            "http://adswizz.com/7",
            "http://adswizz.com/8"
        )
    }

    override fun urlsForError(errorCode: VASTErrorCode, adBaseManager: AdBaseManager, adData: AdData): List<String> {
        return listOf(
            "http://adswizz.com/9",
            "http://adswizz.com/10"
        )
    }

    override fun urlsForNoAd(adBaseManager: AdBaseManager, noAdUrlString: String): List<String> {
        return listOf(
            "http://adswizz.com/11",
            "http://adswizz.com/12"
        )
    }
}
adBaseManager.setAdapter(adBaseManagerAdapter)
```
