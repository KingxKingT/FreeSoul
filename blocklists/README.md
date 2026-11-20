# Blocklist data

This directory is reserved for FreeSoul-maintained, reviewed domain rules. Do not commit large third-party lists until their exact license and redistribution terms have been checked.

## Suggested format

One normalized hostname per line. Comments begin with `#`.

Example (documentation-only placeholders):
```text
# category: adult-sites
# source: user-submitted / reviewed
example.invalid
subdomain.example.invalid
```

Do not include full URLs, paths, query strings, personal data, or browsing histories.

## Categories
- `adult-sites`: domains whose primary purpose is explicit adult content.
- `adult-chat`: adult-oriented chat services.
- `adult-ai`: services primarily intended to generate explicit sexual content.
- `mixed-content`: do not block automatically; require careful review because legitimate content may share the domain.

## Review process
1. Normalize and deduplicate.
2. Validate domain syntax and public-suffix boundaries.
3. Identify mixed-use services and likely false positives.
4. Record provenance and license.
5. Run regression checks.
6. Publish a versioned list with a checksum.

User-submitted candidates are untrusted input and must be reviewed before publication.
