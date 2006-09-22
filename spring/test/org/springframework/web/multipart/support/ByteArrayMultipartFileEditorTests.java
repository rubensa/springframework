/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.web.multipart.support;

import java.io.IOException;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Rick Evans
 */
public final class ByteArrayMultipartFileEditorTests extends TestCase {

	public void testSetValueAsByteArray() throws Exception {
		ByteArrayMultipartFileEditor editor = new ByteArrayMultipartFileEditor();
		String expectedValue = "Shumwere, shumhow, a shuck ish washing you. - Drunken Far Side";
		editor.setValue(expectedValue.getBytes());
		assertEquals(expectedValue, editor.getAsText());
	}

	public void testSetValueAsString() throws Exception {
		ByteArrayMultipartFileEditor editor = new ByteArrayMultipartFileEditor();
		String expectedValue = "'Green Wing' - classic British comedy";
		editor.setValue(expectedValue);
		assertEquals(expectedValue, editor.getAsText());
	}

	public void testSetValueAsCustomObjectInvokesToString() throws Exception {
		ByteArrayMultipartFileEditor editor = new ByteArrayMultipartFileEditor();
		final String expectedValue = "'Green Wing' - classic British comedy";
		Object object = new Object() {
			public String toString() {
				return expectedValue;
			}
		};
		editor.setValue(object);
		assertEquals(expectedValue, editor.getAsText());
	}

	public void testSetValueAsNullGetsBackEmptyString() throws Exception {
		ByteArrayMultipartFileEditor editor = new ByteArrayMultipartFileEditor();
		editor.setValue(null);
		assertEquals("", editor.getAsText());
	}

	public void testSetValueAsMultipartFile() throws Exception {
		String expectedValue = "That is comforting to know";
		ByteArrayMultipartFileEditor editor = new ByteArrayMultipartFileEditor();
		MockControl mock = MockControl.createControl(MultipartFile.class);
		MultipartFile file = (MultipartFile) mock.getMock();
		file.getBytes();
		mock.setReturnValue(expectedValue.getBytes());
		mock.replay();
		editor.setValue(file);
		assertEquals(expectedValue, editor.getAsText());
		mock.verify();
	}

	public void testSetValueAsMultipartFileWithBadBytes() throws Exception {
		ByteArrayMultipartFileEditor editor = new ByteArrayMultipartFileEditor();
		MockControl mock = MockControl.createControl(MultipartFile.class);
		MultipartFile file = (MultipartFile) mock.getMock();
		file.getBytes();
		mock.setThrowable(new IOException());
		mock.replay();
		try {
			editor.setValue(file);
			fail("Must have thrown an IllegalArgumentException: IOException thrown when reading MultipartFile bytes");
		}
		catch (IllegalArgumentException expected) {
		}
		mock.verify();
	}

}
