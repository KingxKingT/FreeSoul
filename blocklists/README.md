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


## Candidate discovery source: ThePornDude

- Directory: https://theporndude.com/
- The homepage describes itself as a directory/review site for adult websites and advertises a searchable directory of 11,938+ sites. It visibly organizes entries into categories such as free tube sites and tags including AI sites and AI sex chat.
- Use this only as a **discovery seed**, not as a trusted blocklist or a guarantee of complete coverage.
- The directory is curated content and may contain redirects, affiliate links, stale entries, mirrors, duplicate brands, or domains that have changed ownership or purpose.
- Do not blindly block every linked hostname. Extract candidate registrable domains, resolve redirect destinations only where permitted, deduplicate, and review each candidate before publication.
- Do not scrape at high volume or bypass access controls. Check the site's terms, robots guidance, and permission before automated collection or redistribution. Prefer a permitted export/API or a manually supplied domain list.
- Keep category provenance (e.g. adult video, adult image, adult chat, AI adult service) and review date. A directory category does not prove every linked domain or subdomain is exclusively adult.
- Do not treat AI-related directory tags as a complete inventory of AI sexual-content services.

