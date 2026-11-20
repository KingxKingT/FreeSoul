# FreeSoul hostname blocking rules

## Matching behavior

A rule for `example.com` matches:
- `example.com`
- `www.example.com`
- `asian.example.com`
- `deep.asian.example.com`

It must **not** match:
- `notexample.com`
- `example.com.attacker.test`

Use a DNS-label boundary check:

```python
hostname == rule or hostname.endswith("." + rule)
```

Never use a substring check such as `"example.com" in hostname`.

## Preserve tenant boundaries

Do not collapse a listed tenant hostname into a shared provider root. For example, if a source lists `some-account.tumblr.com`, keep that exact hostname as the rule. Do not turn it into `tumblr.com`. Apply the same caution to blog hosts, free hosting, page builders, and other multi-tenant services.

A parent rule covers all descendants of that parent. This solves the case where a main adult domain has alternate subdomains without having to enumerate each one, while avoiding broad blocks of shared platforms.

## DNS limitations

DNS filtering blocks hostnames, not individual page paths or content items. If adult and legitimate material are served from the same hostname, a DNS rule cannot reliably distinguish them. Keep those domains in manual review or use a separate content-control mechanism; do not pretend a domain list can selectively block one page.

## Promotion

Only reviewed candidates become active rules. Every active rule needs provenance, a review date, a category, and regression checks for related legitimate sites.
