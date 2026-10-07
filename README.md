# AdswizzSDK - Android
The only audio-centric SDK to Drive Innovation & Differentiation in Audio monetization

## Requirements
* Android Studio Meerkat | 2024.3.1 (or later)
* (AGP) Gradle Build Tools 8.6.0 (or later)
* Minimum Android Compile SDK version 35 (or later)
* Minimum Kotlin version 1.8.22 (or later)

## Releases and Samples

- Check out the [releases section](releases/com/adswizz) to browse the SDK binaries, and the [changelog](documentation/CHANGELOG.md) for what changed in each version.
- Runnable examples live in the [`Samples`](samples) folder of this repository.

## Documentation

Full documentation is available at [docs.sdk.adswizz.com](https://docs.sdk.adswizz.com/#/Main/AndroidAdswizzSDK)

* [Documentation home](https://docs.sdk.adswizz.com/#/Android/README)
* [Before you start](https://docs.sdk.adswizz.com/#/Android/SDKAndroid/BeforeYouStart)
* [Get started](https://docs.sdk.adswizz.com/#/Android/SDKAndroid/GetStarted)
* [Sample projects](https://docs.sdk.adswizz.com/#/Android/SDKAndroid/SampleProjects)
* [Changelog](https://docs.sdk.adswizz.com/#/Android/CHANGELOG)

## Verifying releases

Every AdsWizz SDK release is signed with the AdsWizz SDK GPG key:

- Each published Maven file (`.aar`, `.pom`, `.module`) has its own detached signature, `<file>.asc`, published next to it.
- For each version we also publish a signed list of SHA-256 hashes:
  - `sha256sums/<version>/sha256sums.txt`
  - `sha256sums/<version>/sha256sums.txt.asc`

> **Note:** This verification step is optional. You can integrate and use the SDK without any of the steps below. They are an extra safeguard if you want to confirm that the artifacts in your build are the ones AdsWizz published and have not been modified.

The AdsWizz signing key is uploaded to `keys.openpgp.org` and fetched directly by the gradle plugin. Also, you can download the GPG key from the [AdsWizz docs](https://docs.sdk.adswizz.com/resources/code-signing/adswizz-sdk-gpg-key.asc) page.

Full documentation available at:

- **[Automatic verification with Gradle](https://docs.sdk.adswizz.com/#/Android/SDKAndroid/CodeSigning?id=automatic-verification-with-gradle)** (recommended). Gradle checks the `.asc` signatures on every build.
- **[Manual verification](https://docs.sdk.adswizz.com/#/Android/SDKAndroid/CodeSigning?id=manual-verification)**. You check signatures and hashes yourself with `gpg` and `shasum` / `sha256sum`.

## License

Copyright © 2026 AdsWizz Inc. All rights reserved.

The contents of this repository are proprietary materials of AdsWizz Inc. Public access does not grant any license to use, reproduce, modify or distribute them. Any use is permitted only as expressly authorized by a separate written agreement with AdsWizz Inc. See the [LICENSE](LICENSE) file for details.