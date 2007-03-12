/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.beandoc.util;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * @author Darren Davison
 * @since 1.0
 */
public class BeanDocUtilsTests extends TestCase {    

    Map m;
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        m = new HashMap();
        m.put("key1", new Object());
        m.put("key2", new Object());
        m.put("prefix.pkey1", new Object());
        m.put("prefix.pkey2", new Object());
        m.put("key3", new Object());
    }
    
    public void testFilterMapByPrefixAndStripPrefix() {
        Map filtered = BeanDocUtils.filterByPrefix(m, "prefix.");
        assertEquals(2, filtered.size());
        assertTrue(filtered.containsKey("pkey1"));
        assertTrue(filtered.containsKey("pkey2"));
        assertFalse(filtered.containsKey("key1"));
    }
    
    public void testFilterMapByPrefixAndLeavePrefix() {
        Map filtered = BeanDocUtils.filterByPrefix(m, "prefix.", false);
        assertEquals(2, filtered.size());
        assertTrue(filtered.containsKey("prefix.pkey1"));
        assertTrue(filtered.containsKey("prefix.pkey2"));
        assertFalse(filtered.containsKey("pkey1"));
        assertFalse(filtered.containsKey("key1"));
    }
    
    public void testFilterMapByPrefixWithEmptyPrefix() {
        Map filtered = BeanDocUtils.filterByPrefix(m, "");
        assertEquals(m, filtered);
    }
    
    public void testNonStringKeyInMap() {
        m.put(new Object(), new Object());
        try {
            BeanDocUtils.filterByPrefix(m, "prefix.", true);
        } catch (Exception e) {
            fail(); // s/be swallowed
        }
    }
    
    public void testNormaliseFileNames() {
        Resource[] inputs = new Resource[] {
            new FileSystemResource("/projects/myproject/module2/file1.xml"),
            new FileSystemResource("/projects/myproject/file1.xml"),
            new FileSystemResource("/projects/myproject/module1/file1.xml"),
            new FileSystemResource("/projects/myproject/module1/file2.xml")
        };
        String[] outputs = BeanDocUtils.normaliseFileNames(inputs);
        assertEquals(inputs.length, outputs.length);
        assertEquals("module2" + File.separator + "file1.xml", outputs[1]);
        assertEquals("file1.xml", outputs[0]);
        assertEquals("module1" + File.separator + "file1.xml", outputs[2]);        
    }
    
    public void testGetRelativePath() {
        String p = BeanDocUtils.getRelativePath("foo/bar/baz.html");
        assertEquals("../../", p);
    }
    
    public void testFileList() throws Exception {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        if (tmpDir.exists() && tmpDir.canWrite()) {
            // create some files and dirs
            File root = new File(tmpDir, "beandoc-test");
            root.mkdir();
            File f;
            f = new File(root, "1.java");
            f.createNewFile();
            f = new File(root, "2.java");
            f.createNewFile();
            f = new File(root, "3.java");
            f.createNewFile();
            f = new File(root, "1.cpp");
            f.createNewFile();
            File sub = new File(root, "subdir");
            sub.mkdir();
            f = new File(sub, "1.cpp");
            f.createNewFile();
            f = new File(sub, "2.java");
            f.createNewFile();
            
            // read 'em
            List l = BeanDocUtils.listFilesRecursively(root, 
                new FileFilter() {
                    public boolean accept(File pathname) {
                        return pathname.getName().endsWith(".java");
                    }                
            });
            
            assertEquals(4, l.size());
        }
    }

}
