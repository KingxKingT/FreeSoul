# Large hostname import summary — 2026-11-20

The user supplied an 18 MB text file containing 953,393 hostname lines. It was analyzed locally and retained as a review-only export; it has **not** been copied into the active blocklist or committed as a bulk dataset.

- 953,197 syntactically valid unique hostnames retained.
- 196 malformed DNS hostname rows rejected (mainly underscores, which are not valid in ordinary DNS host labels).
- 14 additional hostnames were retained but flagged for public-suffix-boundary review.
- No exact duplicate hostname rows were found.
- 267,086 entries matched known shared-hosting patterns and must stay at tenant-hostname scope.
- 220,050 entries sit below the Public Suffix List's computed registrable-domain boundary. This is a syntactic grouping metric, not a count of verified adult sites.

The export is intentionally conservative: keep each input hostname. Do not collapse user-hosted subdomains to provider roots. Runtime matching must use exact match or a dot-boundary subdomain match.

Local review outputs are generated from the user-provided file and should be treated as unverified candidates until the promotion checklist is completed.
