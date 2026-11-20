import unittest
from tools.import_domains import extract_host, matches_block_rule, is_shared_hosting_domain


class ImportDomainTests(unittest.TestCase):
    def test_normalizes_url_and_case(self):
        self.assertEqual(extract_host("HTTPS://WWW.Example.COM/path")[0], "www.example.com")

    def test_idn_is_converted(self):
        self.assertEqual(extract_host("bücher.de")[0], "xn--bcher-kva.de")

    def test_rejects_ip_and_localhost(self):
        self.assertIsNone(extract_host("127.0.0.1")[0])
        self.assertIsNone(extract_host("localhost")[0])

    def test_rejects_invalid_label(self):
        self.assertIsNone(extract_host("-bad.example")[0])

    def test_never_auto_approves(self):
        self.assertEqual(extract_host("example.com")[1], "needs_manual_review")

    def test_parent_rule_matches_subdomains(self):
        self.assertTrue(matches_block_rule("asian.xnxx.com", "xnxx.com"))
        self.assertTrue(matches_block_rule("deep.asian.xnxx.com", "xnxx.com"))
        self.assertTrue(matches_block_rule("xnxx.com", "xnxx.com"))

    def test_suffix_matching_respects_dns_label_boundary(self):
        self.assertFalse(matches_block_rule("notxnxx.com", "xnxx.com"))
        self.assertFalse(matches_block_rule("xnxx.com.attacker.test", "xnxx.com"))

    def test_shared_host_tenant_is_not_widened(self):
        self.assertTrue(is_shared_hosting_domain("someone.tumblr.com"))
        self.assertFalse(matches_block_rule("other.tumblr.com", "someone.tumblr.com"))

    def test_adblock_syntax_is_normalized(self):
        self.assertEqual(extract_host("||asian.xnxx.com^")[0], "asian.xnxx.com")
        self.assertEqual(extract_host("*.xnxx.com")[0], "xnxx.com")


if __name__ == "__main__":
    unittest.main()
