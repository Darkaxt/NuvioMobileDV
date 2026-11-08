#!/usr/bin/env python3
import argparse
import re
from pathlib import Path


NUVIODV_VERSION = re.compile(
    r"^(?P<major>0|[1-9]\d*)\.(?P<minor>0|[1-9]\d*)\.(?P<patch>0|[1-9]\d*)-nuviodv\.(?P<fork>0|[1-9]\d*)$"
)
UPSTREAM_VERSION = re.compile(
    r"^(?P<major>0|[1-9]\d*)\.(?P<minor>0|[1-9]\d*)\.(?P<patch>0|[1-9]\d*)$"
)


def parse_core(match: re.Match[str]) -> tuple[int, int, int]:
    return tuple(int(match.group(name)) for name in ("major", "minor", "patch"))


def compute_next_version(
    *,
    current_name: str,
    current_code: int,
    upstream_name: str,
    upstream_code: int,
) -> tuple[str, int]:
    current_match = NUVIODV_VERSION.fullmatch(current_name.strip())
    if current_match is None:
        raise ValueError(f"Current version is not a NuvioDV version: {current_name}")
    upstream_match = UPSTREAM_VERSION.fullmatch(upstream_name.strip())
    if upstream_match is None:
        raise ValueError(f"Upstream version is not a plain semantic version: {upstream_name}")

    current_core = parse_core(current_match)
    upstream_core = parse_core(upstream_match)
    current_fork = int(current_match.group("fork"))
    if upstream_core > current_core:
        next_core = upstream_core
        next_fork = 1
    elif upstream_core == current_core:
        next_core = upstream_core
        next_fork = current_fork + 1
    else:
        raise ValueError(
            f"Upstream version {upstream_name} is older than the fork base "
            f"{'.'.join(str(component) for component in current_core)}"
        )

    next_name = ".".join(str(component) for component in next_core) + f"-nuviodv.{next_fork}"
    next_code = max(current_code, upstream_code) + 1
    return next_name, next_code


def read_xcconfig(path: Path) -> tuple[str, int]:
    values: dict[str, str] = {}
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip()

    name = values.get("MARKETING_VERSION")
    code_text = values.get("CURRENT_PROJECT_VERSION")
    if name is None or code_text is None:
        raise ValueError(f"Version values are missing from {path}")
    try:
        code = int(code_text)
    except ValueError as error:
        raise ValueError(f"CURRENT_PROJECT_VERSION is invalid in {path}: {code_text}") from error
    return name, code


def advance_version_file(fork_config: Path, upstream_config: Path) -> tuple[str, int]:
    current_name, current_code = read_xcconfig(fork_config)
    upstream_name, upstream_code = read_xcconfig(upstream_config)
    next_name, next_code = compute_next_version(
        current_name=current_name,
        current_code=current_code,
        upstream_name=upstream_name,
        upstream_code=upstream_code,
    )

    content = fork_config.read_text(encoding="utf-8")
    content, code_replacements = re.subn(
        r"(?m)^CURRENT_PROJECT_VERSION=.*$",
        f"CURRENT_PROJECT_VERSION={next_code}",
        content,
    )
    content, name_replacements = re.subn(
        r"(?m)^MARKETING_VERSION=.*$",
        f"MARKETING_VERSION={next_name}",
        content,
    )
    if code_replacements != 1 or name_replacements != 1:
        raise ValueError(f"Expected exactly one version value of each kind in {fork_config}")
    fork_config.write_text(content, encoding="utf-8", newline="\n")
    return next_name, next_code


def main() -> None:
    parser = argparse.ArgumentParser(description="Advance the fork-visible NuvioDV release version.")
    parser.add_argument("--fork-config", type=Path, required=True)
    parser.add_argument("--upstream-config", type=Path, required=True)
    parser.add_argument("--github-output", type=Path)
    args = parser.parse_args()

    version_name, version_code = advance_version_file(args.fork_config, args.upstream_config)
    if args.github_output is not None:
        with args.github_output.open("a", encoding="utf-8", newline="\n") as output:
            output.write(f"version_name={version_name}\n")
            output.write(f"version_code={version_code}\n")
    print(f"Advanced NuvioDV to {version_name} ({version_code}).")


if __name__ == "__main__":
    main()
