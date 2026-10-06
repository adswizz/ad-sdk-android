# AdswizzSDK - Android
The only audio-centric SDK to Drive Innovation & Differentiation in Audio monetization

## Requirements
* Android Studio Meerkat | 2024.3.1 (or later)
* (AGP) Gradle Build Tools 8.6.0 (or later)
* Minimum Android Compile SDK version 35 (or later)
* Minimum Kotlin version 1.8.22 (or later)

## Documentation

Full documentation is available at [docs.sdk.adswizz.com](https://docs.sdk.adswizz.com/#/Main/AndroidAdswizzSDK):

* [Documentation home](https://docs.sdk.adswizz.com/#/Android/README)
* [Before you start](https://docs.sdk.adswizz.com/#/Android/SDKAndroid/BeforeYouStart)
* [Get started](https://docs.sdk.adswizz.com/#/Android/SDKAndroid/GetStarted)
* [Sample projects](https://docs.sdk.adswizz.com/#/Android/SDKAndroid/SampleProjects)
* [Changelog](https://docs.sdk.adswizz.com/#/Android/CHANGELOG)

Runnable examples live in the [`Samples`](Samples) folder of this repository. Open `Samples/Examples.xcworkspace` after running `pod install`.

## Verifying releases

Every AdsWizz SDK release is signed with the AdsWizz SDK GPG key:

- Each published Maven file (`.aar`, `.pom`, `.module`) has its own detached signature, `<file>.asc`, published next to it.
- For each version we also publish a signed list of SHA-256 hashes:
    - `sha256sums/<version>/sha256sums.txt`
    - `sha256sums/<version>/sha256sums.txt.asc`

> **Note:** This verification step is optional. You can integrate and use the SDK without any of the steps below. They are an extra safeguard if you want to confirm that the artifacts in your build are the ones AdsWizz published and have not been modified.

You can verify a release in two ways:

1. **[Automatic verification with Gradle](#1-automatic-verification-with-gradle)** (recommended). Gradle checks the `.asc` signatures on every build.
2. **[Manual verification](#2-manual-verification)**. You check signatures and hashes yourself with `gpg` and `shasum` / `sha256sum`.

### Signing key

| | |
|---|---|
| **User ID** | `AdsWizz (AdsWizz SDK Key) <sdk.team@adswizz.com>` |
| **Fingerprint** | `60D9 F924 95C8 F7D6 C072  9540 DA60 9DFB E26F 9410` |
| **Algorithm** | RSA 4096 |
| **Expires** | 2028-10-04 |
| **Keyserver** | [keys.openpgp.org](https://keys.openpgp.org/search?q=60D9F92495C8F7D6C0729540DA609DFBE26F9410) |

> Always use the full fingerprint. Short key IDs can be forged.

---

### 1. Automatic verification with Gradle

Gradle's [dependency verification](https://docs.gradle.org/current/userguide/dependency_verification.html) checks every dependency each time it is resolved, using the per-artifact `.asc` signatures. You don't need to import the key with `gpg`, and you don't need `sha256sums.txt`.

From the root of your project, generate the verification file:

```bash
./gradlew --write-verification-metadata pgp,sha256
```

This command:

- Resolves every resolvable configuration in your build (all projects, plugins and `buildSrc`).
- Downloads each artifact together with its `.asc` signature.
- Fetches the signing keys from public keyservers (e.g.: keys.openpgp.org).
- Writes `gradle/verification-metadata.xml`:
    - Artifacts with a valid signature get a trusted key.
    - Artifacts without a signature get a SHA-256 checksum.

From then on, every build checks dependencies against that file. If an artifact's signature or checksum doesn't match, the build fails.

>  Example on how the AdsWizz entry will look like in your file:
>
> ```xml
> <trusted-keys>
>    <trusted-key id="ADSWIZZ_FINGERPRINT" group="com.adswizz"/>
> </trusted-keys>
> ```

Things to know:

- **All dependencies are covered.** Verification applies to every dependency in your build, not only the AdsWizz SDK. You will need to update `verification-metadata.xml` whenever you add or upgrade a dependency: run the command again, since it adds to the existing file, then review the changes.
- **Some dependencies can be missed.** Dependencies that are only resolved while a task is running may not be included. If a later build fails with a verification error for one of them, run the command again with that task instead of `help`.
- **Use `--refresh-keys` after a key update.** If we extend our key's expiry, Gradle may still be using its cached copy of the key. Pass `--refresh-keys` to download it again.

---

### 2. Manual verification

#### 2.1 Import the public key (once)

```bash
gpg --keyserver hkps://keys.openpgp.org --recv-keys 60D9F92495C8F7D6C0729540DA609DFBE26F9410
```

Or download it and import it manually:

```bash
curl -sSL https://keys.openpgp.org/vks/v1/by-fingerprint/60D9F92495C8F7D6C0729540DA609DFBE26F9410 -o adswizz-sdk.asc
gpg --import adswizz-sdk.asc
```

Check that the fingerprint matches the one in the [Signing key](#signing-key) table:

```bash
gpg --fingerprint 60D9F92495C8F7D6C0729540DA609DFBE26F9410
```

#### 2.2 Verify with `sha256sums.txt`

Use this to check many artifacts of a release at once.

**Step 1: verify the checksum file.** From the folder that contains both files:

```bash
gpg --verify sha256sums.txt.asc sha256sums.txt
```

The output should contain:

```
gpg: Good signature from "AdsWizz (AdsWizz SDK Key) <sdk.team@adswizz.com>"
Primary key fingerprint: 60D9 F924 95C8 F7D6 C072  9540 DA60 9DFB E26F 9410
```

The warning `This key is not certified with a trusted signature!` is expected. It only means you have not certified the key yourself, and it does not affect validity. If you see `BAD signature`, **do not use the release**.

**Step 2: verify the binaries.**

`sha256sums.txt` uses the standard format written by `sha256sum` (GNU coreutils). Each line holds a 64-character SHA-256 hash, and the file name:

```
<sha256 hash>  <file name>
98ea6e4f216f2fb4b69fff9b3a44842c38686ca685f3f55dc48c5d3fb1107be4  adswizz-core-8.3.0.aar
```

In check mode (`-c`), the tool reads the file line by line. For each line it:

1. Opens the named file relative to the current folder.
2. Computes its SHA-256.
3. Compares the result with the expected hash.
4. Prints `<file name>: OK` or `<file name>: FAILED`.

Put the downloaded artifacts in the same folder as `sha256sums.txt` and run:

```bash
# macOS
shasum -a 256 -c --ignore-missing sha256sums.txt

# Linux
sha256sum -c --ignore-missing sha256sums.txt
```

Every file you downloaded should print `OK`, for example:

```
adswizz-core-8.3.0.aar: OK
adswizz-core-8.3.0.pom: OK
```

`--ignore-missing` skips lines whose file is not present, so you can check only the artifacts you actually use. Any `FAILED` line means the file does not match the published release.

#### 2.3 Verify a single artifact with its `.asc` signature

Use this to check one file without `sha256sums.txt`. Download the file and its `.asc` signature, then run:

```bash
gpg --verify adswizz-core-8.3.0.aar.asc adswizz-core-8.3.0.aar
```

## Releases

Check out the [releases section](releases/com/adswizz) to browse the SDK binaries, and the [changelog](https://docs.sdk.adswizz.com/#/Android/CHANGELOG) for what changed in each version.

## License

Copyright © 2026 AdsWizz Inc. All rights reserved.

The contents of this repository are proprietary materials of AdsWizz Inc. Public access does not grant any license to use, reproduce, modify or distribute them. Any use is permitted only as expressly authorized by a separate written agreement with AdsWizz Inc. See the [LICENSE](LICENSE) file for details.