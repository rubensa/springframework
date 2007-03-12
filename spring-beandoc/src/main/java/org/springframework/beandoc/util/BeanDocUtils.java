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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beandoc.BeanDocException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;



/**
 * Collection of static utility methods used by BeanDoc.
 * 
 * @author Darren Davison
 * @author Michael Schuerig, <michael@schuerig.de>
 * @since 1.0
 */
public class BeanDocUtils {
    
    private static final Log logger = LogFactory.getLog(BeanDocUtils.class);
    
    private static final boolean isWindows = 
        System.getProperty("os.name").toLowerCase().indexOf("windows") > -1;
        
    private static final String FILE_SEP_REGEX = (isWindows ? "\\\\" : File.separator);
    
    private static FileFilter dirFilter = new FileFilter() {
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }            
    };
    
    private BeanDocUtils() {
        // no instances required
    }

    /**
     * Returns a new <code>Map</code> containing only those entries
     * from <code>map</code> whose key starts with <code>prefix</code>.
     * Prefixes are removed from the keys in the returned <code>Map</code>.
     * 
     * @param map the original <code>Map</code> to be filtered; must not be <code>null</code>
     * @param prefix prefix by which entries are filtered; may be empty of <code>null</code>
     * @return a new <code>Map</code> with filtered entries
     * @see #filterByPrefix(Map, String, boolean)
     */
    public static Map filterByPrefix(Map map, String prefix) {
        return filterByPrefix(map, prefix, true);        
    }
        
    /**
     * Returns a new <code>Map</code> containing only those entries
     * from <code>map</code> whose key starts with <code>prefix</code>.
     *
     * @param map the original <code>Map</code> to be filtered; must not be <code>null</code>
     * @param prefix prefix by which entries are filtered; may be empty of <code>null</code>
     * @param removePrefix should prefixes be removed from entries in the returned <code>Map</code>?
     * @return a new <code>Map</code> with filtered entries
     */
    public static Map filterByPrefix(
            final Map map,
            final String prefix, 
            final boolean removePrefix) {
        
        final HashMap filteredMap = new HashMap();    

        if (!StringUtils.hasText(prefix)) {
            filteredMap.putAll(map);
            return filteredMap;
        }

        for ( Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
            try {
                Map.Entry entry = (Entry)it.next();
                String name = (String)entry.getKey();
                if ( name.startsWith(prefix) ) {
                    filteredMap.put(
                            removePrefix ? name.substring(prefix.length()) : name,
                            entry.getValue());
                }
            } catch (Exception e) {
                logger.warn("Unable to filter Map.Entry; [" + e.getMessage() + "]");
            }
        }
        
        return filteredMap;
    }

    /**
     * Convert string values to physical resources
     * 
     * @param inputFileNames an array of Strings representing resource names
     * @return an array of <code>Resource</code>'s resolved from the input names
     */
    public static Resource[] getResources(String[] inputFileNames) throws IOException {
        // resolve resources assuming Files as the default (rather than classpath resources)
        ResourcePatternResolver resolver = 
            new PathMatchingResourcePatternResolver(new FileSystemResourceLoader());
        List allResources = new ArrayList();
        
        // each input location could resolve to multiple Resources..
        for (int i = 0; i < inputFileNames.length; i++) {
            Resource[] resources = resolver.getResources(inputFileNames[i]);
            allResources.addAll(Arrays.asList(resources));
        }
        
        Resource[] inputFiles = (Resource[]) 
            allResources.toArray(new Resource[allResources.size()]);
            
        return inputFiles;
    }

    /**
     * For the given array of Resources, return an equivalent length array of filenames
     * that uniquely identify each file-system resource from the shortest possible
     * common root.
     * <p>
     * For example, given the following Resources (files);
     * <pre>
     *   /projects/myproject/file1.xml
     *   /projects/myproject/module1/file1.xml
     *   /projects/myproject/module2/file1.xml
     * </pre>
     * 
     * the method will return a String array of the following names;
     * <pre>
     *   file1.xml
     *   module1/file1.xml
     *   module2/file1.xml
     * </pre>
     * 
     * having stripped the longest common path from all of the input names.
     * 
     * @param inputFiles an array of Resources which must be resolvable as
     * Files
     * @return a String array of filenames from a common root
     */
    public static String[] normaliseFileNames(Resource[] inputFiles) {
        if (inputFiles == null || inputFiles.length == 0)
            return new String[0];
        
        final int numFiles = inputFiles.length;        
        List tokenList = new ArrayList(numFiles);        
        for (int i = 0; i < numFiles; i++)
            try {
                tokenList.add(inputFiles[i].getFile().getAbsolutePath().split(FILE_SEP_REGEX));
            } catch (IOException e) {
                throw new BeanDocException("Failed to tokenize file resource paths.  Are your resources files?");
            }
        
        // if only one file, return the file
        if (numFiles == 1) {
            String[] only = (String[]) tokenList.get(0);
            return new String[] { only[only.length - 1] };
        }
        
        // sort list on length of String[] low to high
        Collections.sort(tokenList, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((String[]) o1).length - ((String[]) o2).length;
            }            
        });
        
        String[] first = (String[]) tokenList.get(0);
        int pathsMatching = first.length - 1;
        for (int i = 1; i < numFiles; i++) {
            String[] next = (String[]) tokenList.get(i);
            for (int j = 0; j < pathsMatching; j++) 
                if ( ! next[j].equals(first[j])) {
                    pathsMatching = j;
                    break;
                }
        }
        
        // strip 'pathsMatching' number of components from each array
        List fileList = new ArrayList(numFiles);
        for (int i = 0; i < numFiles; i++) {
            String[] tokens = (String[]) tokenList.get(i);
            StringBuffer fileName = new StringBuffer();
            for (int j = pathsMatching; j < tokens.length; j++) 
                fileName.append(tokens[j]).append(File.separatorChar);
            fileList.add(fileName.subSequence(0, fileName.length() - 1).toString());
        }
        String[] outputNames = (String[]) fileList.toArray(new String[numFiles]);
        return outputNames;
    }

    /**
     * Return a String denoting a relative path marker for the input file name (String).
     * For example, an input of "foo/bar/baz.html" has a relative marker of "../../"
     * denoting the two path components.
     * 
     * @param input
     * @return a relative marker
     */
    public static String getRelativePath(String input) {
        if (!StringUtils.hasLength(input) || input.indexOf('/') == -1)
            return "";
        
        String[] parts = input.split("/");        
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < parts.length; i++) {
            sb.append("../");
        }
        return sb.toString();
    }
    
    /**
     * Returns a <code>List</code> of <code>File</code> objects based on criteria specified.  The functionality
     * is equivalent to <code>File.listFiles()</code> but with the added benefit of recursing all
     * subdirectories from the root directory supplied.
     *
     * @param rootDir A <code>File</code> object which indicates the root location to
     * begin searching matching files.  If not an existing directory on
     * disk, an <code>IllegalArgumentException</code> will be thrown.
     * @param filter A <code>FileFilter</code> that is used to specify which <code>File</code>'s
     * should be returned.
     * @return A <code>List</code> of <code>File</code> objects that match the 
     * criteria specified in the <code>FileFilter</code> parameter
     */
    public static List listFilesRecursively(File rootDir, FileFilter filter) {        
        if (! rootDir.isDirectory())
            throw new IllegalArgumentException("rootDir is not a directory");
        
        ArrayList files = new ArrayList();
        recurse(rootDir, filter, files);        
        return files;    
    }
       
    private static void recurse(File dir, FileFilter filter, List allFiles) {
        File[] f = dir.listFiles(filter);
        for (int i = 0; i < f.length; i++) 
            allFiles.add(f[i]);

        File[] d = dir.listFiles(dirFilter);
        for (int i = 0; i < d.length; i++) 
            recurse(d[i], filter, allFiles);
    }

}
