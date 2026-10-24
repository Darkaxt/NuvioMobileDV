import unittest
from pathlib import Path


REPOSITORY_ROOT = Path(__file__).resolve().parents[2]
WORKFLOW = REPOSITORY_ROOT / ".github" / "workflows" / "nuviodv-upstream-sync.yml"
RELEASE_WORKFLOW = REPOSITORY_ROOT / ".github" / "workflows" / "nuviodv-android-release.yml"
APP_UPDATER_REPOSITORY = (
    REPOSITORY_ROOT
    / "composeApp"
    / "src"
    / "commonMain"
    / "kotlin"
    / "com"
    / "nuvio"
    / "app"
    / "features"
    / "updater"
    / "AppUpdaterRepository.kt"
)
PLAYER_CONTROLS = (
    REPOSITORY_ROOT
    / "composeApp"
    / "src"
    / "commonMain"
    / "kotlin"
    / "com"
    / "nuvio"
    / "app"
    / "features"
    / "player"
    / "PlayerControls.kt"
)
PLAYER_TIMELINE = PLAYER_CONTROLS.with_name("PlayerTimeline.kt")


class NuvioDvUpstreamSyncWorkflowTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        cls.workflow = WORKFLOW.read_text(encoding="utf-8")
        cls.release_workflow = RELEASE_WORKFLOW.read_text(encoding="utf-8")
        cls.app_updater_repository = APP_UPDATER_REPOSITORY.read_text(encoding="utf-8")
        cls.player_controls = PLAYER_CONTROLS.read_text(encoding="utf-8")
        cls.player_timeline = PLAYER_TIMELINE.read_text(encoding="utf-8")

    def test_runs_daily_and_can_be_dispatched_manually(self) -> None:
        self.assertIn("schedule:", self.workflow)
        self.assertIn("cron: '17 4 * * *'", self.workflow)
        self.assertIn("workflow_dispatch:", self.workflow)

    def test_has_permissions_to_push_and_dispatch_release(self) -> None:
        self.assertIn("contents: write", self.workflow)
        self.assertIn("actions: write", self.workflow)
        self.assertIn("gh workflow run nuviodv-android-release.yml", self.workflow)

    def test_syncs_only_the_authoritative_upstream_branch(self) -> None:
        self.assertIn("https://github.com/NuvioMedia/NuvioMobile.git", self.workflow)
        self.assertIn("git fetch --no-tags upstream cmp-rewrite", self.workflow)
        self.assertIn("git merge --no-commit --no-ff upstream/cmp-rewrite", self.workflow)

    def test_conflict_exception_is_limited_to_the_version_file(self) -> None:
        self.assertIn("allowed_conflict='iosApp/Configuration/Version.xcconfig'", self.workflow)
        self.assertIn('[[ "${conflicts[0]}" == "${allowed_conflict}" ]]', self.workflow)
        self.assertIn("git merge --abort", self.workflow)

    def test_preserves_fork_workflows_during_upstream_merges(self) -> None:
        self.assertIn('pre_merge_head="$(git rev-parse HEAD)"', self.workflow)
        self.assertIn('git diff --name-only "${pre_merge_head}" -- .github/workflows', self.workflow)
        self.assertIn('git checkout "${pre_merge_head}" -- "${workflow_path}"', self.workflow)
        self.assertIn('git rm -- "${workflow_path}"', self.workflow)

    def test_verifies_the_fork_before_push(self) -> None:
        self.assertIn("test_advance_nuviodv_version.py", self.workflow)
        self.assertIn("test-configure-nuviodv-release.sh", self.workflow)
        self.assertIn(": > local.properties", self.workflow)
        self.assertIn(":androidApp:assembleFullDebug", self.workflow)
        self.assertIn("dovi_parse_rpu", self.workflow)
        self.assertIn("id: sync_commit", self.workflow)
        self.assertIn('echo "sha=$(git rev-parse HEAD)" >> "${GITHUB_OUTPUT}"', self.workflow)
        self.assertIn("git push origin HEAD:cmp-rewrite", self.workflow)
        self.assertIn('-f source_sha="${SOURCE_SHA}"', self.workflow)

    def test_release_checks_out_the_exact_synced_commit(self) -> None:
        self.assertIn("source_sha:", self.release_workflow)
        self.assertIn("ref: ${{ inputs.source_sha || github.sha }}", self.release_workflow)
        self.assertIn("REQUESTED_SOURCE_SHA: ${{ inputs.source_sha }}", self.release_workflow)
        self.assertIn('[[ "${source_sha}" == "${REQUESTED_SOURCE_SHA}" ]]', self.release_workflow)
        self.assertIn("SOURCE_SHA: ${{ steps.release.outputs.source_sha }}", self.release_workflow)
        self.assertIn('--target "${SOURCE_SHA}"', self.release_workflow)

    def test_release_requires_and_verifies_mdblist_client_identifier(self) -> None:
        self.assertIn(
            "MDBLIST_CLIENT_ID: ${{ secrets.NUVIODV_MDBLIST_CLIENT_ID }}",
            self.release_workflow,
        )
        self.assertIn(
            "RUNTIME_PROPERTIES_BASE64 MDBLIST_CLIENT_ID KEYSTORE_BASE64",
            self.release_workflow,
        )
        self.assertIn(
            "composeApp/build/generated/runtime-config/kotlin/com/nuvio/app/features/mdblist/MdbListConfig.kt",
            self.release_workflow,
        )
        self.assertIn(
            "grep -Eq 'const val CLIENT_ID = \"[^\"]+\"' \"${mdblist_config}\"",
            self.release_workflow,
        )

    def test_all_published_builds_are_normal_releases(self) -> None:
        self.assertIn("Dispatch the authenticated release", self.workflow)
        self.assertNotIn("authenticated prerelease", self.workflow)
        self.assertNotIn("prerelease:", self.release_workflow)
        self.assertNotIn("--prerelease", self.release_workflow)

    def test_in_app_updater_uses_the_fork_release_feed(self) -> None:
        self.assertIn(
            'url = "https://api.github.com/repos/Darkaxt/NuvioMobileDV/${releasePath(channel)}"',
            self.app_updater_repository,
        )
        self.assertNotIn(
            "https://api.github.com/repos/NuvioMedia/NuvioMobile/",
            self.app_updater_repository,
        )

    def test_playback_details_survive_both_player_control_layouts(self) -> None:
        technical_details_binding = (
            "technicalDetails = if (showTechnicalDetails) "
            "playbackSnapshot.technicalDetailsLine() else null"
        )
        self.assertIn("showTechnicalDetails: Boolean", self.player_controls)
        self.assertEqual(2, self.player_controls.count(technical_details_binding))
        self.assertIn("technicalDetails: String?", self.player_timeline)
        self.assertIn("if (!technicalDetails.isNullOrBlank())", self.player_timeline)
        self.assertIn("text = technicalDetails", self.player_timeline)


if __name__ == "__main__":
    unittest.main()
