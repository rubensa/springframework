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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

/**
 * From the example in the Reference Docs.
 * <p>
 * <b>This class is instructive only and is not designed for normal use</b>
 * <p>
 * See the <a href="http://springframework.sourceforge.net/beandoc/refdoc/extending.html#d0e550">
 * relevent section of the reference documentation</a>
 * for more information.
 * 
 * @author Darren Davison
 * @since 1.0
 */
public class CsvTransformer implements Transformer {

    /**
     * create a new CSV file for each context file
     */
    public void transform(Document[] contextDocuments, File outputDir) {
        for (int i = 0; i < contextDocuments.length; i++) {
            Document doc = contextDocuments[i];
            File outputFile = new File(
                outputDir, doc.getRootElement().getAttributeValue("beandocFileName") + ".csv"
            );
            
            try {
                if (!outputFile.exists())
                    outputFile.createNewFile();            
            
                FileWriter out = new FileWriter(outputFile);
                String csvLine = "";
                
                List beans = doc.getRootElement().getChildren("bean");
                for (Iterator iter = beans.iterator(); iter.hasNext();) {
                    Element bean = (Element) iter.next();
                    csvLine = 
                        bean.getAttributeValue(CsvDecorator.ATTRIBUTE_COUNTER) + "," +
                        bean.getAttributeValue("id") + "," + 
                        bean.getAttributeValue("class") + "\n";

                    out.write(csvLine);
                }
                out.flush();
                out.close();
                
            } catch (IOException e) {
                // should be handled properly!
                e.printStackTrace();
            }
        }
    }

}
