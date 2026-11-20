# Review-only candidate imports

Files in this directory are **not active blocking rules**. They are source material waiting for verification.

## Current user-provided batch

- `porn_dude_seed_2026-11-20.txt`: 87 hostnames supplied by the user from a directory list.
- `pasted_domains_review.csv` is kept as a local analysis artifact, not committed here. It separates confirmed purpose from uncertain/general services.
- Current page checks support adult-purpose classifications for `cake.ski`, `sweetdream.ai`, and `playbun.com`. These remain candidates pending DNS checks, false-positive tests, and inclusion approval.
- `chatzy.com` presents itself as a general private chat-room service. `viby.chat` presents itself as random anonymous stranger chat with photo sharing. Neither should be blanket-blocked based only on that evidence.
- Other uncertain entries remain manual-review candidates. Names, tags, or a directory listing are not proof of current content or ownership.

## Large uploaded hostname list

The uploaded `porn_domains.txt` was analyzed locally but is not copied into the repository:
- 953,393 input lines
- 953,197 syntactically valid, unique hostnames retained in the clean candidate export
- 196 malformed DNS hostname rows rejected
- 14 entries retained but flagged for public-suffix-boundary review
- 267,086 hostnames matched known shared-hosting patterns, so they require tenant-level handling

The input has no exact duplicate hostnames. A large number of entries are tenants on platforms such as Tumblr and Blogspot. Never replace those hostnames with the platform root. Doing so would block unrelated users and legitimate content.

## Promotion checklist

Before any candidate moves to an active list:
1. Verify its current purpose and current hostname/redirect.
2. Confirm that the domain is primarily adult-oriented, not simply a general platform with some user-generated material.
3. Identify shared hosting, public suffixes, CDNs, and other false-positive risks.
4. Record source, collection date, evidence, and license/redistribution permission.
5. Add regression tests for both intended blocks and legitimate services that must remain reachable.
