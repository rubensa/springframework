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

package org.springframework.beandoc;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.springframework.beandoc.transform.Transformer;
import org.springframework.beandoc.transform.XslTransformer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

/**
 * Configuration holds all of the configurable, and some non-configurable (internal) values
 * used by the BeanDoc tool.  This class can be used for programmatic access directly, but
 * is more likely to be used as the command object for CLI or AntTask interfaces to the
 * BeanDoc tool.
 * <p>
 * The simplest way to programmatically document an application context is to get a default
 * Configuration (supplying input files and output directory) and pass this object to the 
 * bean doc tool;
 * <pre>
 * String[] inputs = new String[] {
 *     "/project/config/context.xml", "/project/config/dao.xml"
 * };
 * String outputDirectory = "/project/docs/";
 * Configuration cfg = Configuration.getDefaultConfiguration(
 *     inputs,
 *     outputDirectory
 * );
 * new BeanDocEngine(cfg).process();
 * </pre>
 * 
 * The majority of the configuration encapsulated by this class relates to the graphing
 * output ability.  You will need to download a version of 
 * <a href="http://www.graphviz.org">GraphViz</a> for your platform in order to take advantage 
 * of graphing output.  This is highly recommended as graphing is an excellent method of 
 * documenting application contexts.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class Configuration {
    
    public static final String DOTEXE_SYSTEM_PROPERTY = "dotexe.location";
    
    private static final String DEFAULT_CSS_FILE = "context.css";
    
    // INTERNAL DEFAULTS
    private static String javaVersion = System.getProperty("java.specification.version");
    
    private String defaultFillColour = "#cfcccc";
    
    private Transformer transformer = new XslTransformer();
    

    // DEFAULTS - can be configured
    private String title = "Application Context";
    
    private String dotExe = System.getProperty(DOTEXE_SYSTEM_PROPERTY, null);
    
    private String graphOutputType = "png";
    
    private boolean removeDotFiles = true;
    
    private String graphFontName = "helvetica";
    
    private int graphFontSize = 10;
    
    private String graphRatio = "auto";
    
    private float graphXSize = -1f;
    
    private float graphYSize = -1f;
    
    private String graphBeanShape = "box";
    
    private char graphLabelLocation = 't';
    
    private String[] contextCssUrls = { DEFAULT_CSS_FILE };

    private boolean validateFiles = true;

    // LISTS / MAPS    
    private Map beanColours = new HashMap();
    
    private SortedMap javaDocLocations = new TreeMap(
	    new Comparator() {
	        public int compare(Object arg0, Object arg1) {
	            return ((String) arg1).length() - ((String) arg0).length();
	        }                
	    }
	);
    
    private List ignoreBeans = new LinkedList();
    
    
    // SUPPLIED ON CONSTRUCTION
    private File outputDir;
    
    private Resource[] inputFiles;
    

    /* -----------------------------------------------------------------------
     * construction
     * ----------------------------------------------------------------------- */
     
    /**
     * Static factory method that returns a default Configuration object with some
     * additional well-know JavaDoc locations (shown below).  Graphing is enabled
     * by default and a default colour scheme is specified for based on bean names
     * or id's.
     * <p>
     * To specify your own values, or to not use any at all, just create a new 
     * Configuration instead.
     * 
     * <h3>JavaDoc Locations</h3>
     * <table border="1" cellpadding="3">
     *   <tr><th>package starts with</th><th>URL Prefix</th></tr>
     *   <tr><td>java.</td><td>http://java.sun.com/j2se/[JAVA VERSION]/docs/api/</td></tr>
     *   <tr><td>javax.</td><td>http://java.sun.com/j2se/[JAVA VERSION]/docs/api/</td></tr>
     *   <tr><td>org.springframework.</td><td>http://www.springframework.org/docs/api/</td></tr>
     * </table>
     * <p>
     * <h3>Colour Schemes</h3>
     * <table border="1" cellpadding="3">
     *   <tr><th>BeanName</th><th>Colour (RGB HEX value)</th></tr>
     *   <tr><td>*Dao</td><td>#80cc80</td></tr>
     *   <tr><td>*DataSource</td><td>#cceecc</td></tr>
     *   <tr><td>*Interceptor, *Controller, *Filter, *HandlerMapping</td><td>#cceeee</td></tr>
     *   <tr><td>*Validator</td><td>#eecc80</td></tr>
     *   <tr><td>*</td><td>#cfcccc</td></tr>
     * </table>
     * <p>
     * The following table is a quick guide to default values for most of the other
     * interesting configuration properties when <code>getDefaultConfiguration()</code>
     * is used.
     * 
     * <h3>Defaults</h3>
     * <table border="1" cellpadding="3">
     *   <tr><th>property</th><th>value</th></tr>
     *   <tr><td>doGraphOutput</td><td>true</td></tr>
     *   <tr><td>dotExe</td><td>null - specify this in all cases</td></tr>
     *   <tr><td>graphBeanShape</td><td>box</td></tr>
     *   <tr><td>graphFontName</td><td>helvetica</td></tr>
     *   <tr><td>graphFontSize</td><td>10</td></tr>
     *   <tr><td>graphLabelLocation</td><td>t</td></tr>
     *   <tr><td>graphOutputType</td><td>png</td></tr>
     *   <tr><td>graphRatio</td><td>auto</td></tr>
     * </table>
     * 
     * @param inputFiles an Array of xml <code>Resource</code>s that make up the combined application 
     *      context or bean factory.  Missing files will be ignored when read in.  Resources can be
     *      of any type supported by Spring - typically classpath or file resources.  Usual protocol
     *      handlers (<code>classpath:</code>, <code>file:</code>) will work.
     * @param outputDir the directory that the HTML and (optional) graphs will be written to.
     * @throws IOException if the outputDir does not exist or is not writable.
     * 
     * @see #addJavaDocLocation
     * @see #addBeanColours
     */
    public static Configuration getDefaultConfiguration(Resource[] inputFiles, File outputDir) throws IOException {
        Configuration cfg = new Configuration(inputFiles, outputDir);
        
        // some well-know, overridable javadoc locations for class links
        cfg.addJavaDocLocation(
            "java.",
            "http://java.sun.com/j2se/" + javaVersion + "/docs/api/");
        cfg.addJavaDocLocation(
            "javax.",
            "http://java.sun.com/j2se/" + javaVersion + "/docs/api/");
        cfg.addJavaDocLocation(
            "org.springframework.",
            "http://www.springframework.org/docs/api/");
        cfg.addJavaDocLocation(
            "org.springframework.samples.",
            null);
            
            
        cfg.addBeanColours(
            "*Dao",
            "#80cc80");
        cfg.addBeanColours(
            "*DataSource",
            "#cceecc");
        cfg.addBeanColours(
            "*Interceptor",
            "#cceeee");
        cfg.addBeanColours(
            "*Controller",
            "#cceeee");
        cfg.addBeanColours(
            "*HandlerMapping",
            "#cceeee");
        cfg.addBeanColours(
            "*Filter",
            "#cceeee");
        cfg.addBeanColours(
            "*Validator",
            "#eecc80");
        
        return cfg;        
    }
    
    /**
     * Convenience factory method that takes the names of input files and the output directory as
     * Strings.
     * 
     * @param inputFileNames an array of resource references pointing to input files to be processed
     * @param outputDirName the name of a writeable directory for storing documentation
     * @return a valid Configuration object that can be processed by a BeanDocEngine
     * @throws IOException if inputFileNames cannot be resolved or loaded, or if outputDirName does
     *      not point to a writable directory on disk
     * @see #getDefaultConfiguration(Resource[], File)
     */
    public static Configuration getDefaultConfiguration(String[] inputFileNames, String outputDirName) throws IOException {
        Resource[] inputFiles = getResources(inputFileNames);
        File outputDir = new File(outputDirName);
        return getDefaultConfiguration(inputFiles, outputDir);
    }
    
    /**
     * Default constructor for bean doc configurations.  The object is immediately ready for use with
     * vanilla options but is less functional than an object obtained through one of the static
     * factory methods.  See the relevant mutators for default property values.
     * <p>
     * Construct your Configuration using this method if you wish to specify different colours, colour
     * mapping schemes (ie using package names), JavaDoc locations or if you don't wish to use 
     * graphing at all.
     * 
     * @param inputFiles an Array of xml <code>Resources</code>s that make up the combined application 
     *      context or bean factory.  Missing files will be ignored when read in.  Resources can be
     *      of any type supported by Spring - typically classpath or file resources.  Usual protocol
     *      handlers (<code>classpath:</code>, <code>file:</code>) will work.
     * @param outputDir the directory that the HTML and (optional) graphs will be written to. 
     * @throws IOException if the outputDir does not exist or is not writable.
     * @see #getDefaultConfiguration
     */
    public Configuration(Resource[] inputFiles, File outputDir) throws IOException {
        init(inputFiles, outputDir);
    }
    
    /**
     * Default constructor for bean doc configurations.  The object is immediately ready for use with
     * vanilla options but is less functional than an object obtained through one of the static
     * factory methods.  See the relevant mutators for default property values.
     * <p>
     * Construct your Configuration using this method if you wish to specify different colours, colour
     * mapping schemes (ie using package names), JavaDoc locations or if you don't wish to use 
     * graphing at all.
     * 
     * @param inputFiles an Array of <code>String</code>s resolveable as XML files that make up the 
     *      combined application context or bean factory.  Missing files will be ignored when read in.  
     *      Resources can be of any type supported by Spring - typically classpath or file resources.  
     *      Usual protocol handlers (<code>classpath:</code>, <code>file:</code>) will work.
     * @param outputDir the name of the directory that the HTML and (optional) graphs will be 
     *      written to. 
     * @throws IOException if the outputDir does not exist or is not writable.
     * @see #getDefaultConfiguration(String[], String)
     */
    public Configuration(String[] inputFileNames, String outputDirName) throws IOException {
        Resource[] inputFiles = getResources(inputFileNames);
        File outputDir = new File(outputDirName);
        
        init(inputFiles, outputDir);
    }

    /**
     * Convert string values to actual resources
     * 
     * @param inputFileNames
     * @return
     */
    private static Resource[] getResources(String[] inputFileNames) throws IOException {
        // resolve resources assuming Files as the default (rather than classpath resources)
        ResourcePatternResolver resolver = 
            new PathMatchingResourcePatternResolver(new DefaultFileSystemResourceLoader());
        List allResources = new ArrayList();
        
        // each input location could resolve to multiple Resources..
        for (int i = 0; i < inputFileNames.length; i++) {
            Resource[] resources = resolver.getResources(inputFileNames[i]);
            allResources.addAll(Arrays.asList(resources));
        }

        File outputDir = new File(inputFileNames[inputFileNames.length - 1]);
        Resource[] inputFiles = (Resource[]) 
            allResources.toArray(new Resource[allResources.size()]);
            
        return inputFiles;
    }

    /**
     * @param inputFiles
     * @param outputDir
     */
    private void init(Resource[] inputFiles, File outputDir) throws IOException {
        this.inputFiles = inputFiles;
        this.outputDir = outputDir;

        if (!outputDir.canWrite() || !outputDir.isDirectory())
            throw new IOException(
                "Unable to find or write to output directory [" + outputDir.getAbsolutePath() + "]"
            );
    }



    /* -----------------------------------------------------------------------
     * bean properties / convenience mutators
     * ----------------------------------------------------------------------- */
    
    /**
     * Set the location of the 'dot' executable file from the Graphviz installation.  This file
     * will be called with appropriate parameters if graphing output is required using a 
     * <code>Runtime.getRuntime().exec(...)</code> call.  If this value is not set, graphing
     * output will be disabled.
     * 
     * @param dotExe the platform dependent location of the binary, ie "/usr/local/bin/dot" or
     * "C:/graphviz/dot.exe"
     */
    public void setDotExe(String dotExe) {
        this.dotExe = dotExe;
    }

    /**
     * The shape to draw bean nodes in.  Ignored if setDoGraphOutput is <code>false</code>
     * Common options are;
     * <ul>
     *   <li>box</li>
     *   <li>circle</li>
     *   <li>ellipse</li>
     * </ul>
     * See the Graphviz documentation for a full list of available shapes.  The default
     * is "box"
     * 
     * @param shape the shape for bean nodes in generated graphs.
     */
    public void setGraphBeanShape(String shape) {
        graphBeanShape = shape;
    }

    /**
     * Set the font used in bean labels in graphing output.
     * 
     * @param font the font to use, default is "helvetica" which should work
     * on most platforms
     */
    public void setGraphFontName(String font) {
        graphFontName = font;
    }

    /**
     * Set the font size used in bean labels in graphing output.
     * 
     * @param fontSize the font point size, default is 10
     */
    public void setGraphFontSize(int fontSize) {
        graphFontSize = fontSize;
    }

    /**
     * Determines whether graph titles will appear at the top or bottom
     * of the graph.  Use 't' or 'b' as required
     * 
     * @param labelLocation a char representing Top ('t') or Bottom ('b').  Default is 't'
     */
    public void setGraphLabelLocation(char labelLocation) {
        graphLabelLocation = labelLocation;
    }

    /**
     * Determines the format of the graphing output.  Some options are;
     * <ul>
     *   <li><b>png</b> (Portable Network Graphics)</li>
     *   <li><b>gif</b> (Graphics Interchange Format)</li>
     *   <li><b>jpg</b> (JPEG)</li>
     *   <li><b>svg</b> (Scalable Vector Graphics)</li>
     * </ul>
     * 
     * @param graphType the output format for graphs.  Default is <b>png</b> which
     * is a very efficient format in terms of file size and highly recommended over
     * gif and jpg if your viewer supports it.  Most modern browsers can display 
     * PNG files.
     */
    public void setGraphOutputType(String graphType) {
        graphOutputType = graphType;
    }

    /**
     * Sets the graph ratio.  May require some experimentation to get the most
     * suitable output depending on the content of your bean definition files.
     * <p>
     * <h3>Adapted from the GraphViz documentation:</h3>
     * ratio affects layout size. There are a number of cases, depending on the
     * settings of size and ratio;
     * <ol>
     * <li>ratio was not set (null). If the drawing already fits within the given size,
     * then nothing happens. Otherwise, the drawing is reduced uniformly enough to
     * make the critical dimension fit.</li>
     * 
     * <li>If ratio was set, there are four subcases;
     *  <ul>
     *    <li>If ratio=x where x is a floating point number, then the drawing
     * is scaled up in one dimension to achieve the requested ratio expressed as drawing
     * height/width. For example, ratio=2.0 makes the drawing twice as high as it
     * is wide. Then the layout is scaled using size as in 1.</li>
     *     <li>If ratio="auto" and the page attribute is set and the graph cannot
     * be drawn on a single page, then size is ignored and dot computes an <i>ideal</i> size.
     * In particular, the size in a given dimension will be the smallest integral multiple
     * of the page size in that dimension which is at least half the current size. The two
     * dimensions are then scaled independently to the new size</li>
     *   </ul>
     * </li>
     * </ol>
     * 
     * @param ratio the ratio for graph output, default is "auto"
     * @see #setGraphXSize
     * @see #setGraphYSize
     */
    public void setGraphRatio(String ratio) {
        graphRatio = ratio;
    }

    /**
     * The maximum length of the x-axis of the graph in inches.  Useful to set this value
     * if the graph has to be printed to ensure it will fit on the target paper size.  Used
     * by GraphViz in conjunction with the ratio setting to determine final layout and
     * positioning of nodes on the graph.  If the corresponding value for the y-axis of
     * the graph is not set, this value is ignored by the <code>BeanDocEngine</code>.
     * 
     * @param x a float value specifying the length of the x-axis of the graph
     */
    public void setGraphXSize(float x) {
        graphXSize = x;
    }

    /**
     * The maximum length of the y-axis of the graph in inches.  Useful to set this value
     * if the graph has to be printed to ensure it will fit on the target paper size.  Used
     * by GraphViz in conjunction with the ratio setting to determine final layout and
     * positioning of nodes on the graph.  If the corresponding value for the x-axis of
     * the graph is not set, this value is ignored by the <code>BeanDocEngine</code>.
     * 
     * @param y a float value specifying the length of the y-axis of the graph
     */
    public void setGraphYSize(float y) {
        graphYSize = y;
    }
    
    /**
     * Sets the fill colour used for beans on graphs and keyed in the HTML documentation if no other
     * colour is specified through pattern matching.
     * 
     * @param colour a <code>String</code> in the format "#AABBCC" - a standard hex triplet for the 
     *      RGB values
     */
    public void setDefaultFillColour(String colour) {
        defaultFillColour = colour;
    }

    /**
     * A <code>Map</code> keyed by bean names/ids or classnames that hold colour attributes
     * used to fill graph nodes or key the HTML output. The preferred way to modify colours is 
     * through the {@link #addBeanColours} convenience method.
     * 
     * @param colours a <code>Map</code> of node fill colours for graph output.  Also used to key
     *      the HTML documentation.
     * @see #addBeanColours
     * @see #getDefaultConfiguration(String[], String)
     * @see #getDefaultConfiguration(Resource[], File)
     */
    public void setBeanColours(Map map) {
        beanColours = map;
    }
    
    /**
     * Add a fill colour to the beans on a graph whose name, id's or classname match 
     * the supplied string.  Can be used to nicely highlight application layers or other
     * concepts within your bean factories and application contexts if you follow
     * a disciplined bean naming convention.
     * 
     * @param pattern a String representing a pattern to match.  The pattern can be prefixed or
     *      suffixed with a wildcard (*) but does not use RegEx matching.  A null value will
     *      be ignored
     * @param colour the colour as an RGB HEX triplet to fill the bean with.  May not be null
     * @throws BeanDocException if the colour value is null and the pattern is valid
     */
    public void addBeanColours(String pattern, String colour) {
        if (pattern == null) return;
        if (colour == null)
            throw new BeanDocException("Cannot have a [null] colour");
        beanColours.put(pattern, colour);
    }

    /**
     * A <code>List</code> of patterns representing bean names/ids or classnames that should
     * be excluded from the graphing output.  Does not affect the HTML documentation which 
     * will document all beans regardless of this list.  The preferred way to modify this list is 
     * through the {@link #addIgnoreBeans} convenience method.
     * 
     * @param list a <code>List</code> of patterns of bean names to be excluded from graphs
     * @see #addIgnoreBeans
     */
    public void setIgnoreBeans(List list) {
        ignoreBeans = list;
    }    

    /**
     * Add a naming pattern of bean id's or bean names or classnames that should not be displayed
     * on graphing output.  Some beans (such as PropertyConfigurers and MessageSources)
     * are auxilliary and you may wish to exclude them from context graphs to keep the graphs
     * focused.  All beans will still be documented in the HTML files regardless of these
     * values.
     * <p>
     * This method may be called any number of times to add different patterns to the
     * list of ignored beans.  Pattern may not be null (such a value will be ignored).
     * 
     * @param pattern a String representing a pattern to match.  The pattern can be prefixed or
     *      suffixed with a wildcard (*) but does not use RegEx matching.  May not be null
     */
    public void addIgnoreBeans(String pattern) {
        if (pattern != null) ignoreBeans.add(pattern);
    }

    /**
     * A <code>SortedMap</code> keyed by package prefixes that point to URI's (local, remote,
     * absolute or relative) of JavaDoc locations.  Used to link classnames to their javadoc
     * locations in the HTML output.  The preferred way to modify this list is 
     * through the {@link #addJavaDocLocation} convenience method.
     * 
     * @param map a <code>SortedMap</code> of javadoc locations keyed by package name prefixes.
     * @see #addJavaDocLocation
     * @see #getDefaultConfiguration(String[], String)
     * @see #getDefaultConfiguration(Resource[], File)
     */
    public void setJavaDocLocations(SortedMap map) {
        javaDocLocations = map;
    }

    /**
     * Add a JavaDoc location used in the output documents to link classnames to their
     * JavaDoc pages.  Specify a package prefix followed by a URL.  Classes whose
     * package names start with that prefix (using <code>String.startsWith()</code>)
     * will be linked to this location.
     * <p>
     * For example, adding a location where <pre>
     *    classPrefix = com.foo.bar.
     *    url = http://our.server.com/doc/api/
     * </pre> will cause a class in this package named <code>Example.class</code> to be 
     * linked with an HREF of;
     * <code>http://our.server.com/doc/api/com/foo/bar/Example.html</code>
     * <p>
     * 
     * Note that the package names are used as keys in a <code>SortedMap</code> which 
     * is sorted in reverse order by key length.  This means you can add package keys 
     * in any order you like, and class names will be evaluated against the longest 
     * package names first down to the shortest.  This enables you for example to
     * specify that <code>org.springframework.samples.</code> has a different document
     * location than <code>org.springframework.</code>
     *  
     * @param classPrefix the prefix, of arbitrary length, of the package and class name to 
     *      match against.  May not be null (will be ignored)
     * @param url the root of the API documentation
     */
    public void addJavaDocLocation(String classPrefix, String url) {
        if (classPrefix == null) return;
        javaDocLocations.put(classPrefix, url);
    }

    /**
     * If graphing output is required, a series of intermediate files (.dot files) are
     * created which is what GraphViz uses to actually generate the graphs.  Usually
     * these wil not be needed after the graphs are generated and so by default are
     * discarded.  If you need to keep them for any reason, set this value to <code>false</code>
     * 
     * @param removeDotFiles set to false to prevent intermediate .dot files being discarded.  True
     *      by default.
     */
    public void setRemoveDotFiles(boolean removeDotFiles) {
        this.removeDotFiles = removeDotFiles;
    }

    /**
     * Sets the page titles for the documentation output.  Graph titles are taken from 
     * the individual file names used to generate the graphs.
     * 
     * @param title the page title used in documentation output
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Optional location of a CSS file that can be used to skin the beandoc output.  By default,
     * a file will be supplied and copied into the output directory which the HTML file will
     * reference.  If you set a value here, this file will not be copied to the output directory
     * and your reference will be used instead.
     * 
     * @param contextCssUrls an array of locations (absolute or relative to your output directory) 
     *      that the CSS files can be found which is used to skin the beandoc output.
     */
    public void setContextCssUrls(String[] contextCssUrls) {
        this.contextCssUrls = contextCssUrls;
    }

    /**
     * Set to false to prevent the XML parser validating input files against a DTD.
     * 
     * @param validateFiles set to true to enable validation, false otherwise.  True by default.
     */
    public void setValidateFiles(boolean validateFiles) {
        this.validateFiles = validateFiles;
    }
    
    /**
     * For ultimate control over the output process, specify your own <code>Transformer</code>
     * instance here.  A default XSL based version is used if no other is specified.
     * 
     * @param transformer the <code>Transformer</code> to use
     */
    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Location of the GraphViz 'dot' executable program on the local machine
     * 
     * @return the platform-dependent location of the GraphViz 'dot' executable file
     */
    public String getDotExe() {
        return dotExe;
    }

    /**
     * Default shape used to describe bean nodes on the graph.  See {@link #setGraphBeanShape}
     * for some of the options
     * 
     * @return the shape GraphViz should use to display beans
     * @see #setGraphBeanShape
     */
    public String getGraphBeanShape() {
        return graphBeanShape;
    }

    /**
     * The font name used on the graph nodes.  Must be a known font on the fontpath for the
     * local machine.  Basically this will pretty much work for any font you have installed
     * on Win32 platforms, but may give unexpected results on Unix/Linux type platforms
     * depending on your font server setup.
     * <p>
     * If you want to use a font that GraphViz can't find on your system, set a <code>FONTPATH</code>
     * environment variable pointing to the location of your font directories.
     * 
     * @return the font name used for bean labels on the graph
     */
    public String getGraphFontName() {
        return graphFontName;
    }

    /**
     * Font size (pt) used for node labels on graph output
     * 
     * @return the font size used for bean labels on the graph
     */
    public int getGraphFontSize() {
        return graphFontSize;
    }

    /**
     * Label position denoting whether graph labels appear at the top or bottom of the
     * graph.
     * 
     * @return 't' or 'b' to denote top or bottom respectively
     */
    public char getGraphLabelLocation() {
        return graphLabelLocation;
    }

    /**
     * The type of output that the GraphViz 'dot' program should create from the
     * intermediate .dot files.  Default is PNG.  See {@link #setGraphOutputType} for 
     * some of the optional formats supported
     * 
     * @return the type of graph output that will be generated from the 
     * 		.dot files
     * @see #setGraphOutputType
     */
    public String getGraphOutputType() {
        return graphOutputType;
    }

    /**
     * A value denoting ratio of x and y axes on the graoh output.  Used in conjunction
     * with {@link #setGraphXSize} and {@link #setGraphYSize} to determine final size and
     * layout.
     *     
     * @return the ratio to use for the graph
     * @see #getGraphXSize
     * @see #getGraphYSize
     */
    public String getGraphRatio() {
        return graphRatio;
    }

    /**
     * The maximum length of the x-axis of the graph in inches.  Useful to set this value
     * if the graph has to be printed to ensure it will fit on the target paper size.  Used
     * by GraphViz in conjunction with the ratio setting to determine final layout and
     * positioning of nodes on the graph.  If the corresponding value for the y-axis of
     * the graph is not set, this value is ignored by the <code>BeanDocEngine</code>.
     * 
     * @return a float value for the length of the x-axis of a graph, or -1 if the value is
     *      not set.
     * @see #setGraphXSize
     * @see #getGraphYSize
     * @see #setGraphYSize
     */
    public float getGraphXSize() {
        return graphXSize;
    }

    /**
     * The maximum length of the y-axis of the graph in inches.  Useful to set this value
     * if the graph has to be printed to ensure it will fit on the target paper size.  Used
     * by GraphViz in conjunction with the ratio setting to determine final layout and
     * positioning of nodes on the graph.  If the corresponding value for the x-axis of
     * the graph is not set, this value is ignored by the <code>BeanDocEngine</code>.
     * 
     * @return a float value for the length of the y-axis of a graph, or -1 if the value is
     *      not set.
     * @see #setGraphYSize
     * @see #getGraphXSize
     * @see #setGraphXSize
     */
    public float getGraphYSize() {
        return graphYSize;
    }

    /**
     * A <code>SortedMap</code> keyed by package prefixes that point to URI's (local, remote,
     * absolute or relative) of JavaDoc locations.  Used to link classnames to their javadoc
     * locations in the HTML output.
     * <p>
     * The returned underlying <code>SortedMap</code> is modifiable and will, if modified, affect
     * subsequent calls to the <code>BeanDocEngine</code>'s <code>process()</code> method if
     * you are using the tool programmatically.  The preferred way to modify this list is 
     * through the {@link #addJavaDocLocation} convenience method.
     * 
     * @return a <code>SortedMap</code> of javadoc locations keyed by package name prefixes.
     * @see #addJavaDocLocation
     * @see #getJavaDocForClassName
     * @see #getDefaultConfiguration(String[], String)
     * @see #getDefaultConfiguration(Resource[], File)
     */
    public SortedMap getJavaDocLocations() {
        return javaDocLocations;
    }
    
	/**
	 * Queries the internal Map of locations and returns the first
	 * matching JavaDoc location that meets this className prefix.
	 * 
	 * @param className the package prefix pattern that a JavaDoc location is specified
     *      for.
	 * @return javaDoc location (URL) or null if no location is
	 * 		specified for the given class name
	 * @see #addJavaDocLocation
	 */
	public String getJavaDocForClassName(String className) {
		for (Iterator i = javaDocLocations.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			if (className.startsWith(key)) {
				String class2url = StringUtils.replace(className, ".", "/") + ".html";
				String jdoc = (String) javaDocLocations.get(key);
                if (jdoc == null) return null;
				if (!jdoc.endsWith("/")) jdoc += "/";
				return jdoc + class2url;
			}
		}
		
		return null;
	}    
    
    /**
     * Returns a <code>Map</code> keyed by bean name or class name that is used to 
     * determine the fill colour for bean descriptions in the HTML output andf on the
     * graphing output.
     * 
     * @return the Map used to describe fill colours of beans, keyed by bean name
     *      or classname
     * @see #getColourForBean
     */
    public Map getBeanColours() {
        return beanColours;
    }
	
	/**
	 * Return the correct colour to describe this bean based on prior
	 * configuration settings.  Bean names (or id's) override classname
	 * matches where a conflicting result would otherwise occur.
	 * 
	 * @param idOrName the id or name attribute of the bean you wish to get the
     *      fill colour for
	 * @param className the fully qualified classname of the bean 
	 * @return the colour (as an RGB triplet prefixed with a # symbol) that 
	 * 		should be used to describe the bean with paramters supplied.
	 */
	public String getColourForBean(String idOrName, String className) {
		// check names first
		String colour = getColourMatch(idOrName);
		if (colour != null) return colour;
		
		// try classnames
		colour = getColourMatch(className);
		if (colour != null) return colour;
		
		// no match
		return defaultFillColour;
	}

    /**
	 * @param pattern
	 * @return the first colour from the Map that matches the pattern
	 */
	private String getColourMatch(String pattern) {
		if (pattern == null) return null;
		
		for (Iterator i = beanColours.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			if (
				(key.startsWith("*") && pattern.endsWith(key.substring(1)))
				||
				(key.endsWith("*") && pattern.startsWith(key.substring(0, key.length() - 1)))
				||
				(key.equals(pattern))
			)
				return (String) beanColours.get(key);
		}
		return null;
	}

    /**
     * A <code>List</code> of patterns representing bean names/ids or classnames that should
     * be excluded from the graphing output.  Does not affect the HTML documentation which 
     * will document all beans regardless of this list.
     * <p>
     * The returned underlying <code>List</code> is modifiable and will, if modified, affect
     * subsequent calls to the <code>BeanDocEngine</code>'s <code>process()</code> method if
     * you are using the tool programmatically.  The preferred way to modify this list is 
     * through the {@link #addIgnoreBeans} convenience method.
     * 
     * @return a <code>List</code> of patterns of bean names to be excluded from graphs
     * @see #addIgnoreBeans
     * @see #isBeanIgnored
     */
    public List getIgnoreBeans() {
        return ignoreBeans;
    }

	/**
     * Patterns of bean or classnames can be used to indicate that some beans should be
     * excluded from the graphing output.  All beans will still be documented in the beandoc
     * output by default.
     * 
     * @return true if the bean should be ignored on graphing output, false
     *      otherwise.
     * @see #addIgnoreBeans
     */
    public boolean isBeanIgnored(String idOrName, String className) {
        
        String[] ignored = (String[]) ignoreBeans.toArray(new String[ignoreBeans.size()]);
        for (int i = 0; i < ignored.length; i++) {
            String key = ignored[i];
            if (
                (key.startsWith("*") && 
                    ((idOrName != null && idOrName.endsWith(key.substring(1))) || 
                    (className != null && className.endsWith(key.substring(1)))))
                ||
                (key.endsWith("*") && 
                    ((idOrName != null && idOrName.startsWith(key.substring(0, key.length() - 1))) || 
                    (className != null && className.startsWith(key.substring(0, key.length() - 1)))))
                ||
                (key.equals(idOrName) || key.equals(className))
            )
                return true;
        }
        return false;
    }

    /**
     * The <code>File</code> representing an output directory that the beandoc tool will use for
     * outputting HTML and graph images.
     * 
     * @return the directory that output will be written to.
     */
    public File getOutputDir() {
        return outputDir;
    }

    /**
     * Should intermediate .dot files be removed?
     * 
     * @return true if intermediate .dot files will be removed after graphing output has
     *      completed, or false if they will be kept in the output directory.  True by default.
     */
    public boolean isRemoveDotFiles() {
        return removeDotFiles;
    }

    /**
     * Title of the application context.  For example "JPetStore Application Context".  Will
     * be used as the <code>&lt;title&gt;</code> tag in HTML documentation.
     * 
     * @return the page title used in the documentation 
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Fill colour used for beans on graphs and keyed in the HTML documentation if no other
     * colour is specified through pattern matching.
     * 
     * @return the default fill colour for beans that don't match any pattern used for 
     *      determining fill colours
     * @see #addBeanColours
     */
    public String getDefaultFillColour() {
        return defaultFillColour;
    }

    /**
     * The input resources (files, classpath resources) used as the actual inputs to the 
     * beandoc tool.  Typical non-trivial application contexts will be made up of two or
     * more resources.
     * 
     * @return the array of input resources that make up the application context being
     *      documented.
     */
    public Resource[] getInputFiles() {
        return inputFiles;
    }

    /**
     * An array of URI's (absolute or relative) that will be added toi the HTML output as 
     * <code>&lt;link rel="stylesheet"&gt;</code> tags in the header.  If none are provided
     * then a default CSS file is copied to the output directory and linked in the HTML
     * header.
     * 
     * @return the String array representing absolute or relative references to CSS files
     *      used to skin the beandoc output.
     */
    public String[] getContextCssUrls() {
        return contextCssUrls;
    }

    /**
     * Input files can optionally be validated against a DTD in the XML file.  True by
     * default.
     * 
     * @return true if input files should be validated against a DTD, false otherwise.
     */
    public boolean isValidateFiles() {
        return validateFiles;
    }

    /**
     * Get the <code>Transformer</code> instance that should be used to actually generate
     * the documentation and .dot files (where applicable) from the input DOM trees.  By
     * default an instance of {@link XslTransformer} is created which has its own
     * XSLT stylesheets for transforming output.  In order to use a different strategy for
     * ultimate control over the beandoc output, use {@link #setTransformer}
     * 
     * @return the Transformer instance
     * @see #setTransformer
     */
    public Transformer getTransformer() {
        return transformer;
    }

    /**
     * Returns a representation of all configuration options.  Can be large and takes some
     * processing - use judiciously.
     * 
     * @return a String representation of the configuration
     */
    public String toString() {
        StringBuffer sb = new StringBuffer(512);
        sb.append("\ntitle: \t\t\t\t\t" + title + "\n")
            .append("validate input: \t\t" + validateFiles + "\n")
            .append("input resources: \t\t" + StringUtils.arrayToCommaDelimitedString(inputFiles) + "\n")
            .append("ouptut directory: \t\t" + outputDir.getAbsolutePath() + "\n")
            .append("transformer: \t\t\t" + transformer.getClass().getName() + "\n")
            .append("javadoc locations: \t\t" + javaDocLocations + "\n")
            .append("default colour: \t\t" + defaultFillColour + "\n")
            .append("bean colours: \t\t\t" + beanColours + "\n")
            .append("css files: \t\t\t\t" + StringUtils.arrayToCommaDelimitedString(contextCssUrls) + "\n");
            
        // graph output config
        if (dotExe != null) {
            sb.append("graphviz executable: \t" + dotExe + "\n")
                .append("node shape: \t\t\t" + graphBeanShape + "\n")
                .append("font name: \t\t\t\t" + graphFontName + "\n")
                .append("font size: \t\t\t\t" + graphFontSize + "\n")
                .append("label location: \t\t" + graphLabelLocation + "\n")
                .append("output type: \t\t\t" + graphOutputType + "\n")
                .append("ratio: \t\t\t\t\t" + graphRatio + "\n")
                .append("size (x, y): \t\t\t" + graphXSize + ", " + graphYSize + "\n")
                .append("ignored beans: \t\t\t" + StringUtils.collectionToCommaDelimitedString(ignoreBeans) + "\n")
                .append("remove .dot files: \t\t" + removeDotFiles + "\n")
                ;
        }
        else 
            sb.append("graphing output [disabled]");
            
        return sb.toString();
    }

}
