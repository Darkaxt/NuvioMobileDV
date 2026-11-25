# NuvioDV Upstream Maintenance Specification

Authorization: `already_authorized` by the user on 2026-09-12.

## Requirements

### 2026-11-25 Android maintenance (ACTIVE)

Owner: Claude NuvioDV maintenance task. Daily sync 45419876570 failed closed on
three conflicts with upstream 0d20af832 / 1.1.0-beta.3 (47 commits): both
Vietnamese string files and Version.xcconfig. The fork's vi changes were
line-ending-only (content identical ignoring CR), so upstream content is taken
with the fork's LF normalization; the version helper advances to 1.1.0-nuviodv.6 /
12275. Upstream workflows are unchanged. Retained upstream ASS/SSA styling,
libmpv 60sp subtitle maximum, ExoPlayer libass, addon subtitle runtime state and
updated Media3 common/extractor AARs; their new mpv property calls compile against
the pinned 0.1.12 wrapper. Identity, AAR hash, D4 failover, Animation toggle and
Anime4K MAIN_raw shaders are retained.

Local verification: sync/version/RPDB/release contracts and fork invariants pass;
gated readiness b875be01e962600230c21266bc939acf passes the focused
player/playback host tests (61 suites, 0 failures) and the full debug APK
(package com.darkaxt.nuviodv, 1.1.0-nuviodv.6 / 12275, label NuvioDV, ARM64-only
player). Nine AAR libraries are byte-identical in the APK; libc++_shared differs
only by the known AGP stripping. Signed release and public APK checks follow.

### 2026-11-20 Android maintenance (COMPLETE)

Fast-forwarded the clean checkout to daily-delivered 0df56f67e, which contains
inspected upstream e022cfdf7. Sync 44741250885 and release 44534094643 succeed.
Normal v1.1.0-nuviodv.3 / code 12272 is independently downloaded: APK SHA-256
4a7f7051507bcb2125dea77ac98cc1229a6a4ae3cc8c630efe15a9fba958b159. Existing signer
1FB94424753A90F993C678B6FA4322579253BB303F22C335DB395A9E2557D571 and v2 signature
pass, package com.darkaxt.nuviodv and ARM64 identity match. All ten native libraries
are byte-identical to the unchanged qualified AAR e8f5e4063a2db82a78c84350d8170ec7951a301488248b82b1bae9a4779bf02a.
Non-empty SIMKL/MDBList/Trakt client identifiers match the authenticated local
configuration by hash and are present in delivered DEX; updater targets this fork.
No source correction, rebuild, duplicate release or device access is needed.
Wrapper 27bde9f7c contains FortunasXP fee434bfc with zero missing commits; successful
42532012258 / artifact 13637774468 is unexpired. Full cross-repository reconciliation
is tracked in Desktop NUVIODESKTOP_PARITY_SPEC.md.

### 2026-11-17 Android maintenance (COMPLETE)

The November 17 cross-repository ledger is in Desktop
`NUVIODESKTOP_PARITY_SPEC.md`. Existing heartbeat authority covers the complete
normal delivery. Start state was clean, fast-forwarded to already delivered
`949e6a5df` / `0.6.13-nuviodv.2`. Daily sync `44094106133` failed before compilation
on application, version and upstream workflow conflicts. Inspected upstream is
`b2e3d67ba` / `1.1.0-beta.1` (66 commits). Its prerelease suffix describes the
upstream source; this personal fork still publishes only normal releases.

Bounded sync corrections: restore owned workflow files before classifying merge
conflicts, continuing to abort on any application conflict; normalize valid
upstream prerelease names to their semantic core for the normal fork version.
Real Git fixture regression reproduces the workflow conflict failure while its
application-conflict variant still fails closed. Version regression reproduces
rejection of `1.1.0-beta.1`. Both corrections retain existing helpers and need no
new architectural layer. Release version becomes `1.1.0-nuviodv.1` / `12270`.
The workflow regression also proves that a missing upstream ref / failure before
Git creates MERGE_HEAD remains an error. This CI-only guard correction does not
change Android compiled inputs; repeat the source contracts without rebuilding
unchanged application code solely for workflow text.

Upstream also migrated its Maven mpv wrapper to 0.2.1. The fork retains the exact
qualified AAR / 0.1.12 API and its create/init/destroy lifecycle, pre-init process
safety options and shader behavior; do not adopt constructor/cache/close calls
from the newer wrapper. Retain upstream subtitle-cue improvements, Plex/Silo
artwork/provider changes, realistic watched fixtures and all other non-workflow
history. RPDB, episode-artwork priority/queue, local credentials, identity,
classification choices and Anime4K remain required invariants.

All source paths must be reconciled before the managed focused Android tests and
integrated full debug APK. Final signed workflow must also pass the complete
Linux host suite and generated runtime configuration checks. Independently check
the normal release/tag/source, downloaded APK hash, signer/package/version/code,
provider identifiers, updater and pinned native payload. No Android endpoint is
needed for these gates; device playback is not claimed.

Integrated validation now passes: readiness `47acc861fd6a543dcef6d15f611b7686`,
original workflow `d8619b22d418e8b423a1e1427fc7b047`, native exit 0 / verified
whole-tree and daemon exit. Adapter independently reports acquisition of
`Local\Darka.AndroidGradleBuildGate`, observed two workers/no parallel projects
and a 3 GiB Gradle heap, configured 3 GiB Kotlin daemon heap, one 512 MiB test
fork, no memory failure; peak aggregate working set 5385572352 bytes. Same
Kotlin 2.4.10 toolchain retains its established daemon strategy. Regular Python
is selected only in this workload's PATH because the ambient `python` now resolves
to SVP's isolated embedded distribution; no global environment was changed.

Only the CI regression/merge guard and documentation changed after readiness;
hash comparison proves every compiled application input is unchanged. Final
portable contracts pass, so no duplicate app compilation is required for that
CI-only correction. Local full APK SHA-256
`ddac58b2b071a1f1a169be49a87b80431916b550d0a202c9d82399e91ca940be`
has the expected package/version/code/label, ARM64-only player and both libdovi
exports. Nine pinned libraries match byte-for-byte; AGP's stripped local C++
runtime retains every allocated ELF section. Gate-owned native caches were
reclaimed by Coordinator after process exit, and its remaining three spent log
members / 157474 bytes were removed immediately through reviewed transaction
`60aaa0886477deb7ec9f991da22e96dd`.

