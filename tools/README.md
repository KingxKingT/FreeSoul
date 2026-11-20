# FreeSoul domain import tool

This tool converts a **permitted export or user-supplied TXT/CSV file** into a CSV review queue. It does not crawl ThePornDude or any other website.

## Run

```bash
python tools/import_domains.py candidates.txt -o domain-review.csv
python -m unittest discover -s tests -v
```

Input may contain one URL or hostname per line, or a CSV with a `domain` or `url` column. The output is only a review queue. Every accepted hostname is marked `needs_manual_review`; this tool never adds entries to the live blocklist.

## Before importing directory data

The directory's terms and robots instructions must be checked, and automated collection/redistribution must be authorized. A robots file could not be verified through the available page retrieval, and no official export/API or permission has been confirmed. Therefore this repository does not include a crawler. Do not bypass a CAPTCHA, bot challenge, login, or access restriction.

## Important limitations

- This performs syntax normalization, not a full Public Suffix List lookup.
- It does not determine whether a site is adult, current, safe to block, or legally redistributable.
- Review each domain's purpose, ownership, shared legitimate use, provenance, and false-positive risk.
- Keep mixed-use services in a separate manual-review category.
