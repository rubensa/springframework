/*
 * FilenameAppenderStrategy.java
 */

package org.springframework.beandoc.output;



/**
 * FilenameAppenderStrategy is a trivial implementation that simply
 * appends a String to the end of the input file name in order to
 * produce the output name.  The suffix is supplied in the 
 * constructor.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class FilenameAppenderStrategy implements FilenameStrategy {

    private String suffix;

    /**
     * takes the String that will be appended to the input name to 
     * produce the output file name
     */
    public FilenameAppenderStrategy(String suffix) {
        this.suffix = suffix;
    }

    /**
     * @see org.springframework.beandoc.output.FilenameStrategy#getFileName(java.lang.String)
     */
    public String getFileName(String input) {
        return input + suffix;
    }

}
