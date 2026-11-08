import tempfile
import unittest
from pathlib import Path

from advance_nuviodv_version import advance_version_file, compute_next_version


class AdvanceNuvioDvVersionTest(unittest.TestCase):
    def test_advances_fork_counter_when_upstream_base_is_unchanged(self) -> None:
        self.assertEqual(
            compute_next_version(
                current_name="0.4.17-nuviodv.4",
                current_code=12203,
                upstream_name="0.4.17",
                upstream_code=122,
            ),
            ("0.4.17-nuviodv.5", 12204),
        )

    def test_adopts_newer_upstream_base_and_resets_fork_counter(self) -> None:
        self.assertEqual(
            compute_next_version(
                current_name="0.4.17-nuviodv.5",
                current_code=12203,
                upstream_name="0.5.0",
                upstream_code=13000,
            ),
            ("0.5.0-nuviodv.1", 13001),
        )

    def test_rejects_upstream_base_older_than_fork_base(self) -> None:
        with self.assertRaisesRegex(ValueError, "older"):
            compute_next_version(
                current_name="0.4.18-nuviodv.1",
                current_code=12203,
                upstream_name="0.4.17",
                upstream_code=122,
            )

    def test_rejects_a_non_nuviodv_current_version(self) -> None:
        with self.assertRaisesRegex(ValueError, "NuvioDV"):
            compute_next_version(
                current_name="0.4.18",
                current_code=12203,
                upstream_name="0.4.19",
                upstream_code=123,
            )

    def test_updates_only_version_values_in_xcconfig(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            fork_config = root / "fork.xcconfig"
            upstream_config = root / "upstream.xcconfig"
            fork_config.write_text(
                "CURRENT_PROJECT_VERSION=12203\nMARKETING_VERSION=0.4.17-nuviodv.4\n",
                encoding="utf-8",
            )
            upstream_config.write_text(
                "CURRENT_PROJECT_VERSION=122\nMARKETING_VERSION=0.4.17\n",
                encoding="utf-8",
            )

            result = advance_version_file(fork_config, upstream_config)

            self.assertEqual(result, ("0.4.17-nuviodv.5", 12204))
            self.assertEqual(
                fork_config.read_text(encoding="utf-8"),
                "CURRENT_PROJECT_VERSION=12204\nMARKETING_VERSION=0.4.17-nuviodv.5\n",
            )
            self.assertNotIn(b"\r", fork_config.read_bytes())


if __name__ == "__main__":
    unittest.main()
