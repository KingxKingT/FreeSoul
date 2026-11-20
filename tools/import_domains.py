#!/usr/bin/env python3
"""Turn a permitted/user-supplied domain export into a review queue.

This tool does not crawl websites and never marks a domain as approved.
Input: one URL or hostname per line, or a CSV with a 'domain'/'url' column.
"""
from __future__ import annotations

import argparse
import csv
import ipaddress
import re
import sys
from pathlib import Path
from urllib.parse import urlsplit

LABEL = re.compile(r"^[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?$")
COMMON_MULTIPART_SUFFIXES = {
    "co.uk", "org.uk", "ac.uk", "com.au", "net.au", "org.au",
    "co.nz", "org.nz", "co.jp", "co.za", "com.br", "com.mx",
}


def extract_host(raw: str) -> tuple[str | None, str]:
    value = raw.strip().strip('"').strip("'")
    if not value or value.startswith("#"):
        return None, "empty_or_comment"
    if any(ch.isspace() for ch in value):
        return None, "whitespace_inside_value"
    candidate = value if "://" in value else "//" + value
    try:
        parsed = urlsplit(candidate)
        host = parsed.hostname
    except ValueError:
        return None, "malformed_url"
    if not host:
        return None, "no_hostname"
    host = host.rstrip(".").lower()
    try:
        host = host.encode("idna").decode("ascii")
    except UnicodeError:
        return None, "invalid_idn"
    if len(host) > 253:
        return None, "hostname_too_long"
    try:
        ipaddress.ip_address(host)
        return None, "ip_address_not_domain"
    except ValueError:
        pass
    if host == "localhost" or "." not in host:
        return None, "not_fully_qualified"
    labels = host.split(".")
    if any(not LABEL.fullmatch(label) for label in labels):
        return None, "invalid_dns_label"
    suffix2 = ".".join(labels[-2:])
    if suffix2 in COMMON_MULTIPART_SUFFIXES and len(labels) <= 2:
        return None, "public_suffix_not_registrable_domain"
    if len(labels[-1]) < 2:
        return None, "invalid_top_level_label"
    return host, "needs_manual_review"


def read_values(path: Path):
    text = path.read_text(encoding="utf-8-sig")
    # CSV is recognized by its header; otherwise treat each non-comment line as an entry.
    first = next((line for line in text.splitlines() if line.strip() and not line.lstrip().startswith("#")), "")
    if "," in first:
        rows = csv.DictReader(text.splitlines())
        fields = {name.lower(): name for name in (rows.fieldnames or [])}
        key = fields.get("domain") or fields.get("url")
        if key:
            for row in rows:
                yield row.get(key, "")
            return
    yield from text.splitlines()


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input", type=Path, help="Permitted export or user-provided TXT/CSV")
    parser.add_argument("-o", "--output", type=Path, default=Path("domain-review.csv"))
    args = parser.parse_args()
    if not args.input.is_file():
        parser.error(f"input file not found: {args.input}")
    seen = set()
    accepted = []
    rejected = []
    for line_no, raw in enumerate(read_values(args.input), 1):
        host, status = extract_host(raw)
        if host:
            if host in seen:
                rejected.append((line_no, raw, "duplicate"))
            else:
                seen.add(host)
                accepted.append((host, line_no, "needs_manual_review"))
        elif status not in {"empty_or_comment"}:
            rejected.append((line_no, raw, status))
    with args.output.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow(["hostname", "source_line", "status", "category", "review_notes"])
        for host, line_no, status in accepted:
            writer.writerow([host, line_no, status, "unclassified", "Verify purpose, ownership, false-positive risk, source rights, and date before blocking."])
    print(f"Review queue: {len(accepted)} unique hostnames -> {args.output}")
    print(f"Rejected/duplicate rows: {len(rejected)}")
    if rejected:
        reject_path = args.output.with_name(args.output.stem + "-rejected.csv")
        with reject_path.open("w", newline="", encoding="utf-8") as handle:
            writer = csv.writer(handle)
            writer.writerow(["source_line", "input", "reason"])
            writer.writerows(rejected)
        print(f"Rejected rows: {reject_path}")
    print("Safety: nothing is approved or added to an active blocklist by this tool.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
