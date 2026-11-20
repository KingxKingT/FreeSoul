# Review-only candidate imports

Files in this directory are **not active blocking rules**. They are source material waiting for verification.

## Current user-provided batch

- `porn_dude_seed_2026-11-20.txt`: 87 hostnames supplied by the user from a directory list.
- `verified_adult_candidates-2026-11-20.csv`: seven candidates with clear first-party evidence of adult-oriented purpose (two adult content sites and five adult AI services). They are **reviewed candidates, not active rules**; test DNS, ownership/current hostname, false positives and app regression before activation.
- `chatzy.com` presents itself as a general private chat service; `viby.chat` describes anonymous stranger chat with photo sharing. Do not block these general services based only on directory placement.
- `telescope.me` shows live creator streams, but this review did not establish that the whole hostname is exclusively adult. Keep it in manual review.
- Seven other names in the pasted batch remain purpose-unconfirmed manual-review items; 70 more entries remain unverified. See the downloadable review CSV for per-domain notes.
- Names, tags, or directory listing alone are not proof of current content or ownership.

## Large uploaded hostname list

The uploaded `porn_domains.txt` was analyzed locally but is not copied into the repository:
- 953,393 input lines
- 953,197 syntactically valid, unique hostnames retained in the clean candidate export
- 196 malformed DNS hostname rows rejected
- 14 entries retained but flagged for public-suffix-boundary review
- 267,086 hostnames matched known shared-hosting patterns, so they require tenant-level handling
- No exact duplicate hostnames were found

A large number of entries are tenants on platforms such as Tumblr and Blogspot. Never replace those hostnames with the platform root. Doing so would block unrelated users and legitimate content.

## Promotion checklist

Before any candidate moves to an active list:
1. Verify its current purpose and current hostname/redirect.
2. Confirm that the domain is primarily adult-oriented, not simply a general platform with some user-generated material.
3. Identify shared hosting, public suffixes, CDNs, and other false-positive risks.
4. Record source, collection date, evidence, and license/redistribution permission.
5. Add regression tests for both intended blocks and legitimate services that must remain reachable.