Delivery passes: original signed run `44188988989` completes the full Linux host
suite, signing, generated configuration/native checks and normal publication from
`60c33e2d775b5f483f95fe58efc974e33e37e809`. Independently retrieved normal
`v1.1.0-nuviodv.1` resolves to that source. APK SHA-256
`1707d8ec218dfab700239fa692bae0a59113f5969272bd9ff58d36474c1d4984` matches GitHub
and the release notes. Package/label/version/code, existing signer
`1FB94424753A90F993C678B6FA4322579253BB303F22C335DB395A9E2557D571`, v2 signature,
all ten exact pinned native hashes, both defined libdovi exports, fork updater and
SIMKL/MDBList/Trakt client hashes pass. Fresh no-op daily sync `44190047181` is
successful and inspected upstream remains contained. No device access or device
playback validation is claimed. Remaining cross-repository maintenance is tracked
in the Desktop ledger rather than treated as Android delivery work.

Final reconciliation M1 repairs a separate evidenced checkout-cleanup defect.
The actual recursive Git cleanup command exits 128 on unused orphan gitlink
`libass-android`; a new index/mapping regression additionally identifies
`vendor/quickjs-kt` and `vendor/TorrServer`. Their uninitialized directories are
empty and no build configuration includes those source roots. Actual dependency
resolution continues through bundled AARs / declared artifacts. Remove only the
three unused index pointers, preserving all working directories, AARs and the
legitimate MPVKit mapping. The regression fails before correction, then the actual
recursive Git command and all portable source/authentication contracts pass.
Only Git metadata, that source contract and this evidence change; no compiled
application input or native pin changes. The delivered `v1.1.0-nuviodv.1` remains
the exact qualified application, so this CI-only correction needs no new APK.
Fresh no-op daily sync `44203646649` at repair source `a8752bb08` passes after
the push, confirms inspected `b2e3d67ba` is already contained, and completes its
actual post-checkout cleanup without the prior Git exit-128 warning. No new build
or release is generated by that no-op. All compiled inputs and the delivered APK
remain unchanged. Final issue/release/source reconciliation is recorded in the
November 17 Desktop ledger.

### 2026-11-14 maintenance and issue delta

Android was synchronized at the initial check: clean local fast-forward to
`406f88814`, with zero missing commits from inspected upstream `ba149964c`. Daily sync `43590697115`
and normal signed release `v0.6.12-nuviodv.7` / run `43591048141` are successful.
Preserve the existing qualified Android AAR and shipped Anime4K settings/behavior;
unchanged native artifacts do not require another native build. Final refresh
then found 18 newly arrived commits at upstream `6b7f15e40`. That later source
boundary is now integrated and delivered by `1c1847736` as the normal release
`v0.6.13-nuviodv.1`; final refresh confirms zero missing upstream commits.
Android library `27bde9f7` contains inspected FortunasXP tip `fee434bfc` and its
successful AAR delivery run remains `42532012258`.

Late-sync source is integrated as `0.6.13-nuviodv.1` / `12268`: upstream loading
stages coexist with fork stream metrics; upstream mapped profile state retains
animation preload. The new benchmark/non-minified variants and generator target
`com.darkaxt.nuviodv.benchmark`, preserving production/debug fork identity and
avoiding upstream benchmark collisions. Workflows and the AAR hash remain exact.
Vietnamese line-ending normalization preserves parsed XML values. Portable
version/sync/RPDB/authentication contracts pass. Local readiness
`ae467df644da00af38823dd07a6ef505`, workflow `579d44b4e9c942769e028cc710d7c8bc`,
passes focused loading/Anime4K tests and the full integrated debug APK with native
exit 0 and verified whole-tree exit. The shared Gradle gate used two workers,
no parallel projects, 3 GiB Gradle/Kotlin daemon heaps and one 512 MiB test fork;
peak aggregate working set was 7687708672 bytes, with no memory failure or budget
increase. Kotlin 2.4.10 retains the established daemon strategy. An initial shell
contract probe resolved Windows' WSL `bash.exe` and exited 126; the explicit Git
Bash runtime passes the same contracts. No WSL build was run.

Debug APK SHA-256 `f491919d27fe928db247f51537cf1f15e335022d7d290fd2d9d4a3125440986e`
has the correct package/name/code/label and ARM64-only patched player with both
libdovi exports. Nine AAR libraries are byte-identical, including libmpv
`1c5bf5e2256b3a20fb41617cbd5d47b017d821c512f8dc850d88e617c1d0f8f8`.
The C++ runtime differs only through AGP symbol stripping: merged-native bytes
match AAR `0c52cfab...`, stripped-native bytes match APK `c4c2fe5c...`, and every
allocated ELF section matches the original. This diagnostic mismatch required no
production change. Android application delivery is COMPLETE: authenticated run
`43654900049` passes the full Linux host suite, signing, generated configuration,
package/native checks and normal publication from exact source
`1c1847736779dcf9389fe454c3bef416bf37c75e`.

The independently downloaded public APK is SHA-256
`cb631f31fb6a10498cd5ed243a8db1c5cb7097eed659fba2383a9ce3ac3323a5`, matching
GitHub's asset digest and release notes. Actual package/label/name/code, ARM64-only
payload, existing signer `1FB94424753A90F993C678B6FA4322579253BB303F22C335DB395A9E2557D571`
and v2 signature pass. All ten public native library hashes match the pinned AAR
(the public C++ runtime is unstripped). Both libdovi exports, fork updater and
packaged SIMKL/MDBList/Trakt client hashes pass. All 62 retained mobile releases
are normal and the latest endpoint resolves to this exact release. The daily
sync remains successful; wrapper AAR delivery remains unexpired. No Android
device was accessed or device playback validation claimed. The exact completed
local gate scratch was removed via reviewed transaction
`d06866618e44d001da5601dc918610fb` (10 members / 3362963 bytes).

The open-issue API delta since `2026-11-11T21:31:00Z` returned 31 entries in its
complete first page (fewer than the requested 100): 18 issues and 13 pull requests.
Issue bodies and all comments on the six commented issues were reviewed. Baselines
#1730, #1675, #1723, #2444, #2391, #2395, #1956, #1972 and #2574 have no post-cutoff
updates. #1730 remains the high-relevance historical descriptor-exhaustion baseline;
#1675/#1723 provide no new evidence connecting them to this fork's native path.
No new DV/libplacebo/GPU-next/hardware-decoder/pink-output or long-playback failure
evidence was found. #2655 requests a Sinhala subtitle font without a crash or
native-pipeline trace. #2661 is an unverified code-review claim about offline
progress uploads, and #2662 concerns missing bingeGroup source matching, observed
on iOS; neither establishes a native rendering failure. Other updates concern UI,
catalog order, provider proposals and download features. No issue-driven patch or
upstream comment was made. Next delta cutoff: `2026-11-14T23:21:00Z`, deliberately
slightly overlapping this retrieval so issues updated during review are not lost.

