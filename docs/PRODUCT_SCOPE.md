# Product scope

## Goal
Help users reduce access to known adult websites on their Android devices through transparent, privacy-preserving DNS filtering.

## MVP requirements
1. Start/stop protection with explicit Android VPN consent.
2. Filter DNS queries against a curated adult-domain list.
3. Support Wi-Fi and mobile data while the local VPN is active.
4. Keep the latest downloaded rules available offline.
5. Allow user-managed exceptions (allowlist) and custom blocked domains.
6. Show protection state, last list update, and aggregate blocked-query count.
7. Explain why a domain was blocked and provide a false-positive report path.
8. Provide clear disclosures about VPN use, data handling, and filtering limitations.

## Non-goals for MVP
- Detecting explicit images or videos on social media.
- Reading private messages or inspecting page contents.
- HTTPS interception, installing user certificates, or decrypting traffic.
- Guaranteeing that every adult page or new domain is blocked.
- Collecting browsing histories or transmitting visited domains to an account server.
- Tamper-proof protection against a device owner with technical control.

## Acceptance tests
- Known test domains resolve to the configured blocked response while protection is active.
- A control set of legitimate sites continues to resolve.
- Protection stops cleanly and releases the VPN interface.
- Rules remain available after a restart and when offline.
- Updating rules is atomic: a failed download never replaces the last good list.
- Allowlist exceptions work and are visible to the user.
- DNS queries and full browsing history are not uploaded to FreeSoul servers.

## User safety and privacy
Use local processing by default. Any optional diagnostics must be opt-in, minimized, and must not include a user's visited-domain history. Do not make coercive or deceptive claims about protection being impossible to disable.
