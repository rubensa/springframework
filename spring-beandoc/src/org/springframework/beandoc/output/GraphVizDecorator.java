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

package org.springframework.beandoc.output;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Element;
import org.springframework.beandoc.BeanDocException;
import org.springframework.beandoc.util.BeanDocUtils;


/**
 * GraphVizDecorator
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class GraphVizDecorator extends SimpleDecorator {

    protected static final String ATTRIBUTE_GRAPH_TYPE = "beandocGraphType";
  
    protected static final String ATTRIBUTE_GRAPH_FONTNAME = "beandocGraphFontName";
	
    protected static final String ATTRIBUTE_GRAPH_FONTSIZE = "beandocGraphFontSize";
	
    protected static final String ATTRIBUTE_GRAPH_SIZE = "beandocGraphSize";
  
    protected static final String ATTRIBUTE_GRAPH_RATIO = "beandocGraphRatio";
	
    protected static final String ATTRIBUTE_GRAPH_BEANSHAPE = "beandocGraphBeanShape";
	
    protected static final String ATTRIBUTE_GRAPH_LABELLOCATION = "beandocGraphLabelLocation";
    
    protected static final String ATTRIBUTE_GRAPH_CONSOLIDATED = "beandocConsolidatedImage";
	
    protected static final String ATTRIBUTE_GRAPH_IGNORE = "beandocGraphIgnore";
    
    protected static final String ATTRIBUTE_GRAPH_RANK = "beandocRank";
	
    protected static final String ATTRIBUTE_COLOUR = "beandocFillColour";
    
    private String fontName = "helvetica";
    
    private int fontSize = 10;
    
    private String ratio = "auto";
    
    private float graphXSize = -1f;
    
    private float graphYSize = -1f;
    
    private String beanShape = "box";
    
    private char labelLocation = 't';
    
    private String defaultFillColour = "#cfcccc";

    private String outputType = "png";
    
    private Map beanColours = new HashMap();
    
    private Pattern[] beanColoursPatterns;

    private List ignoreBeans = new LinkedList();
    
    private Pattern[] ignoreBeansPatterns;
    
    private List rankBeans = new LinkedList();
    
    private Pattern[] rankBeansPatterns;
    

    /**
     * 
     */
    public GraphVizDecorator() {
        addBeanColours(".*Dao", "#80cc80");
        addBeanColours(".*DataSource", "#cceecc");
        addBeanColours(".*Interceptor", "#cceeee");
        addBeanColours(".*Controller", "#cceeee");
        addBeanColours(".*HandlerMapping", "#cceeee");
        addBeanColours(".*Filter", "#cceeee");
        addBeanColours(".*Validator", "#eecc80");
    }
    
    /**
     * should be called after all bean properties are set.  Converts String
     * patterns to Regex patterns.
     */
    void init() {
        rankBeansPatterns = BeanDocUtils.convertStringsToPatterns(rankBeans);
        ignoreBeansPatterns = BeanDocUtils.convertStringsToPatterns(ignoreBeans);
        beanColoursPatterns = BeanDocUtils.convertStringsToPatterns(beanColours.keySet());
    }
    
    /**
     * Decorates root element with graph type attributes and each bean element
     * as required with colour information.
     * 
     * @see org.springframework.beandoc.output.SimpleDecorator#decorateElement(org.jdom.Element)
     */
    protected void decorateElement(Element element) {
        if (element.isRootElement()) {
            element.setAttribute(ATTRIBUTE_GRAPH_FONTNAME, fontName);
            element.setAttribute(ATTRIBUTE_GRAPH_TYPE, outputType);
			element.setAttribute(ATTRIBUTE_GRAPH_FONTSIZE, String.valueOf(fontSize));
			if (graphXSize > -1 && graphYSize > -1)
			    element.setAttribute(ATTRIBUTE_GRAPH_SIZE, graphXSize + ", " + graphYSize);		
			element.setAttribute(ATTRIBUTE_GRAPH_RATIO, ratio);		
			element.setAttribute(ATTRIBUTE_GRAPH_BEANSHAPE, beanShape);
			element.setAttribute(ATTRIBUTE_GRAPH_LABELLOCATION, String.valueOf(labelLocation));
            element.setAttribute(ATTRIBUTE_GRAPH_CONSOLIDATED, "consolidated." + outputType);
        }
        
        
        if ("bean".equals(element.getName())) {
			String id = element.getAttributeValue(Tags.ATTRIBUTE_ID);
			String name = element.getAttributeValue(Tags.ATTRIBUTE_NAME);
			if (name == null) name = "anon";
			String idOrName = (id == null) ? name : id;
			String className = element.getAttributeValue(Tags.ATTRIBUTE_CLASSNAME);
			
			// patterns of beans to be coloured
			element.setAttribute(ATTRIBUTE_COLOUR, getDefaultFillColour());
			for (int i = 0; i < beanColoursPatterns.length; i++) {
		        Matcher beanMatcher = beanColoursPatterns[i].matcher(idOrName);
		        Matcher classMatcher = beanColoursPatterns[i].matcher(className);
		        String colour;
		        try {
		            if (beanMatcher.matches() || classMatcher.matches()) {
		                colour = (String) beanColours.get(beanColoursPatterns[i].pattern());
		                element.setAttribute(ATTRIBUTE_COLOUR, colour);
		                logger.debug("bean [" + idOrName + "] has colour [" + colour + "]");
		            }
	                
		        } catch (NullPointerException npe) {
		            // no match (!)
		            logger.debug("d)");
		        }
		    }
			
			// patterns of beans to be ignored on graphs
			for (int i = 0; i < ignoreBeansPatterns.length; i++) {
		        Matcher beanMatcher = ignoreBeansPatterns[i].matcher(idOrName);
		        Matcher classMatcher = ignoreBeansPatterns[i].matcher(className);
		        try {
		            if (beanMatcher.matches() || classMatcher.matches()) {		        
		                element.setAttribute(ATTRIBUTE_GRAPH_IGNORE, "true");
		                logger.debug("bean [" + idOrName + "] will be excluded from graphs");		
		            }
		        } catch (NullPointerException npe) {
		            // no match (!)
		        }
		    }
			
			// patterns of beans to be constrained by rank on graphs
			for (int i = 0; i < rankBeansPatterns.length; i++) {
		        Matcher beanMatcher = rankBeansPatterns[i].matcher(idOrName);
		        Matcher classMatcher = rankBeansPatterns[i].matcher(className);
		        try {
		            if (beanMatcher.matches() || classMatcher.matches()) {		        
		                element.setAttribute(ATTRIBUTE_GRAPH_RANK, String.valueOf(i));
		                logger.debug("bean [" + idOrName + 
		                    "] will be constrained to rank with token value [" + i + "]");		
		            }
		        } catch (NullPointerException npe) {
		            // no match (!)
		        }
		    }
			
        }	    
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
     */
    public void setBeanColours(Map colours) {
        beanColours.putAll(colours);
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
    public void setBeanShape(String shape) {
        beanShape = shape;
    }

    /**
     * Set the font used in bean labels in graphing output.
     * 
     * @param font the font to use, default is "helvetica" which should work
     * on most platforms
     */
    public void setFontName(String font) {
        fontName = font;
    }

    /**
     * Set the font size used in bean labels in graphing output.
     * 
     * @param fontSize the font point size, default is 10
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * Determines whether graph titles will appear at the top or bottom
     * of the graph.  Use 't' or 'b' as required
     * 
     * @param labelLocation a char representing Top ('t') or Bottom ('b').  Default is 't'
     */
    public void setLabelLocation(char labelLocation) {
        this.labelLocation = labelLocation;
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
    public void setRatio(String ratio) {
        this.ratio = ratio;
    }

    /**
     * The maximum length of the x-axis of the graph in inches.  Useful to set this value
     * if the graph has to be printed to ensure it will fit on the target paper size.  Used
     * by GraphViz in conjunction with the ratio setting to determine final layout and
     * positioning of nodes on the graph.  If the corresponding value for the y-axis of
     * the graph is not set, this value is ignored.
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
     * the graph is not set, this value is ignored.
     * 
     * @param y a float value specifying the length of the y-axis of the graph
     */
    public void setGraphYSize(float y) {
        graphYSize = y;
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
    public void setOutputType(String graphType) {
        outputType = graphType;
    }

    /**
     * A <code>List</code> of patterns representing bean names/ids or classnames that should
     * be excluded from the output.
     * <p>
     * The returned underlying <code>List</code> is modifiable and will, if modified, affect
     * subsequent calls to the <code>ContextProcessor</code>'s <code>process()</code> method if
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
     * A <code>List</code> of patterns representing bean names/ids or classnames that should
     * be excluded from the output documents.  The preferred way to modify this list is 
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
     * on output.  Some beans (such as PropertyConfigurers and MessageSources)
     * are auxilliary and you may wish to exclude them from documents to keep the output
     * focused.
     * <p>
     * This method may be called any number of times to add different patterns to the
     * list of ignored beans.  Pattern may not be null (such a value will be ignored).
     * 
     * @param pattern a String representing a pattern to match.  The pattern uses
     * 		RegEx matching.  May not be null
     */
    public void addIgnoreBeans(String pattern) {
        if (pattern != null) ignoreBeans.add(pattern);
    }
    
    /**
     * A list of patterns of bean names or package names that determine how groups of
     * similar beans are graphed.  Specifically, a ranked set of beans will all 
     * appear on the same rank (row) of a graph.
     * 
     * @param rankBeans the List of patterns of grouped beans
     */
    public void setRankBeans(List rankedBeans) {
        this.rankBeans = rankedBeans;
    }    

    /**
     * Add a naming pattern of bean id's or bean names or classnames that should be
     * constrained to the same rank of a graph.
     * <p>
     * This method may be called any number of times to add different patterns to the
     * list of ignored beans.  Pattern may not be null (such a value will be ignored).
     * 
     * @param pattern a String representing a pattern to match.  The pattern uses 
     * 		RegEx matching.  May not be null
     */
    public void addRankBeans(String pattern) {
        if (pattern != null) rankBeans.add(pattern);
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
     * The type of output that the GraphViz 'dot' program should create from the
     * intermediate .dot files.  Default is PNG.  See {@link #setGraphOutputType} for 
     * some of the optional formats supported
     * 
     * @return the type of graph output that will be generated from the 
     *      .dot files
     * @see #setGraphOutputType
     */
    public String getOutputType() {
        return outputType;
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
     * Default shape used to describe bean nodes on the graph.  See {@link #setGraphBeanShape}
     * for some of the options
     * 
     * @return the shape GraphViz should use to display beans
     * @see #setGraphBeanShape
     */
    public String getBeanShape() {
        return beanShape;
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
    public String getFontName() {
        return fontName;
    }

    /**
     * Font size (pt) used for node labels on graph output
     * 
     * @return the font size used for bean labels on the graph
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Label position denoting whether graph labels appear at the top or bottom of the
     * graph.
     * 
     * @return 't' or 'b' to denote top or bottom respectively
     */
    public char getLabelLocation() {
        return labelLocation;
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
    public String getRatio() {
        return ratio;
    }

    /**
     * The maximum length of the x-axis of the graph in inches.  Useful to set this value
     * if the graph has to be printed to ensure it will fit on the target paper size.  Used
     * by GraphViz in conjunction with the ratio setting to determine final layout and
     * positioning of nodes on the graph.  If the corresponding value for the y-axis of
     * the graph is not set, this value is ignored.
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
     * the graph is not set, this value is ignored.
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
     * A list of patterns of bean names or package names that determine how groups of
     * similar beans are graphed.  Specifically, a ranked set of beans will all 
     * appear on the same rank (row) of a graph.
     * 
     * @return the List of patterns of grouped beans
     */
    public List getRankBeans() {
        return rankBeans;
    }
}
