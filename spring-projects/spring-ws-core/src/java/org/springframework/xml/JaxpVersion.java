/*
 * Copyright 2006 the original author or authors.
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

package org.springframework.xml;

/**
 * Helper class used to find the current version of JAXP. We cannot depend on the Java version, since JAXP can be
 * upgraded idenpendantly of the Java version.
 * <p/>
 * Only distinguishes between JAXP 1.0, 1.1, 1.3, and 1.4, since JAXP 1.2 was a maintenance release with no new
 * classes.
 *
 * @author Arjen Poutsma
 */
public abstract class JaxpVersion {

    public static final int JAXP_10 = 0;

    public static final int JAXP_11 = 1;

    public static final int JAXP_13 = 3;

    public static final int JAXP_14 = 4;

    private static final String JAXP_11_CLASS_NAME = "javax.xml.transform.Transformer";

    private static final String JAXP_13_CLASS_NAME = "javax.xml.xpath.XPath";

    private static final String JAXP_14_CLASS_NAME = "javax.xml.transform.stax.StAXSource";

    private static int jaxpVersion = JAXP_10;

    static {
        try {
            Class.forName(JAXP_14_CLASS_NAME);
            jaxpVersion = JAXP_14;
        }
        catch (ClassNotFoundException ex1) {
            try {
                Class.forName(JAXP_13_CLASS_NAME);
                jaxpVersion = JAXP_13;
            }
            catch (ClassNotFoundException ex2) {
                try {
                    Class.forName(JAXP_11_CLASS_NAME);
                    jaxpVersion = JAXP_11;
                }
                catch (ClassNotFoundException ex3) {
                    // default to JAXP 1.0
                }
            }
        }
    }

    /**
     * Gets the major JAXP version. This means we can do things like if <code>(getJaxpVersion() < JAXP_13)</code>.
     *
     * @return a code comparable to the JAXP_XX codes in this class
     * @see #JAXP_10
     * @see #JAXP_11
     * @see #JAXP_13
     * @see #JAXP_14
     */
    public static int getJaxpVersion() {
        return jaxpVersion;
    }
}