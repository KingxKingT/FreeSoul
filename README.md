# FreeSoul

FreeSoul is an Android-first adult website protection project. The first milestone is reliable domain-level blocking; social-media image/video moderation is explicitly out of scope until website protection is stable.

## Current scope (MVP)

- Android app, initially targeting Android 10+.
- DNS-based filtering of known adult domains across apps and browsers while protection is active.
- Curated, updateable domain lists with source attribution and license checks.
- Allowlist and false-positive reporting workflow.
- Local protection status and aggregate blocked-request counters.
- No collection or sale of browsing history.

## Important limitations

DNS filtering blocks domains, not individual pages, images, videos, or chat messages. It cannot reliably identify explicit content hosted on a general-purpose platform or distinguish adult content from legitimate content on the same domain. Some apps use encrypted DNS, hard-coded resolvers, or shared hosting and may bypass or resist DNS filtering. FreeSoul must communicate these limits honestly.

## Technical direction

Evaluate an Apache-2.0-compatible Android codebase such as [RethinkDNS](https://github.com/celzero/rethink-app) as a possible foundation or reference. Do not assume it is a drop-in library: inspect its modules, integration boundaries, current license notices, and build process before deciding whether to fork or reuse components.

Use Android's supported `VpnService` approach for local DNS filtering if the chosen architecture requires it. No root access, HTTPS interception, certificate installation, or accessibility-based screen scraping in the MVP.

## Repository map

- `docs/PRODUCT_SCOPE.md` — MVP requirements and non-goals.
- `docs/TECHNICAL_PLAN.md` — proposed architecture and validation plan.
- `blocklists/README.md` — list sources, format, update and licensing rules.
- `app/` — Android application (to be added after the foundation is selected).

## Development status

Planning and repository setup. No APK has been built or tested yet.

## Contributing domain lists

Send candidate domains as plain text or CSV. Every entry will be normalized, deduplicated, checked for malformed values and reviewed for false positives before inclusion. Do not submit URLs containing personal information or private browsing records.

## License

The license for FreeSoul's original code and documentation has not yet been selected. Third-party code and blocklists retain their own licenses and notices; do not assume that every upstream list can be redistributed commercially.
