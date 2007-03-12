/*
 * FilenameStrategy.java
 */

package org.springframework.beandoc.output;


/**
 * FilenameStrategy implementors return an output File based on an input
 * location.
 * 
 * @author Darren Davison
 * @since 1.0
 */
interface FilenameStrategy {

    String getFileName(String input);
    
}
