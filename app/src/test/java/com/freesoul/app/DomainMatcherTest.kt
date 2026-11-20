package com.freesoul.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainMatcherTest {
    private val rules = setOf("xnxx.com", "cake.ski")

    @Test fun parentAndAllDescendantsAreBlocked() {
        assertTrue(DomainMatcher.isBlocked("xnxx.com", rules))
        assertTrue(DomainMatcher.isBlocked("www.xnxx.com", rules))
        assertTrue(DomainMatcher.isBlocked("asian.xnxx.com", rules))
        assertTrue(DomainMatcher.isBlocked("deep.asian.xnxx.com", rules))
    }

    @Test fun similarNamesAreNotBlocked() {
        assertFalse(DomainMatcher.isBlocked("notxnxx.com", rules))
        assertFalse(DomainMatcher.isBlocked("xnxx.com.example.org", rules))
    }

    @Test fun hostnameIsNormalized() {
        assertTrue(DomainMatcher.isBlocked("WWW.CAKE.SKI.", rules))
    }

    @Test fun invalidHostnamesAreNotBlocked() {
        assertFalse(DomainMatcher.isBlocked("not a hostname", rules))
        assertFalse(DomainMatcher.isBlocked("localhost", rules))
    }
}
