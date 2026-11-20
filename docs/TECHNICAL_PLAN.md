# Technical plan

## Architecture proposal

### Android client
- Kotlin and Android Studio.
- Android `VpnService` to route DNS traffic through a local filtering component, subject to Android platform rules and explicit user consent.
- A small DNS proxy/filter that evaluates queried hostnames against local rule sets.
- Encrypted list downloads over HTTPS; verify integrity and publish updates atomically.
- Room or a similarly small local store for settings and aggregate counters only.

### Rule evaluation
1. Normalize the queried hostname (lowercase, IDNA-safe, remove trailing dot).
2. Check user allowlist first.
3. Check curated block rules, including explicit subdomain semantics.
4. Return a deliberate blocked response for matches; forward other queries to the configured upstream resolver.
5. Do not log raw query history. Keep only aggregate counters by time period if needed.

### Blocklist pipeline
- Keep upstream sources separate from FreeSoul's own reviewed additions.
- Record source URL, license, retrieval date, version/hash, and transformation steps.
- Deduplicate normalized domains.
- Reject URLs, IP addresses, malformed entries, and public suffixes unless explicitly justified.
- Run false-positive checks against a small legitimate-domain regression set.
- Publish versioned snapshots; retain last-known-good snapshot on update failure.

## Candidate upstreams to evaluate
- Rethink app: https://github.com/celzero/rethink-app (Apache-2.0 project; inspect current repository license and module boundaries before reuse).
- HaGeZi DNS blocklists: https://github.com/hagezi/dns-blocklists (check the exact list license and redistribution terms before bundling).
- StevenBlack hosts: https://github.com/StevenBlack/hosts (check current license and list composition before bundling).

Do not assume the licenses of the app and its data sources are interchangeable. Maintain all required notices and consult legal advice before commercial redistribution.

## Android constraints
- Android permits only one active `VpnService` per user/profile in the normal case; coexistence with another VPN is a known limitation.
- Some apps use their own encrypted DNS, hard-coded IPs, or certificate-pinned connections and may bypass DNS-based controls.
- DNS filtering cannot block a specific URL path or inspect page contents.
- Avoid accessibility services and screen capture in the website-only MVP.

## Test plan
- Unit tests for hostname normalization, wildcard/subdomain matching, allowlist precedence, list parsing, and update rollback.
- Integration tests on Wi-Fi and mobile data.
- Regression tests for legitimate domains and common app connectivity.
- Battery, reconnect, airplane-mode, captive-portal, and VPN lifecycle tests.
- Manual tests with at least two Android versions and multiple browsers.

## Status
This is a proposal, not an implemented or tested architecture. Select a reusable engine only after inspecting its current source, license, API surface, and build instructions.
