# FreeSoul domain import tool

Converts a permitted export or user-supplied TXT/CSV file into a review queue. It does not crawl ThePornDude or any other website.

## Run

```bash
python tools/import_domains.py candidates.txt -o domain-review.csv
python -m unittest discover -s tests -v
```

Input may contain one URL or hostname per line, or a CSV with a `domain` or `url` column. The importer normalizes hostnames, rejects malformed entries, removes exact duplicates, and tags known shared-hosting providers for manual review. It never collapses a tenant hostname to the platform root.

Runtime matching is exact-host or dot-boundary subdomain matching; see `docs/BLOCKING_RULES.md`. A rule for `example.com` therefore matches `asian.example.com`, but not `notexample.com`.

The output is only a review queue. Nothing becomes an active block rule automatically. Before collecting or redistributing directory data, check the source site's terms and obtain any required permission.
