# FreeSoul Android test build

This repository includes an early Android DNS-filtering prototype for sideload testing on Android 10+. It is **not production-ready**.

## What it does

- Starts Android `VpnService` after the user approves the VPN permission dialog.
- Routes DNS lookups through a local VPN DNS address; ordinary web traffic is not tunneled.
- Checks hostnames against the small test list in `app/src/main/assets/adult_domains.txt`.
- Blocks a listed hostname and all DNS subdomains using a label-boundary check.
- Returns NXDOMAIN for blocked names. Other DNS queries are forwarded to Cloudflare DNS, with Google DNS as fallback.
- Keeps a blocked-request counter locally. FreeSoul does not upload browsing history.

## Test list

The APK contains seven adult-purpose candidate domains, not a complete blocklist. The large uploaded hostname inventory remains review-only and is not bundled into the APK.

## Limitations

- IPv4 UDP DNS only. DNS-over-TCP, IPv6-specific paths, browser DNS-over-HTTPS, and strict Android Private DNS may bypass this prototype.
- DNS filtering blocks hostnames, not individual pages, images, chat messages, or videos.
- Only one Android VPN can normally be active at a time.
- Allowed DNS lookups are sent in plaintext UDP to Cloudflare or Google DNS.
- The small test list is not complete protection.

For the initial test, temporarily disable browser Secure DNS / DNS-over-HTTPS and set Android Private DNS to Off or Automatic, not a strict provider hostname. This reduces DNS privacy during the test.

## Build

GitHub Actions builds `app-debug.apk` and uploads it as the `FreeSoul-debug-apk` workflow artifact. Local builds require JDK 17, Android SDK Platform 35, Build Tools 35.0.0, and Gradle 8.9.
