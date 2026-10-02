#!/usr/bin/env python3
# SPDX-FileCopyrightText: Copyright The OrbitOS Authors
# SPDX-License-Identifier: Apache-2.0
"""
Licence check for upstream patches (ground rule 4).

Before merging anything from upstream, run:
    python orbitos/scripts/upstream_license_check.py upstream/lts-4.3
or for one commit or tag:
    python orbitos/scripts/upstream_license_check.py <commit-or-tag>

Every commit that the merge would bring in (HEAD..REF) must pass:
  1. it is reachable from the upstream lts-4.3 branch or a v4.3.x tag (never 4.4+, lts-4.4 or master);
  2. the top-level LICENSE at that commit is the Apache License 2.0;
  3. every file it adds or changes either has no SPDX licence identifier or says Apache-2.0;
  4. its changes do not add the text "Business Source License" or "BUSL".
Exit code 0 = all commits pass. Exit code 1 = refuse the merge and read the report.

Also usable on the current tree (no merge): --tree checks LICENSE and all SPDX identifiers.
"""
import argparse
import re
import subprocess
import sys

ALLOWED_TAG = re.compile(r"^v4\.3(\.\d+)*$")
ALLOWED_BRANCH = "refs/remotes/upstream/lts-4.3"
SPDX_ID = re.compile(r"SPDX-License-Identifier:\s*([A-Za-z0-9.+-]+)")
BUSL = re.compile(r"Business Source License|\bBUSL\b|\bBSL-1\.1\b|\bBUSL-1\.1\b")
APACHE_FIRST_LINES = ("Apache License", "Version 2.0, January 2004")


def git(*args, check=True):
    result = subprocess.run(["git", *args], capture_output=True, text=True, encoding="utf-8", errors="replace")
    if check and result.returncode != 0:
        raise RuntimeError("git %s failed: %s" % (" ".join(args), result.stderr.strip()))
    return result.stdout


def license_is_apache(commit):
    try:
        text = git("show", "%s:LICENSE" % commit)
    except RuntimeError:
        return False, "no LICENSE file"
    head = "\n".join(text.splitlines()[:5])
    if BUSL.search(text[:4000]):
        return False, "LICENSE is the Business Source License"
    if all(marker in head for marker in APACHE_FIRST_LINES):
        return True, ""
    return False, "LICENSE does not start with the Apache License 2.0 text"


def reachable_from_allowed(commit):
    if subprocess.run(["git", "merge-base", "--is-ancestor", commit, ALLOWED_BRANCH],
                      capture_output=True).returncode == 0:
        return True
    for tag in git("tag", "--contains", commit).split():
        if ALLOWED_TAG.match(tag):
            return True
    return False


def check_commit(commit):
    problems = []
    if not reachable_from_allowed(commit):
        problems.append("not on upstream lts-4.3 or a v4.3.x tag")
    ok, reason = license_is_apache(commit)
    if not ok:
        problems.append(reason)
    # What this commit brings to the branch: its diff against its first parent.
    parent = "%s^1" % commit
    changed = git("diff", "--name-only", "--diff-filter=AMR", parent, commit).split("\n")
    for path in sorted({p for p in changed if p}):
        try:
            content = git("show", "%s:%s" % (commit, path))
        except RuntimeError:
            continue
        for match in SPDX_ID.finditer(content[:3000]):
            if match.group(1) != "Apache-2.0":
                problems.append("%s: SPDX-License-Identifier %s" % (path, match.group(1)))
    added = [line for line in git("diff", parent, commit).splitlines()
             if line.startswith("+") and not line.startswith("+++")]
    if any(BUSL.search(line) for line in added):
        problems.append("adds Business Source License text")
    return problems


def check_tree():
    problems = []
    ok, reason = license_is_apache("HEAD")
    if not ok:
        problems.append("HEAD: " + reason)
    for line in git("grep", "-n", "-I", "SPDX-License-Identifier:", "HEAD", "--", ".", check=False).splitlines():
        match = SPDX_ID.search(line)
        if match and match.group(1) != "Apache-2.0":
            problems.append(line[:200])
    for line in git("grep", "-n", "-I", "-E", "Business Source License|BUSL-1\\.1", "HEAD", "--", ".",
                    ":!orbitos/scripts/upstream_license_check.py", ":!UPSTREAM_PATCHES.md", ":!docs/**",
                    ":!orbitos/docs/**", check=False).splitlines():
        problems.append("BUSL text: " + line[:200])
    return problems


def main():
    parser = argparse.ArgumentParser(description="Refuse upstream patches that are not Apache-2.0.")
    parser.add_argument("ref", nargs="?", help="upstream ref to merge, e.g. upstream/lts-4.3")
    parser.add_argument("--tree", action="store_true", help="check the current tree instead of a merge")
    args = parser.parse_args()

    if args.tree:
        problems = check_tree()
        for problem in problems:
            print("FAIL " + problem)
        print("Tree check: %s" % ("FAILED" if problems else "passed"))
        sys.exit(1 if problems else 0)

    if not args.ref:
        parser.error("give an upstream ref, or --tree")
    # First-parent history: each merge is judged by everything it brings in at once.
    commits = git("rev-list", "--reverse", "--first-parent", "HEAD..%s" % args.ref).split()
    if not commits:
        print("Nothing to merge: HEAD already contains %s." % args.ref)
        return
    failed = 0
    for commit in commits:
        problems = check_commit(commit)
        subject = git("log", "-1", "--format=%h %s", commit).strip()
        if problems:
            failed += 1
            print("FAIL %s" % subject)
            for problem in problems:
                print("     - %s" % problem)
        else:
            print("ok   %s" % subject)
    print("\n%d commit(s) checked, %d refused." % (len(commits), failed))
    if failed:
        print("Do NOT merge. Record the refusal in UPSTREAM_PATCHES.md and ask for legal review if unsure.")
        sys.exit(1)
    print("All commits are Apache-2.0. You may merge, then log the merge in UPSTREAM_PATCHES.md.")


if __name__ == "__main__":
    main()