Existing heartbeat `maintain-nuviodv-upstream-fork` was updated in place to preserve
the already shipped Windows AnimeJaNai/SVP and Android Anime4K behavior, current
native feature checkout and exact application pins. Read-back verifies the same
active three-day cadence, target chat and quiet no-change notification intent.
Desktop/native delivery and cleanup evidence are recorded in the November 14
section of `NUVIODESKTOP_PARITY_SPEC.md` in the Desktop checkout.
All four monitored branches contain their final inspected upstream tips. Final
reviewed transaction `8765ae50f6f55f7b288f5c7c83b53ece` removes the spent E: copies
and exact local preflight MSI/debug APK (60 members / 741102037 bytes), with no
exclusions or residuals. Public releases and installed Windows state are verified;
no maintenance blocker or tracked deferral remains.

### 2026-11-11 Android maintenance delivery

Merge `ac06ad0e2` integrates inspected upstream `4135f96cd` (0.6.12). It resolves
the `FolderDetailRepository.kt` conflict by retaining fork Trakt routing through
`source.catalogRouteKey()` and upstream's new folder-label setting. The qualified
AAR remains unchanged at `e8f5e406...`; identities, RPDB, credentials and updater
routing are preserved. All source contracts pass.

Normal, non-draft release
[`v0.6.12-nuviodv.1`](https://github.com/Darkaxt/NuvioMobileDV/releases/tag/v0.6.12-nuviodv.1),
code `12261`, targets exact source `e261d52ac07911b6dcad7f2a968a11ec7cf80f3d`.
Run `43089796486` passes the complete Android host suite on Linux, signed APK
assembly, generated authentication checks and package/signature verification.
Independent public download SHA-256 is
`0ca27acd37c6d9eaceb83cd58956946b437ec34b244e2fa9e216c4de08179f8e`.
Actual APK inspection verifies package/label/version/code, ARM64-only code,
existing signer `1FB94424753A90F993C678B6FA4322579253BB303F22C335DB395A9E2557D571`,
valid v2 signature, renamed fork updater and all ten AAR libraries byte-for-byte.
Device playback was not claimed or accessed.

Windows readiness receipt `ca89a6f179c27cf16a81149fb0eb4fcb` retains original
failed workflows `8b18a08b4a4a8859935836faa5909092`,
`2d44ee0d90ff25bf31bf46e854303798` and `bdca42c21f4605dfd0e3844832131384`.
They establish Windows/Robolectric POSIX rename and FileProvider path mismatches,
not an Android download production defect. Experimental shadows were removed.
Original-receipt attempt 4 (`2c57a206c1908e2018d804f1bdaa7690`) passes affected
portable tests and full debug APK assembly for the merge source. Its managed
native exit is zero with verified process-tree exit, two workers/no parallel
projects, 3 GiB Gradle/Kotlin daemon heaps and one test fork. No memory failure
occurred; compiler scratch and debug APK were removed through reviewed cleanup.

Linux run `43088040328` then found two missing post-credits navigation targets,
an obsolete one-second completion fixture and a concurrent Trakt test recorder.
Source `e261d52ac` restores upstream's post-/mid-credits target category while
retaining long-credit handling; test fixtures use realistic content duration and
a Mutex-protected recorder. The full Linux suite and final signed build verify
these corrections. No extra local build or successful-receipt replay is claimed.
The normal release workflow now enforces the full Linux suite before packaging,
using two workers/no parallelism and a 3 GiB heap shared with in-process Kotlin.
The post-repair no-update check passed on original sync run `43084002957`,
attempt 2, without merge, commit, push or release. Android maintenance is COMPLETE;
no blocker or tracked deferral remains for this delivered scope.

### 2026-11-11 issue delta review

Paginated open-issue retrieval covered all 34 issues updated since November 8
midnight; 28 meet the previous heartbeat cutoff `2026-11-08T19:04:48Z`.
Relevant bodies and all their comments were inspected, plus baseline reports
#1730, #1675, #1723, #2444, #2391, #2395, #1956 and #1972. None of those baselines
has new comments since that cutoff. #1730 remains the high-relevance historical
descriptor-exhaustion/leak-class baseline; #1675/#1723 gain no new player-path
evidence. #2613 and #2564 concern iOS player transitions, not the Android libmpv
path. #2377 gained a feature upvote, not native failure logs. No new actionable
DV/libplacebo/GPU-next/decoder/subtitle-audio pipeline evidence was found.

Existing [#2574](https://github.com/NuvioMedia/NuvioMobile/issues/2574), updated
November 8 at 15:02 UTC (before the preceding heartbeat), reports purple output
after 15-20 minutes while audio continues on Motorola E7 Plus / Android 10,
upstream 0.6.10-beta, with YUV420P compatibility disabled; reopening recovers.
It has no logs or explicit renderer/decoder/descriptor evidence and is not a new
post-cutoff finding or proof that the fork's pinned leak fix regressed. Retain it
as an unconfirmed candidate if engine/renderer and FD logs become available.
No issue-driven change or upstream comment was made. Next delta cutoff is this
review's retrieval, November 11 at approximately 21:31 UTC.

### Current authorized native-player update (2026-11-08)

The user requested updating Android mpv and consuming the result in NuvioDV.
Use the independently qualified AAR from library source
`84c7e40cb83ba66918edf4ce811b4a177b742c79`, workflow `42532012258`, SHA-256
`e8f5e4063a2db82a78c84350d8170ec7951a301488248b82b1bae9a4779bf02a`.
It advances mpv to `8bc468896ec7` and libplacebo to `9fabf6d5057c` / API 387,
retaining the existing FFmpeg/libdovi pins, RPU/P010 patches and wrapper API.

The cross-repository authoritative ledger is
`D:\Projects\mpv\DOCS\NUVIODV_WINDOWS_RUNTIME_SPEC.md`. Native qualification is
COMPLETE; this application's integration/verification stage is COMPLETE. Consume
the exact artifact, keep base version `0.6.10`, advance the fork counter to `2`
and Android code to `12259`, pass existing contracts and the required integrated
Android build, then publish and independently verify the signed normal release
at the exact completed source. Preserve all other fork features and runtime
credentials. Both automated sync and release builds must check one shared native
checksum. Device playback remains owner validation; do not claim it from a build.

Rollback restores the prior AAR from Git history (SHA-256
`b266c9f5ed940b761da572f1496fcf2152fe624cdfd42354b33ba5c9e3fc8721`) and
publishes it with a new higher version code; do not reset app data or attempt an
automatic Android version-code downgrade. No application-stage deferrals.

Verification: version/sync/RPDB contracts and authenticated release-configuration
checks pass. Gate ticket `009a8bd351384cb6917a797357760a92` acquired
`Local\Darka.AndroidGradleBuildGate` and completed the focused player engine-switch,
technical-details and settings tests plus `:androidApp:assembleFullDebug`.
Observed profile: two workers, no parallel projects, Gradle 3 GiB heap with
in-process Kotlin sharing it, one 512 MiB test fork, preserved required JVM flags.
No memory failure; single-use Gradle daemon exit verified. Its D: compiler scratch
was removed through the reviewed cleanup helper after lifecycle completion.

The built APK has package `com.darkaxt.nuviodv`, label `NuvioDV`, version
`0.6.10-nuviodv.2` / `12259`. Its libmpv, JNI and FFmpeg payloads match the qualified
AAR byte-for-byte. Its stripped C++ runtime retains the AAR's build ID
`ba615e4ff75bc2d8ba1eee93f983e02bc427c433`.

Final delivery is COMPLETE: run `42535938912` published normal, non-draft release
[`v0.6.10-nuviodv.2`](https://github.com/Darkaxt/NuvioMobileDV/releases/tag/v0.6.10-nuviodv.2)
from exact source `82c8dada01798eabf6111a9f473e794cc3ee0163`.
Independently retrieved APK SHA-256
`3daaf2c07f29eb7e6e0c535a7b2625f9083707a81592f19c2447a48209362e29`
matches public release metadata. Package/version/code/label and ARM64-only code
pass actual APK inspection. Signature Scheme v2 verifies with the existing
certificate SHA-256 `1FB94424753A90F993C678B6FA4322579253BB303F22C335DB395A9E2557D571`.
All ten qualified AAR native libraries, including its C++ runtime, match the
published APK byte-for-byte. Its updater references `Darkaxt/NuvioMobileDV`.
Generated Supabase/Trakt/SIMKL/MDBList configuration checks passed in the signed
release workflow. No blocker or tracked deferral remains for this native update.
No device playback claim is made.

- M1: A GitHub Actions workflow must check `NuvioMedia/NuvioMobile` branch `cmp-rewrite` daily.
- M2: When no upstream commit is missing from `Darkaxt/NuvioMobileDV:cmp-rewrite`, the workflow must not create a commit, push, or release.
- M3: When upstream commits are missing, merge their history into the fork without discarding NuvioDV changes. The workflow may automatically preserve the fork's `iosApp/Configuration/Version.xcconfig` during a conflict limited to that file; any other merge conflict must fail closed for manual repair.
- M4: Each synchronized release must retain the exact current upstream semantic version as its base and advance the Android version code. If upstream remains on the same version, increment only the `-nuviodv.#` fork counter; if upstream advances its version, adopt it and reset the fork counter to `1`; fail closed if upstream appears older than the fork base.
- M5: Before pushing, verify the NuvioDV package identity, app label, local patched libmpv dependency, authenticated-release test, and a full Android debug APK build. A failed invariant or build must prevent the push and release.
- M6: After pushing the verified sync commit, dispatch the authenticated NuvioDV release workflow with that exact commit as an explicit checkout input. Every published NuvioDV build must be a normal GitHub release; branch-ref propagation must not be allowed to publish an older commit.
- M7: A recurring Codex check must run every three days. It must inspect upstream divergence and daily-workflow/release results; if GitHub automation failed or left commits unsynchronized, it is authorized to diagnose, make the minimal fork-preserving repair, verify, commit, push, and trigger the release itself.
- M8: The three-day check must also inspect new or newly updated open upstream issues for playback problems relevant to NuvioDV, libmpv, Dolby Vision, libplacebo/pink output, hardware decoding, renderer/color-format behavior, subtitles/audio interaction, or long-playback pipeline failure.
- M9: Issue monitoring must not comment on upstream issues or automatically broaden the fork. It reports only evidence-backed candidates worth considering and stays quiet when neither maintenance nor issue triage produces an actionable change.
- M10: Public release history must contain only tags whose left-side version matches the upstream release represented by their source commit. Superseded misnumbered releases and tags must be removed after their audit metadata is recorded.
- M11: The four releases retained by the 2026-09-14 lineage repair remain valid historical releases. Later verified sync releases may extend that set; every current release must be normal and the latest release must be the newest verified NuvioDV version.
- M12: The NuvioDV in-app updater must query `Darkaxt/NuvioMobileDV` normal releases, not the upstream `NuvioMedia/NuvioMobile` feed.
- M13: Upstream synchronization must preserve the fork's `.github/workflows` tree so the default GitHub Actions token never attempts a forbidden workflow-file push; upstream application history and all non-workflow files remain mergeable.
- M14: Authenticated NuvioDV releases must include every runtime client identifier required by an integrated account provider. In particular, `MDBLIST_CLIENT_ID` must be injected into the release build and the workflow must fail closed before publication when it is absent or when generated `MdbListConfig.CLIENT_ID` is blank.
- M15: The personal mobile fork must be named `Darkaxt/NuvioMobileDV`; the local `origin`, README clone/release links, updater endpoint, updater contract, recurring maintenance automation, and final normal release must all use the renamed repository without changing the upstream remote, package identity, signing identity, patched player, or maintenance behavior.
- M16: Install the verified renamed-repository release on the user's actual phone and tablet, preserving existing NuvioDV application data when present and leaving the standard Nuvio package untouched, and repoint or create each device's ObtainX NuvioDV source entry for `Darkaxt/NuvioMobileDV`. Identify every endpoint by ADB serial and model before mutation; AYN Thor is not the phone or tablet and must not be used as a substitute.

## Acceptance criteria

- MC1: Version-advance tests prove same-base fork-counter increments, newer-upstream resets, older-upstream rejection, and a higher Android version code.
- MC2: The daily workflow parses, has write permissions needed for source sync and release dispatch, and contains fail-closed merge/build behavior.
- MC3: A manual workflow dispatch consumes the currently pending upstream commits, preserves fork invariants, pushes the verified merge, and dispatches a successful authenticated normal release.
- MC4: A three-day Codex heartbeat is active with the authorized remediation and playback-issue scope.
- MC5: Repository changes and maintenance evidence are committed and pushed to `cmp-rewrite`.
- MC6: Release-policy tests reject prerelease inputs/flags and prove the daily sync dispatches the normal release workflow.
- MC7: A fresh GitHub API audit proves every retained release is normal and the latest endpoint resolves to the newest verified NuvioDV release.
- MC8: A regression contract fails against the upstream updater owner and passes only when the in-app release feed owner is `Darkaxt`.
- MC9: A regression contract proves upstream workflow-file changes are restored to the pre-merge fork tree before commit/push, while a real pending-upstream sync reaches zero commits behind and publishes the exact verified normal release.
- MC10: Release-configuration tests reject a missing MDBList client identifier, preserve a supplied identifier in `local.properties`, and the release workflow verifies that generated `MdbListConfig.CLIENT_ID` is non-empty before publishing.
- MC11: GitHub reports the public fork as `Darkaxt/NuvioMobileDV`; the local `origin` and current routing references resolve to that repository, the old URL redirects, the updater contract passes, and a higher-version normal release from the renamed repository is independently verified.
- MC12: ADB reports the intended phone and tablet by explicit serial/model, each has the exact public APK installed at version code `12248`, each retains pre-existing NuvioDV data when present without modifying standard Nuvio, and each ObtainX entry resolves the new repository's normal release feed rather than the old repository name.

## Stage and reconciliation ledger

| Stage | Status | Requirements | Verification |
| --- | --- | --- | --- |
| Daily verified upstream sync and release | COMPLETE | M1-M6 | Runs 34665705660 and 34666617695 consumed the initial pending commits; source-SHA input fixes branch-ref propagation race; run 34667333366 verified the unchanged-upstream no-op path; version contract corrected in `61e4eb15`; on 2026-09-13 run 34723027169 failed closed on four overlapping player-settings files plus the expected version conflict, after which merge `b62001d` retained both upstream settings and NuvioDV playback details and release run 34723777285 published verified prerelease `v0.4.18-nuviodv.1`; on 2026-09-14 sync run 34786755533 consumed seven upstream commits without conflicts and release run 34787071223 published verified prerelease `v0.4.19-nuviodv.1` from exact source `4ea1125` |
| Three-day Codex maintenance and issue triage | COMPLETE | M7-M9 | Active heartbeat `maintain-nuviodv-upstream-fork` runs every three days with authorized remediation, quiet no-change behavior, and issue-delta triage; baseline reviewed all 343 open issues and records #1730 as high relevance while #1675/#1723 remain low confidence |
| Public release-lineage repair | COMPLETE | M6, M10-M11 | Commit `3f063f84` removed the prerelease input/flag paths and made normal releases the enforced workflow contract; invalid historical releases/tags were deleted after audit, and subsequent releases through `v0.4.23-nuviodv.1` are normal and preserve upstream-aligned left-side versions |
| Fork updater and workflow-permission repair | COMPLETE | M12-M13 | Commit `79a4adf1` points the regression contract and updater at the fork and preserves the fork workflow tree; upstream refactored the updater during the pending batch, so merge `79a32bc0` retained that refactor while routing `AppUpdaterRepository` to `Darkaxt/NuvioMobile`; local full-debug build passed, release run 35336010585 published the exact normal release, and no-update sync run 35336838855 passed |
| MDBList release credential repair | COMPLETE | M14 | Root cause confirmed against public 0.6.2 APKs: upstream embeds a non-empty MDBList client ID while NuvioDV `0.6.2-nuviodv.1` embeds an empty value. Configuration and workflow regression contracts pass; GitHub secret `NUVIODV_MDBLIST_CLIENT_ID` is installed; focused Gradle generation under gate ticket `ea1a025caff840f296125a187bf06751` produced a non-empty `MdbListConfig.CLIENT_ID` with the required 3 GB/two-worker profile and verified daemon exit. Release run `39348011632` failed closed because the negative test inherited the job-level MDBList secret; commit `94ea9777` isolated that test with `env -u`, and run `39348153891` then published normal release `v0.6.2-nuviodv.2` from exact source `94ea977705107ec93ad6b04e072ac2fb23748c5c`. The independently downloaded ARM64 APK has SHA-256 `990A41B23D392BF2E77597B98A036D78BEA9F974E4B25C57557B81E72290947D`, package `com.darkaxt.nuviodv`, version code `12244`, version name `0.6.2-nuviodv.2`, the expected signer, ARM64-only patched libmpv with libdovi symbols, and exactly one 40-character opaque identifier shared with upstream but absent from the broken prior NuvioDV APK. |
| 2026-10-21 upstream 0.6.3 sync and release | COMPLETE | M1-M6, M13-M14 | Upstream advanced by 35 commits. Scheduled run `39576735468` failed closed on `values-vi/strings.xml` plus the expected version conflict. The Vietnamese upstream resource contains every fork key, so merge `b83cff1e` resolves the conflict to the complete upstream translation and advances the fork to `0.6.3-nuviodv.1` / `12245`. Fork contracts pass, the integrated full-debug APK has the correct NuvioDV package/version/label and patched ARM64 libmpv, and focused player-detail/badge tests pass. Gradle gate tickets `25c30c04f05241378e2c9d509a8f3171` and `d338fbeff2224df89c4990fb5b0554cb` both acquired `Local\\Darka.AndroidGradleBuildGate`, enforced two workers/no parallelism/3 GB Gradle and Kotlin heaps, reported no memory failure, and verified daemon exit. Release run `39620264955` published normal release `v0.6.3-nuviodv.1` from exact source `b83cff1efa8f0c1b1f0b0c7b47c33203db8eb513`. The independently downloaded ARM64 APK has SHA-256 `F74C9D8669EC2EB3353B3EF790753006558CF1BD32AEF77B30022474AD4786BB`, package `com.darkaxt.nuviodv`, version code `12245`, version name `0.6.3-nuviodv.1`, the expected signer, ARM64-only patched libmpv with libdovi symbols, and exactly one 40-character opaque identifier shared with upstream but absent from the broken `0.6.2-nuviodv.1` APK. |
| 2026-10-24 upstream 0.6.4 sync, triage, and release | COMPLETE | M1-M9, M13-M14 | Upstream advanced by 59 commits after automated release `v0.6.3-nuviodv.2`. Scheduled run `40079921639` failed closed on the Vietnamese resources, `PlayerControls.kt`, `PlayerTimeline.kt`, and the expected version conflict. Root-cause comparison shows upstream removed the source/provider timeline row and migrated both sliders to explicit `SliderState`, while NuvioDV independently added the optional stream-derived technical-details row across the same player signatures. Merge `ded4f9ab592d37f2f95933b57171204ca45fdec6` includes exact upstream `c114d6fb`, retains both behaviors, preserves the fork workflow tree and patched libmpv hash, and advances to `0.6.4-nuviodv.1` / `12247`. Fork contracts, focused player/libass verification, and the full debug APK pass; Gradle gate tickets `7855720ed7e942e29ec320dc0f9f9c2f` and `3e7d1a4226f04f51858fe75aad3c4f66` acquired `Local\\Darka.AndroidGradleBuildGate`, enforced two workers/no parallelism/3 GB Gradle and Kotlin heaps, reported no memory failure, and verified daemon exit. The debug APK has the expected package/version/label and one ARM64 libmpv with libdovi symbols. Issue-delta review found no new direct DV, pink/purple, libplacebo, or long-playback failure evidence; issue `#2358` is a low-confidence device-decoder request without logs, and updated `#2242` adds only OS-overlay context. A malformed manual source input made release run `40104507243` fail closed at checkout without creating an asset or tag; corrected run `40104576998` published normal release `v0.6.4-nuviodv.1` from the exact merge source. The independently downloaded public APK has SHA-256 `9278BC8721AFF697B938AF5583725519228987CD5D6C8E540C67859E2040524A`, package `com.darkaxt.nuviodv`, version code `12247`, version name `0.6.4-nuviodv.1`, label `NuvioDV`, the expected signer and v2 signature, ARM64-only patched libmpv with libdovi symbols, and exactly one current opaque credential identifier shared with the last verified good NuvioDV APK but absent from the broken `0.6.2-nuviodv.1` APK. |
| Rename fork to NuvioMobileDV and replacement delivery | COMPLETE | M2, M4-M6, M12-M16 | Repository rename, current routes, maintenance automation, version advance, gated local build, exact-source normal release, independent public APK verification, and both device halves of MC12 are satisfied. Samsung phone `RFCY80551LT` / `SM-F966B` has the exact public APK at `0.6.4-nuviodv.2` / version code `12248`; this was a first NuvioDV install, so no NuvioDV data existed to migrate, while `com.nuvio.app` remained installed and untouched. ObtainX `2.21.0` cold-started, consumed the new `com.darkaxt.nuviodv` record, refreshed it from `Darkaxt/NuvioMobileDV`, and resolved the live GitHub API asset. Samsung tablet `R52W60CFTRL` / `SM-X910` upgraded from `0.6.2-nuviodv.2` / `12244` to the same exact public release while retaining `firstInstallTime=2026-09-12 04:22:48` and `dataDir=/data/user/0/com.darkaxt.nuviodv`. Its ObtainX `2.9.9` record changed from `Darkaxt/NuvioMobile` to `Darkaxt/NuvioMobileDV`, now carries installed/latest versions `0.6.4-nuviodv.2` / `v0.6.4-nuviodv.2` and the canonical nested live asset URL, and cold-started without fatal or JSON parse failures. NuvioDV cold-started without fatal failures on both endpoints. AYN Thor was never modified. |
| 2026-10-27 upstream 0.6.5 sync, triage, and release | COMPLETE | M1-M9, M13-M14 | Scheduled run `40484021975` failed closed because upstream's complete Vietnamese localization changed line endings and added four keys, creating a non-version conflict alongside the expected version conflict. Merge `c4e2a6388850263ce925b8d0a1f07cf702623450` resolves the localization to upstream's strict superset, contains exact upstream `61658f366f985fc7758f0942a14c1754c69aca68`, preserves the fork workflow tree and patched libmpv SHA-256 `B266C9F5ED940B761DA572F1496FCF2152FE624CDFD42354B33BA5C9E3FC8721`, and advances to `0.6.5-nuviodv.1` / `12250`. Fork contracts and the authenticated-release configuration test pass. The affected ratings test passes after replacing its one-second placeholder fixture with a realistic duration; a separate upstream Android download lifecycle test fails on the Windows Robolectric filesystem because `android.util.AtomicFile.finishWrite` leaves the committed payload in `.json.new` while the stale base remains. Required full-debug build gate ticket `289ed9896d1c41d1ab13d5a85508619c` acquired `Local\\Darka.AndroidGradleBuildGate`, enforced two workers/no parallelism/3 GB Gradle and Kotlin heaps, reported no memory failure, verified daemon exit, and produced the correct NuvioDV package/version/label with one ARM64 libmpv. Issue-delta review found no new DV, pink/purple, libplacebo, or long-playback evidence; new `#2395` identifies audio-focus loss as a concrete both-engine pause trigger during calls, while `#2391` reports repeatable rewind on buffer underrun without logs or an engine-specific trace. Neither is implemented automatically. Release run `40528738977` published normal release `v0.6.5-nuviodv.1` from the exact merge source, and no-update sync run `40531081815` completed successfully without another commit or release. The independently downloaded public APK has SHA-256 `779E800C36B5D0A9738C445F1A5802FA0C84D1501B9D1B9E6BD459752ED238FD`, package `com.darkaxt.nuviodv`, version code `12250`, version name `0.6.5-nuviodv.1`, label `NuvioDV`, the expected signer and v2 signature, and exactly one ARM64 libmpv exporting `dovi_parse_rpu` and `dovi_parse_rpu_bin_file`. |
| 2026-10-30 upstream sync, triage, and release | COMPLETE | M1-M9, M13-M14 | Scheduled runs `40861454985` and `41044166931` failed closed on five non-version conflicts after upstream advanced by 64 commits. Merge `fcc83a418f9998d86737a4451a2640de4e979a58` contains exact upstream `10feb659ae3368008a584316bd5e1d2665e3aa6c`, retains upstream's split playback-settings navigation and ratings fixture while preserving the NuvioDV playback-details toggle and RPDB service page, preserves the fork workflow tree and patched libmpv SHA-256 `B266C9F5ED940B761DA572F1496FCF2152FE624CDFD42354B33BA5C9E3FC8721`, and advances to `0.6.5-nuviodv.3` / `12252`. Fork contracts and the focused ratings, settings-navigation, playback-engine, track-preference, and playback-details tests pass. Required full-debug build gate ticket `a3e4457e55d847cbbc38608f00e59d21` acquired `Local\\Darka.AndroidGradleBuildGate`, used two workers, no parallel projects, 3 GB Gradle and Kotlin heaps, and one test fork, reported no memory failure, verified daemon exit, and produced the correct NuvioDV package/version/label with one ARM64 libmpv. Release run `41076862677` published normal release `v0.6.5-nuviodv.3` from the exact merge source, and no-update sync run `41078651659` confirmed that the fork already contains upstream `10feb659ae3368008a584316bd5e1d2665e3aa6c` without another commit or release. The independently downloaded public APK has SHA-256 `A4451315F2F018747BD56FADA449E1ED48353FBEE747953E3CF7930AEC81AFEB`, package `com.darkaxt.nuviodv`, version code `12252`, version name `0.6.5-nuviodv.3`, label `NuvioDV`, the expected signer and v2 signature, and exactly one ARM64 libmpv exporting `dovi_parse_rpu` and `dovi_parse_rpu_bin_file`. Issue-delta review found new high-confidence issue `#2444`: libmpv playback on Android 16 accumulates about 32,470 `anon_inode:sync_file` descriptors before SIGABRT/MediaCodec failure and can block the main thread in `libmpv.so` during `surfaceDestroyed`, matching the long-playback descriptor-exhaustion class represented by baseline `#1730`; no issue-driven change was implemented automatically. Issue `#2391` gained a second-device confirmation but still lacks logs or an engine-specific trace. Blockers and tracked deferrals: none. |
| 2026-11-02 upstream sync, triage, and release | COMPLETE | M1-M9, M13-M14 | Upstream advanced by 63 commits to `46febb568348aa5e135bff0d4d5c914933aff5a0`; scheduled runs `41192757715`, `41301430966`, and `41445770814` failed closed on the Vietnamese resource plus the expected version conflict. Merge `c866c2d895d732f8908fcc611def5ca7defb8187` resolves Vietnamese to upstream's strict superset, contains that exact upstream commit, preserves the fork workflow tree and patched libmpv SHA-256 `B266C9F5ED940B761DA572F1496FCF2152FE624CDFD42354B33BA5C9E3FC8721`, and advances to `0.6.6-nuviodv.1` / `12253`. Verification exposed and repaired two deterministic player defects: post-/mid-credits scenes were incorrectly emitted as skip targets while long feature credits were rejected, and Android subtitle-font metadata rewrites used a rename operation that cannot replace an existing file on Windows; chapter/skip and subtitle-import/appearance tests then passed. Required full-debug build gate ticket `bf38226646b5455aaeba9f64164dae75` acquired `Local\\Darka.AndroidGradleBuildGate`, enforced two workers, no parallel projects, 3 GB Gradle and Kotlin heaps, and one test fork, reported no memory failure, verified daemon exit, and produced the correct package/version/label with one ARM64 libmpv exporting both required libdovi symbols. Release run `41542847542` published normal release `v0.6.6-nuviodv.1` from the exact merge source; the independently downloaded APK has SHA-256 `AB3F5563525147CC0EB4445F6F47BE4B685C3505BFE370F1A5EC319CE0C93C79`, the expected package/version/code/label and signer, a valid v2 signature, and the expected patched ARM64 libmpv. No-update sync run `41543842696` then confirmed the fork already contains upstream `46febb568348aa5e135bff0d4d5c914933aff5a0`. Issue `#2391` gained actionable evidence that enabling ExoPlayer Native Memory makes buffer underruns crash during auto-failover with `ForegroundServiceDidNotStartInTimeException`; this implicates ExoPlayer recovery/service lifecycle rather than a direct DV/libmpv rendering defect, so no issue-driven change was made automatically. Blockers and tracked deferrals: none. |
| Final reconciliation | COMPLETE | M1-M16 | MC1-MC12 remain satisfied; the fork contains upstream `46febb568348aa5e135bff0d4d5c914933aff5a0`, preserves the NuvioDV identity, patched player and workflows, and latest normal release `v0.6.6-nuviodv.1` plus its independently inspected public APK match exact release source `c866c2d895d732f8908fcc611def5ca7defb8187`. All 47 retained releases are normal, the latest-release endpoint resolves to `v0.6.6-nuviodv.1`, follow-up sync run `41543842696` is a verified no-op, and blockers and tracked deferrals equal zero. |

Stage-closure refactoring assessment: no material design debt was introduced; the upstream merge preserves the existing fork boundaries and established release workflow without adding fork-only architecture.

Blockers: none.

Tracked deferrals: none.

## 2026-09-13 sync incident

- Upstream advanced by 33 commits after the preceding scheduled run and released Nuvio `0.4.18`.
- The immediate manual dispatch failed closed before any push or release because upstream's loading-status and pause-overlay settings overlapped NuvioDV's playback-details setting in four repository/storage files; `Version.xcconfig` was the fifth conflict.
- Manual resolution retained all three settings across the shared repository plus Android and iOS storage/sync paths.
- Focused version/workflow contracts and the three affected Android player-settings tests passed; the integrated full debug APK built successfully as `0.4.18-nuviodv.1` with versionCode `12208`.
- The earlier `v0.4.18-nuviodv.1` release and tag, created from commit `b566cf8` under the superseded version algorithm, blocked the canonical upstream-`0.4.18` tag. Its published metadata and APK checksum `DAFABBED4D76FC39FA0D72860BD1D8C7A37C2FA187416943E5A7643CB43A771A` were recorded before deleting that obsolete release/tag; the source commit remains in repository history.
- Release run `34723777285` succeeded against exact source commit `b62001dbce2d8f132dafca3b86680de87bbdf9f5` and published prerelease `v0.4.18-nuviodv.1`.
- Independent download verification confirmed APK SHA-256 `8018D2F9BBB44BE4D527B09897186A86F9E1AB12A16F56F3FCA4F2E5ED839DBF`, package `com.darkaxt.nuviodv`, label `NuvioDV`, versionCode `12208`, versionName `0.4.18-nuviodv.1`, ARM64-only native code, APK Signature Scheme v2, signer certificate SHA-256 `1FB94424753A90F993C678B6FA4322579253BB303F22C335DB395A9E2557D571`, and exported `dovi_parse_rpu` plus `dovi_parse_rpu_bin_file` symbols in `libmpv.so`.

## 2026-09-14 sync

- Upstream advanced by seven commits and released Nuvio `0.4.19`; sync run `34786755533` merged the history without conflicts, passed fork invariants and the integrated Android build/APK verification, then pushed exact release source `4ea112503236b6b7b4d16dfcbac4a6ecd6db667c` as `0.4.19-nuviodv.1` with versionCode `12210`.
- The first release dispatch failed closed because obsolete prerelease/tag `v0.4.19-nuviodv.1`, created from commit `3dfb5cb` while its upstream merge parent was still Nuvio `0.4.17`, occupied the corrected canonical tag. Its APK checksum `58FFEA699A7806D142EA62E663668BEE0AAC3CA365006FD36FF5356FE645CF7D`, publication time, two-download count, and target commit were recorded before deleting that obsolete release/tag; its source commit remains in history.
- Release run `34787071223` succeeded against exact source commit `4ea112503236b6b7b4d16dfcbac4a6ecd6db667c` and published prerelease `v0.4.19-nuviodv.1`.
- Independent download verification confirmed APK SHA-256 `E0D042B78CFDF1C7F7EDC7BFE395EB0B1E8F9E3CED345D0CCE391D0F24D3E4A6`, package `com.darkaxt.nuviodv`, label `NuvioDV`, versionCode `12210`, versionName `0.4.19-nuviodv.1`, ARM64-only native code, APK Signature Scheme v2, signer certificate SHA-256 `1FB94424753A90F993C678B6FA4322579253BB303F22C335DB395A9E2557D571`, and exported `dovi_parse_rpu` plus `dovi_parse_rpu_bin_file` symbols in `libmpv.so`.

## 2026-09-14 public release-lineage repair

- Root cause: automated sync releases were explicitly configured as prereleases, while four earlier builds used invented left-side versions that did not match their actual upstream base. Update clients consequently saw installed `0.4.21-nuviodv.1` as newer than canonical `0.4.19-nuviodv.1` even though the latter had the higher Android versionCode.
- Commit `3f063f84` removed the prerelease workflow input and `--prerelease` publication branch. Daily syncs now dispatch only normal releases, matching the upstream project's delivery model and providing one monotonic public feed.
- The active three-day `maintain-nuviodv-upstream-fork` automation was updated to inspect and dispatch the normal-release workflow and to reject draft/prerelease publication.
- Deleted invalid release/tag `v0.4.17-nuviodv.1`: source `2a0c0aa`, actual upstream base `0.4.16`, APK SHA-256 `FD26E22A2BBE3A27D8B835FAEBD0B83C03B3049D58E256EE954257E9710423A0`, six downloads.
- Deleted invalid release/tag `v0.4.17-nuviodv.2`: source `11a3a98`, actual upstream base `0.4.16`, APK SHA-256 `9FA5031A5ABB56B6D91C9DF0B5FBFDDB8532DE294683D94BA9CD35FC5CCF4715`, four downloads.
- Deleted invalid release/tag `v0.4.20-nuviodv.1`: tag source `ddecc19`, actual upstream base `0.4.17`, APK SHA-256 `F30C8D29E4EC2AF851B354BFE633F05374A47D25FB0D32ED96CEC3EA798810EF`, one download.
- Deleted invalid release/tag `v0.4.21-nuviodv.1`: source `7066fe3`, actual upstream base `0.4.17`, APK SHA-256 `7DC5E4D584CDC45DD18603084ECAB7F043BB7AD09321C2C7009903EBE409240D`, four downloads.
- Retained `v0.4.17-nuviodv.5`, `v0.4.18-nuviodv.1`, `v0.4.18-nuviodv.2`, and `v0.4.19-nuviodv.1` because each tag resolves to a source whose upstream base matches its left-side version. All four are normal releases; `v0.4.19-nuviodv.1` is explicitly Latest. No retained artifact required rebuilding.

## 2026-09-18 updater and synchronization repair

- The in-app updater still queried `NuvioMedia/NuvioMobile`; upstream then refactored updater selection into `AppUpdaterRepository`, so merge `79a32bc0` retained the new stable/beta behavior and changed its GitHub endpoint to `Darkaxt/NuvioMobile`.
- Scheduled sync runs `35205432333` and `35327470164` had integrated and built upstream successfully but failed at push because upstream modified stock workflow files and GitHub's default Actions token cannot update workflow files. Commit `79a4adf1` makes future merges restore the fork's pre-merge `.github/workflows` tree before push.
- The first repaired live dispatch, run `35335125861`, failed closed on the updater refactor conflict plus the expected version conflict. Manual merge `79a32bc0` preserved upstream application changes, the fork updater endpoint, and fork workflows; the full debug APK then built successfully as `0.4.23-nuviodv.1` with versionCode `12214`.
- Release run `35336010585` published normal release `v0.4.23-nuviodv.1` from exact source `79a32bc00a26b19880681f3c325964689398b407`. The GitHub latest endpoint returns that release and reports `draft=false` and `prerelease=false`.
- Independent download verification confirmed APK SHA-256 `EA12B7ECF9BA09D89FCFAB4588E8CDA107DA4EBBBDC54878DF12AC1DB56DE83E`, package `com.darkaxt.nuviodv`, label `NuvioDV`, versionCode `12214`, versionName `0.4.23-nuviodv.1`, ARM64-only native code, APK Signature Scheme v2, signer certificate SHA-256 `1FB94424753A90F993C678B6FA4322579253BB303F22C335DB395A9E2557D571`, and exported `dovi_parse_rpu` plus `dovi_parse_rpu_bin_file` symbols in `libmpv.so`.
- The fork is zero commits behind upstream after the merge; no-update sync run `35336838855` passed without creating another commit or release.
- Issue delta triage identified upstream #1972 (optional smoother ASS motion for Android libmpv) and #1956 (MPV autoplay skips and marks the next episode watched) as playback-relevant candidates. They remain unimplemented because maintenance monitoring does not broaden fork scope automatically.

## 2026-09-18 upstream 0.4.24 sync

- Upstream advanced by eleven commits and released Nuvio `0.4.24`. Sync run `35396917932` failed closed before push because the upstream player-controls split overlapped NuvioDV's playback-details header and because both sides changed the shared strings and version files.
- Manual merge `220b744d62382cca4cc0f3eb63b4aa41d744fdbd` preserved upstream's legacy/new player-layout architecture while keeping the optional live stream details in both layouts. Focused player tests, sync/version contracts, release-configuration checks, and the integrated full debug APK build passed as `0.4.24-nuviodv.1` with versionCode `12221`.
- Release run `35397925136` published normal release `v0.4.24-nuviodv.1` from that exact merge commit. The GitHub latest endpoint returns it with `draft=false` and `prerelease=false`.
- Independent download verification confirmed APK SHA-256 `AB5F5A2EBB4FD57A27437F75E292D0BC93DEC3C5D1A9BDACD9BB3BA3869EA0AF`, package `com.darkaxt.nuviodv`, label `NuvioDV`, versionCode `12221`, versionName `0.4.24-nuviodv.1`, ARM64-only native code, APK Signature Scheme v2, the existing release signer certificate SHA-256 `1FB94424753A90F993C678B6FA4322579253BB303F22C335DB395A9E2557D571`, and exported `dovi_parse_rpu` plus `dovi_parse_rpu_bin_file` symbols in `libmpv.so`.
- The fork is zero commits behind upstream after the merge, and the fork's workflow tree was unchanged by this synchronization.
