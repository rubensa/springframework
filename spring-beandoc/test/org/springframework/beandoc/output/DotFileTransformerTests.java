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
import java.util.List;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Darren Davison
 * @since 1.0
 */
public class DotFileTransformerTests extends TestCase {
    
    DotFileTransformer dft;
    Document[] docs = new Document[2];
    File outputDir;
    
    public void setUp() {
        outputDir = new File("/tmp"); // never used, doesn't matter
        dft = new DotFileTransformer();
        dft.setFilenameStrategy(new FilenameAppenderStrategy(".dot"));
        assertEquals("/org/springframework/beandoc/output/stylesheets/dot.xsl", dft.getTemplateName());
        
        for (int i = 0; i < 2; i++) {
            docs[i] = new Document();
            Element root = new Element("beans");
            root.setAttribute("beandocFileName", "context" + i + ".xml");
            docs[i].setRootElement(root);
            
            Element bean1 = new Element("bean");
            Element bean2 = new Element("bean");
            bean1.setAttribute("id", "bean1" + i);
            bean1.setAttribute("id", "bean2" + i);
            root.addContent(bean1);
            root.addContent(bean2);
        }
    }
    
    public void testOutputNames() {
        assertEquals("testfile.xml.dot", dft.getOutputForDocument("testfile.xml"));
    }
    
    public void testPostTransform() {
        TestDotFileTransformer tdft = new TestDotFileTransformer();
        tdft.transform(docs, outputDir);
    }
    
    class TestDotFileTransformer extends DotFileTransformer {
        // we're interested in the 3rd invocation of this method (twice for the individual docs,
        // 3rd time for the consolidated list
        int count = 0;
        protected void doXslTransform(Document doc, File outputDir) {
            if (++count == 3) {
                List beanList = doc.getRootElement().getChildren("bean");
                assertEquals(4, beanList.size());    
            }       
        }
    }
}
