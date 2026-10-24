# NuvioDV

> [!IMPORTANT]
> This is a personal Android fork of
> [NuvioMobile](https://github.com/NuvioMedia/NuvioMobile), maintained only to
> cover player gaps that are not yet addressed upstream. It is not an official
> Nuvio build.

NuvioDV currently covers focused player and collection gaps:

- improved Dolby Vision handling through a libdovi-enabled libmpv build;
- an included libplacebo correction relevant to the late-playback pink-screen
  failure—the pink-screen issue is not being treated as a Dolby Vision issue;
  and
- an optional player metadata row sourced from the active decoder, showing
  resolution, codec, dynamic range, and frame rate; and
- first-class authenticated Trakt collection sources for recommendations,
  watchlists, Up Next, Recently Aired, and calendar rows.

The Android app uses the package ID `com.darkaxt.nuviodv`, the visible name
`NuvioDV`, and `NuvioDV-*.apk` output names, so it can be installed alongside
the standard Nuvio app. The Kotlin namespace remains unchanged intentionally.

This fork should be deprecated as soon as upstream Nuvio and its player stack
cover these gaps. A possible later expansion with mpvEx features is explicitly
outside the current scope.

## Patched player provenance

The Android build vendors `composeApp/libs/lib-mpv-android-libdovi-arm64.aar`
from [Darkaxt/mpv-android-libdovi](https://github.com/Darkaxt/mpv-android-libdovi):

- source commit: `a6d4de8b2109347e07dcd2daef258deee9a97e29`
- successful build: [GitHub Actions run 34656988140](https://github.com/Darkaxt/mpv-android-libdovi/actions/runs/34656988140)
- artifact: `mpv-android-libdovi-arm64`
- AAR SHA-256: `B266C9F5ED940B761DA572F1496FCF2152FE624CDFD42354B33BA5C9E3FC8721`

The native libmpv payload is ARM64-only. This integration does not claim full
Dolby Vision Profile 7 FEL decoding. Device playback validation is intentionally
performed by the fork owner as real-world samples become available.

Public NuvioDV builds are distributed as ARM64 APKs on this fork's
[GitHub Releases](https://github.com/Darkaxt/NuvioMobileDV/releases) page. They
are consistently signed by the fork's dedicated release key and can be tracked
by release-monitoring clients such as Obtainium or ObtainX. Every published build
is a normal release so update clients see one monotonic release lineage.

NuvioDV versions use `<upstream version>-nuviodv.<fork build>`. The semantic
version on the left always matches the current upstream Nuvio release; only the
final NuvioDV build counter advances while that upstream version is unchanged.

Future releases are produced by the Android-only
`Publish NuvioDV Android Release` workflow. It builds, signs, verifies, and
publishes the APK without invoking the inherited iOS or store-release paths.

The `Sync NuvioDV with upstream` workflow checks the upstream `cmp-rewrite`
branch daily. When new commits exist, it merges their history, advances both
the release-visible base version and Android version code, verifies the fork's
identity and patched player in a full Android build, pushes the merge, and then
dispatches the release workflow. Unexpected merge conflicts fail closed for
manual maintenance; no commit or release is created when upstream is unchanged.

## Native Trakt collection sources

The collection editor supports both public Trakt lists and authenticated
account catalogues. Account catalogues reuse the Trakt account connected under
Nuvio settings for the active profile; they do not require an AIOMetadata addon
or a second Trakt login. Supported native rows are movie and series
recommendations, movie and series watchlists, series Up Next, series Recently
Aired, and a configurable series calendar window.

The reconciled import that migrates the seven existing AIOMetadata-backed Trakt
rows to these native sources is available locally at:

`C:\Users\darka\Documents\Projects\Stremio Add-on Tester\output\nuvio-collections-kaptain-mega-hybrid-2026-09-18-nuviodv-trakt.reconciled.json`

Connect Trakt in Nuvio settings before opening those rows. Existing imported
public-list sources remain compatible and keep their list sorting controls.
Exports include the positive list-ID compatibility sentinel required by the
deployed `nuvio.tv` validator while retaining explicit native account-source semantics.

## Upstream project

<div align="center">

  <img src="https://nuvio.tv/assets/nuvio-app-logo-wordmark.webp" alt="Nuvio" width="320" />

  <p>
    A free, open-source media app for your phone, your desktop, and the TV you already own.
    <br />
    Bring your own sources. Nuvio turns them into a library with artwork, ratings, subtitles, and your place saved on every screen.
  </p>

  [Website](https://nuvio.tv) · [GitHub releases](https://github.com/NuvioMedia/NuvioMobile/releases/latest) · [Support Nuvio](https://nuvio.tv/support)

</div>

### Get official Nuvio Mobile

- [Android on Google Play](https://play.google.com/store/apps/details?id=com.nuvio.app)
- [Android APK](https://github.com/NuvioMedia/NuvioMobile/releases/latest)
- iOS via AltStore or SideStore: add [this source URL](https://raw.githubusercontent.com/NuvioMedia/NuvioMobile/cmp-rewrite/store.json) in the app's Sources section, then install Nuvio.

## Build from source

```bash
git clone https://github.com/Darkaxt/NuvioMobileDV.git
cd NuvioMobile
```

### Android

Android development requires Android Studio and the Android SDK.

```bash
./gradlew :androidApp:assembleFullDebug
```

### iOS

iOS development requires macOS and Xcode.

```bash
env NUVIO_IOS_DISTRIBUTION=full xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -sdk iphonesimulator \
  -derivedDataPath build/ios-derived-full-simulator \
  CODE_SIGNING_ALLOWED=NO \
  build
```

The shared app is built with Kotlin Multiplatform and Compose Multiplatform.

## License

[GNU General Public License v3.0](./LICENSE)
