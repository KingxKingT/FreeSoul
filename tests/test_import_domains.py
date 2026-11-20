import tempfile
import unittest
from pathlib import Path
from tools.import_domains import extract_host


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


if __name__ == "__main__":
    unittest.main()
