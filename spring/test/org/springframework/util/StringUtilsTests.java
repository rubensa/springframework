/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.util;

import java.util.Arrays;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class StringUtilsTests extends TestCase {

	public void testHasTextBlank() throws Exception {
		String blank = "          ";
		assertEquals(false, StringUtils.hasText(blank));
	}

	public void testHasTextNullEmpty() throws Exception {
		assertEquals(false, StringUtils.hasText(null));
		assertEquals(false, StringUtils.hasText(""));
	}

	public void testHasTextValid() throws Exception {
		assertEquals(true, StringUtils.hasText("t"));
	}

	public void testTrimLeadingWhitespace() throws Exception {
		assertEquals("", StringUtils.trimLeadingWhitespace(""));
		assertEquals("", StringUtils.trimLeadingWhitespace(" "));
		assertEquals("", StringUtils.trimLeadingWhitespace("\t"));
		assertEquals("a", StringUtils.trimLeadingWhitespace(" a"));
		assertEquals("a ", StringUtils.trimLeadingWhitespace("a "));
		assertEquals("a ", StringUtils.trimLeadingWhitespace(" a "));
	}

	public void testTrimTrailingWhitespace() throws Exception {
		assertEquals("", StringUtils.trimTrailingWhitespace(""));
		assertEquals("", StringUtils.trimTrailingWhitespace(" "));
		assertEquals("", StringUtils.trimTrailingWhitespace("\t"));
		assertEquals("a", StringUtils.trimTrailingWhitespace("a "));
		assertEquals(" a", StringUtils.trimTrailingWhitespace(" a"));
		assertEquals(" a", StringUtils.trimTrailingWhitespace(" a "));
	}

	public void testCountOccurrencesOf() {
		assertTrue("nullx2 = 0",
				StringUtils.countOccurrencesOf(null, null) == 0);
		assertTrue("null string = 0",
				StringUtils.countOccurrencesOf("s", null) == 0);
		assertTrue("null substring = 0",
				StringUtils.countOccurrencesOf(null, "s") == 0);
		String s = "erowoiueoiur";
		assertTrue("not found = 0",
				StringUtils.countOccurrencesOf(s, "WERWER") == 0);
		assertTrue("not found char = 0",
				StringUtils.countOccurrencesOf(s, "x") == 0);
		assertTrue("not found ws = 0",
				StringUtils.countOccurrencesOf(s, " ") == 0);
		assertTrue("not found empty string = 0",
				StringUtils.countOccurrencesOf(s, "") == 0);
		assertTrue("found char=2", StringUtils.countOccurrencesOf(s, "e") == 2);
		assertTrue("found substring=2",
				StringUtils.countOccurrencesOf(s, "oi") == 2);
		assertTrue("found substring=2",
				StringUtils.countOccurrencesOf(s, "oiu") == 2);
		assertTrue("found substring=3",
				StringUtils.countOccurrencesOf(s, "oiur") == 1);
		assertTrue("test last", StringUtils.countOccurrencesOf(s, "r") == 2);
	}

	public void testReplace() throws Exception {
		String inString = "a6AazAaa77abaa";
		String oldPattern = "aa";
		String newPattern = "foo";

		// Simple replace
		String s = StringUtils.replace(inString, oldPattern, newPattern);
		assertTrue("Replace 1 worked", s.equals("a6AazAfoo77abfoo"));

		// Non match: no change
		s = StringUtils.replace(inString, "qwoeiruqopwieurpoqwieur", newPattern);
		assertTrue("Replace non matched is equal", s.equals(inString));

		// Null new pattern: should ignore
		s = StringUtils.replace(inString, oldPattern, null);
		assertTrue("Replace non matched is equal", s.equals(inString));

		// Null old pattern: should ignore
		s = StringUtils.replace(inString, null, newPattern);
		assertTrue("Replace non matched is equal", s.equals(inString));
	}

	public void testDelete() throws Exception {
		String inString = "The quick brown fox jumped over the lazy dog";

		String noThe = StringUtils.delete(inString, "the");
		assertTrue("Result has no the [" + noThe + "]",
				noThe.equals("The quick brown fox jumped over  lazy dog"));

		String nohe = StringUtils.delete(inString, "he");
		assertTrue("Result has no he [" + nohe + "]",
				nohe.equals("T quick brown fox jumped over t lazy dog"));

		String nosp = StringUtils.delete(inString, " ");
		assertTrue("Result has no spaces",
				nosp.equals("Thequickbrownfoxjumpedoverthelazydog"));

		String killEnd = StringUtils.delete(inString, "dog");
		assertTrue("Result has no dog",
				killEnd.equals("The quick brown fox jumped over the lazy "));

		String mismatch = StringUtils.delete(inString, "dxxcxcxog");
		assertTrue("Result is unchanged", mismatch.equals(inString));
	}

	public void testDeleteAny() throws Exception {
		String inString = "Able was I ere I saw Elba";

		String res = StringUtils.deleteAny(inString, "I");
		assertTrue("Result has no Is [" + res + "]",
				res.equals("Able was  ere  saw Elba"));

		res = StringUtils.deleteAny(inString, "AeEba!");
		assertTrue("Result has no Is [" + res + "]",
				res.equals("l ws I r I sw l"));

		String mismatch = StringUtils.deleteAny(inString, "#@$#$^");
		assertTrue("Result is unchanged", mismatch.equals(inString));

		String whitespace =
				"This is\n\n\n    \t   a messagy string with whitespace\n";
		assertTrue("Has CR", whitespace.indexOf("\n") != -1);
		assertTrue("Has tab", whitespace.indexOf("\t") != -1);
		assertTrue("Has  sp", whitespace.indexOf(" ") != -1);
		String cleaned = StringUtils.deleteAny(whitespace, "\n\t ");
		assertTrue("Has no CR", cleaned.indexOf("\n") == -1);
		assertTrue("Has no tab", cleaned.indexOf("\t") == -1);
		assertTrue("Has no sp", cleaned.indexOf(" ") == -1);
		assertTrue("Still has chars", cleaned.length() > 10);
	}


	public void testQuote() {
		assertEquals("'myString'", StringUtils.quote("myString"));
		assertEquals("''", StringUtils.quote(""));
		assertNull(StringUtils.quote(null));
	}

	public void testQuoteIfString() {
		assertEquals("'myString'", StringUtils.quoteIfString("myString"));
		assertEquals("''", StringUtils.quoteIfString(""));
		assertEquals(new Integer(5), StringUtils.quoteIfString(new Integer(5)));
		assertNull(StringUtils.quoteIfString(null));
	}

	public void testUnqualify() {
		String qualified = "i.am.not.unqualified";
		assertEquals("unqualified", StringUtils.unqualify(qualified));
	}

	public void testCapitalize() {
		String capitalized = "i am not capitalized";
		assertEquals("I am not capitalized", StringUtils.capitalize(capitalized));
	}

	public void testUncapitalize() {
		String capitalized = "I am capitalized";
		assertEquals("i am capitalized", StringUtils.uncapitalize(capitalized));
	}

	public void testGetFilename() {
		assertEquals(null, StringUtils.getFilename(null));
		assertEquals("", StringUtils.getFilename(""));
		assertEquals("myfile", StringUtils.getFilename("myfile"));
		assertEquals("myfile", StringUtils.getFilename("mypath/myfile"));
		assertEquals("myfile.", StringUtils.getFilename("myfile."));
		assertEquals("myfile.", StringUtils.getFilename("mypath/myfile."));
		assertEquals("myfile.txt", StringUtils.getFilename("myfile.txt"));
		assertEquals("myfile.txt", StringUtils.getFilename("mypath/myfile.txt"));
	}

	public void testGetFilenameExtension() {
		assertEquals(null, StringUtils.getFilenameExtension(null));
		assertEquals(null, StringUtils.getFilenameExtension(""));
		assertEquals(null, StringUtils.getFilenameExtension("myfile"));
		assertEquals(null, StringUtils.getFilenameExtension("myPath/myfile"));
		assertEquals("", StringUtils.getFilenameExtension("myfile."));
		assertEquals("", StringUtils.getFilenameExtension("myPath/myfile."));
		assertEquals("txt", StringUtils.getFilenameExtension("myfile.txt"));
		assertEquals("txt", StringUtils.getFilenameExtension("mypath/myfile.txt"));
	}

	public void stripFilenameExtension() {
		assertEquals(null, StringUtils.stripFilenameExtension(null));
		assertEquals("", StringUtils.stripFilenameExtension(""));
		assertEquals("myfile", StringUtils.stripFilenameExtension("myfile"));
		assertEquals("mypath/myfile", StringUtils.stripFilenameExtension("mypath/myfile"));
		assertEquals("myfile", StringUtils.stripFilenameExtension("myfile."));
		assertEquals("mypath/myfile", StringUtils.stripFilenameExtension("mypath/myfile."));
		assertEquals("myfile", StringUtils.stripFilenameExtension("myfile.txt"));
		assertEquals("mypath/myfile", StringUtils.stripFilenameExtension("mypath/myfile.txt"));
	}

	public void testPathEquals() {
		assertTrue("Must be true for the same strings",
				StringUtils.pathEquals("/dummy1/dummy2/dummy3",
						"/dummy1/dummy2/dummy3"));
		assertTrue("Must be true for the same win strings",
				StringUtils.pathEquals("C:\\dummy1\\dummy2\\dummy3",
						"C:\\dummy1\\dummy2\\dummy3"));
		assertTrue("Must be true for one top path on 1",
				StringUtils.pathEquals("/dummy1/bin/../dummy2/dummy3",
						"/dummy1/dummy2/dummy3"));
		assertTrue("Must be true for one win top path on 2",
				StringUtils.pathEquals("C:\\dummy1\\dummy2\\dummy3",
						"C:\\dummy1\\bin\\..\\dummy2\\dummy3"));
		assertTrue("Must be true for two top paths on 1",
				StringUtils.pathEquals("/dummy1/bin/../dummy2/bin/../dummy3",
						"/dummy1/dummy2/dummy3"));
		assertTrue("Must be true for two win top paths on 2",
				StringUtils.pathEquals("C:\\dummy1\\dummy2\\dummy3",
						"C:\\dummy1\\bin\\..\\dummy2\\bin\\..\\dummy3"));
		assertTrue("Must be true for double top paths on 1",
				StringUtils.pathEquals("/dummy1/bin/tmp/../../dummy2/dummy3",
						"/dummy1/dummy2/dummy3"));
		assertTrue("Must be true for double top paths on 2 with similarity",
				StringUtils.pathEquals("/dummy1/dummy2/dummy3",
						"/dummy1/dum/dum/../../dummy2/dummy3"));
		assertTrue("Must be true for current paths",
				StringUtils.pathEquals("./dummy1/dummy2/dummy3",
						"dummy1/dum/./dum/../../dummy2/dummy3"));
		assertFalse("Must be false for relative/absolute paths",
				StringUtils.pathEquals("./dummy1/dummy2/dummy3",
						"/dummy1/dum/./dum/../../dummy2/dummy3"));
		assertFalse("Must be false for different strings",
				StringUtils.pathEquals("/dummy1/dummy2/dummy3",
						"/dummy1/dummy4/dummy3"));
		assertFalse("Must be false for one false path on 1",
				StringUtils.pathEquals("/dummy1/bin/tmp/../dummy2/dummy3",
						"/dummy1/dummy2/dummy3"));
		assertFalse("Must be false for one false win top path on 2",
				StringUtils.pathEquals("C:\\dummy1\\dummy2\\dummy3",
						"C:\\dummy1\\bin\\tmp\\..\\dummy2\\dummy3"));
		assertFalse("Must be false for top path on 1 + difference",
				StringUtils.pathEquals("/dummy1/bin/../dummy2/dummy3",
						"/dummy1/dummy2/dummy4"));
	}


	public void testSortStringArray() {
		String[] input = new String[] {"myString2"};
		input = StringUtils.addStringToArray(input, "myString1");
		assertEquals("myString2", input[0]);
		assertEquals("myString1", input[1]);

		StringUtils.sortStringArray(input);
		assertEquals("myString1", input[0]);
		assertEquals("myString2", input[1]);
	}

	public void testRemoveDuplicateStrings() {
		String[] input = new String[] {"myString2", "myString1", "myString2"};
		input = StringUtils.removeDuplicateStrings(input);
		assertEquals("myString1", input[0]);
		assertEquals("myString2", input[1]);
	}

	public void testSplitArrayElementsIntoProperties() {
		String[] input = new String[] {"key1=value1 ", "key2 =\"value2\""};
		Properties result = StringUtils.splitArrayElementsIntoProperties(input, "=");
		assertEquals("value1", result.getProperty("key1"));
		assertEquals("\"value2\"", result.getProperty("key2"));
	}

	public void testSplitArrayElementsIntoPropertiesAndDeletedChars() {
		String[] input = new String[] {"key1=value1 ", "key2 =\"value2\""};
		Properties result = StringUtils.splitArrayElementsIntoProperties(input, "=", "\"");
		assertEquals("value1", result.getProperty("key1"));
		assertEquals("value2", result.getProperty("key2"));
	}

	public void testTokenizeToStringArray() {
		String[] sa = StringUtils.tokenizeToStringArray("a,b , ,c", ",");
		assertEquals(3, sa.length);
		assertTrue("components are correct",
				sa[0].equals("a") && sa[1].equals("b") && sa[2].equals("c"));
	}

	public void testTokenizeToStringArrayWithNotIgnoreEmptyTokens() {
		String[] sa = StringUtils.tokenizeToStringArray("a,b , ,c", ",", true, false);
		assertEquals(4, sa.length);
		assertTrue("components are correct",
				sa[0].equals("a") && sa[1].equals("b") && sa[2].equals("") && sa[3].equals("c"));
	}

	public void testTokenizeToStringArrayWithNotTrimTokens() {
		String[] sa = StringUtils.tokenizeToStringArray("a,b ,c", ",", false, true);
		assertEquals(3, sa.length);
		assertTrue("components are correct",
				sa[0].equals("a") && sa[1].equals("b ") && sa[2].equals("c"));
	}

	public void testCommaDelimitedListToStringArrayWithNullProducesEmptyArray() {
		String[] sa = StringUtils.commaDelimitedListToStringArray(null);
		assertTrue("String array isn't null with null input", sa != null);
		assertTrue("String array length == 0 with null input", sa.length == 0);
	}

	public void testCommaDelimitedListToStringArrayWithEmptyStringProducesEmptyArray() {
		String[] sa = StringUtils.commaDelimitedListToStringArray("");
		assertTrue("String array isn't null with null input", sa != null);
		assertTrue("String array length == 0 with null input", sa.length == 0);
	}

	private void testStringArrayReverseTransformationMatches(String[] sa) {
		String[] reverse =
				StringUtils.commaDelimitedListToStringArray(StringUtils.arrayToCommaDelimitedString(sa));
		assertEquals("Reverse transformation is equal",
				Arrays.asList(sa),
				Arrays.asList(reverse));
	}

	public void testDelimitedListToStringArrayWithComma() {
		String[] sa = StringUtils.delimitedListToStringArray("a,b", ",");
		assertEquals(2, sa.length);
		assertEquals("a", sa[0]);
		assertEquals("b", sa[1]);
	}

	public void testDelimitedListToStringArrayWithSemicolon() {
		String[] sa = StringUtils.delimitedListToStringArray("a;b", ";");
		assertEquals(2, sa.length);
		assertEquals("a", sa[0]);
		assertEquals("b", sa[1]);
	}

	public void testDelimitedListToStringArrayWithEmptyString() {
		String[] sa = StringUtils.delimitedListToStringArray("a,b", "");
		assertEquals(3, sa.length);
		assertEquals("a", sa[0]);
		assertEquals(",", sa[1]);
		assertEquals("b", sa[2]);
	}

	public void testDelimitedListToStringArrayWithNullDelimiter() {
		String[] sa = StringUtils.delimitedListToStringArray("a,b", null);
		assertEquals(1, sa.length);
		assertEquals("a,b", sa[0]);
	}

	public void testCommaDelimitedListToStringArrayMatchWords() {
		// Could read these from files
		String[] sa = new String[] {"foo", "bar", "big"};
		doTestCommaDelimitedListToStringArrayLegalMatch(sa);
		testStringArrayReverseTransformationMatches(sa);

		sa = new String[] {"a", "b", "c"};
		doTestCommaDelimitedListToStringArrayLegalMatch(sa);
		testStringArrayReverseTransformationMatches(sa);

		// Test same words
		sa = new String[] {"AA", "AA", "AA", "AA", "AA"};
		doTestCommaDelimitedListToStringArrayLegalMatch(sa);
		testStringArrayReverseTransformationMatches(sa);
	}

	public void testCommaDelimitedListToStringArraySingleString() {
		// Could read these from files
		String s = "woeirqupoiewuropqiewuorpqiwueopriquwopeiurqopwieur";
		String[] sa = StringUtils.commaDelimitedListToStringArray(s);
		assertTrue("Found one String with no delimiters", sa.length == 1);
		assertTrue("Single array entry matches input String with no delimiters",
				sa[0].equals(s));
	}

	public void testCommaDelimitedListToStringArrayWithOtherPunctuation() {
		// Could read these from files
		String[] sa = new String[] {"xcvwert4456346&*.", "///", ".!", ".", ";"};
		doTestCommaDelimitedListToStringArrayLegalMatch(sa);
	}

	/**
	 * We expect to see the empty Strings in the output.
	 */
	public void testCommaDelimitedListToStringArrayEmptyStrings() {
		// Could read these from files
		String[] sa = StringUtils.commaDelimitedListToStringArray("a,,b");
		assertEquals("a,,b produces array length 3", 3, sa.length);
		assertTrue("components are correct",
				sa[0].equals("a") && sa[1].equals("") && sa[2].equals("b"));

		sa = new String[] {"", "", "a", ""};
		doTestCommaDelimitedListToStringArrayLegalMatch(sa);
	}

	public void testEndsWithIgnoreCase() {
		String suffix = "fOo";

		assertTrue(StringUtils.endsWithIgnoreCase("barfoo", suffix));
		assertTrue(StringUtils.endsWithIgnoreCase("barFoo", suffix));
		assertTrue(StringUtils.endsWithIgnoreCase("barfoO", suffix));
		assertTrue(StringUtils.endsWithIgnoreCase("barFOO", suffix));
		assertTrue(StringUtils.endsWithIgnoreCase("barfOo", suffix));
		assertFalse(StringUtils.endsWithIgnoreCase(null, suffix));
		assertFalse(StringUtils.endsWithIgnoreCase("barfOo", null));
		assertFalse(StringUtils.endsWithIgnoreCase("b", suffix));
	}

	private void doTestCommaDelimitedListToStringArrayLegalMatch(String[] components) {
		StringBuffer sbuf = new StringBuffer();
		for (int i = 0; i < components.length; i++) {
			if (i != 0) {
				sbuf.append(",");
			}
			sbuf.append(components[i]);
		}

		String[] sa = StringUtils.commaDelimitedListToStringArray(sbuf.toString());
		assertTrue("String array isn't null with legal match", sa != null);
		assertEquals("String array length is correct with legal match", components.length, sa.length);
		assertTrue("Output equals input", Arrays.equals(sa, components));
	}

}
