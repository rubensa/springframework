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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdom.Element;
import org.springframework.beandoc.BeanDocException;
import org.springframework.beandoc.ContextProcessor;
import org.springframework.core.io.Resource;



/**
 * GraphVizDecorator
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class GraphVizDecorator extends SimpleDecorator {

    private static final String ATTRIBUTE_GRAPH_NAME = "beandocGraphName";
  
    private static final String ATTRIBUTE_GRAPH_FONTNAME = "beandocGraphFontName";
	
	private static final String ATTRIBUTE_GRAPH_FONTSIZE = "beandocGraphFontSize";
	
    private static final String ATTRIBUTE_GRAPH_SIZE = "beandocGraphSize";
  
    private static final String ATTRIBUTE_GRAPH_RATIO = "beandocGraphRatio";
	
	private static final String ATTRIBUTE_GRAPH_BEANSHAPE = "beandocGraphBeanShape";
	
	private static final String ATTRIBUTE_GRAPH_LABELLOCATION = "beandocGraphLabelLocation";
	
	private static final String ATTRIBUTE_COLOUR = "beandocFillColour";
    
    private String graphFontName = "helvetica";
    
    private int graphFontSize = 10;
    
    private String graphRatio = "auto";
    
    private float graphXSize = -1f;
    
    private float graphYSize = -1f;
    
    private String graphBeanShape = "box";
    
    private char graphLabelLocation = 't';
    
    private String defaultFillColour = "#cfcccc";
    
    private Map beanColours = new HashMap();

    /**
     * 
     */
    public GraphVizDecorator() {
        addBeanColours("*Dao", "#80cc80");
        addBeanColours("*DataSource", "#cceecc");
        addBeanColours("*Interceptor", "#cceeee");
        addBeanColours("*Controller", "#cceeee");
        addBeanColours("*HandlerMapping", "#cceeee");
        addBeanColours("*Filter", "#cceeee");
        addBeanColours("*Validator", "#eecc80");
    }
    
    /**
     * Decorates root element with graph type attributes and each bean element
     * as required with
     * 
     * @see org.springframework.beandoc.output.SimpleDecorator#decorateElement(org.jdom.Element)
     */
    protected void decorateElement(Element element) {
        if (element.isRootElement()) {
            element.setAttribute(ATTRIBUTE_GRAPH_FONTNAME, graphFontName);
			element.setAttribute(ATTRIBUTE_GRAPH_FONTSIZE, String.valueOf(graphFontSize));
			element.setAttribute(ATTRIBUTE_GRAPH_RATIO, String.valueOf(graphRatio));		
			element.setAttribute(ATTRIBUTE_GRAPH_BEANSHAPE, graphBeanShape);
			element.setAttribute(ATTRIBUTE_GRAPH_LABELLOCATION, String.valueOf(graphLabelLocation));
        }
        
        if ("bean".equals(element.getName())) {
			String idOrName = element.getAttributeValue(ContextProcessor.ATTRIBUTE_ID);
			if (idOrName == null) idOrName = element.getAttributeValue(ContextProcessor.ATTRIBUTE_NAME);
			String className = element.getAttributeValue(ContextProcessor.ATTRIBUTE_CLASSNAME);
			
			String colour = getColourForBean(idOrName, className);
			
			element.setAttribute(ATTRIBUTE_COLOUR, colour);
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

}
