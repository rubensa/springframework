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
package org.springframework.web.servlet.view.document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;

import junit.framework.TestCase;

/**
 * @author Alef Arendsen
 */
public class ExcelTestSuite extends TestCase {	
	
	MockHttpServletRequest request;
	MockHttpServletResponse response;
	MockServletContext servletCtx;
	StaticWebApplicationContext webAppCtx;
	
	public void setUp() {		
		servletCtx = new MockServletContext() {			
			public String getRealPath(String path) {
				File f = new File("./test/org/springframework/web/servlet/view/document/" + path);
				return f.getAbsolutePath();
			}
		};
		request = new MockHttpServletRequest(servletCtx);		
		response = new MockHttpServletResponse();
		webAppCtx = new StaticWebApplicationContext();
		webAppCtx.setServletContext(servletCtx);
	}
	
	public void testExcel()
	throws Exception {
		
		AbstractExcelView excelView = new AbstractExcelView() {			
			protected void buildExcelDocument(Map model, HSSFWorkbook wb,
				HttpServletRequest request, HttpServletResponse response)
			throws Exception {
				HSSFSheet sheet = wb.createSheet();				
				wb.setSheetName(0, "Test Sheet");
				
				// test all possible permutation of row or column not existing
				HSSFCell cell = getCell(sheet, 2, 4);				
				cell.setCellValue("Test Value");
				cell = getCell(sheet, 2, 3);
				setText(cell, "Test Value");
				cell = getCell(sheet, 3, 4);
				setText(cell, "Test Value");
				cell = getCell(sheet, 2, 4);
				setText(cell, "Test Value");				
			}
			
		};
		
		excelView.render(new HashMap(), request, response);
		
		POIFSFileSystem poiFs = new POIFSFileSystem(new ByteArrayInputStream(response.getContentAsByteArray()));
		HSSFWorkbook wb = new HSSFWorkbook(poiFs);
		assertEquals("Test Sheet", wb.getSheetName(0));
		HSSFSheet sheet = wb.getSheet("Test Sheet");
		HSSFRow row = sheet.getRow(2);
		HSSFCell cell = row.getCell((short)4);
		assertEquals("Test Value", cell.getStringCellValue());		
	}
	
	public void testExcelWithTemplateNoLoc()
	throws Exception {				
		
		request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE,
				newDummyLocaleResolver("nl","nl"));		
		
		
		AbstractExcelView excelView = new AbstractExcelView() {			
			protected void buildExcelDocument(Map model, HSSFWorkbook wb,
				HttpServletRequest request, HttpServletResponse response)
			throws Exception {
				HSSFSheet sheet = wb.getSheet("Sheet1");
				
				// test all possible permutation of row or column not existing
				HSSFCell cell = getCell(sheet, 2, 4);				
				cell.setCellValue("Test Value");
				cell = getCell(sheet, 2, 3);
				setText(cell, "Test Value");
				cell = getCell(sheet, 3, 4);
				setText(cell, "Test Value");
				cell = getCell(sheet, 2, 4);
				setText(cell, "Test Value");				
			}
			
		};
		
		excelView.setApplicationContext(webAppCtx);		
		
		excelView.setUrl("template");
		excelView.render(new HashMap(), request, response);
		
		POIFSFileSystem poiFs = new POIFSFileSystem(new ByteArrayInputStream(response.getContentAsByteArray()));
		HSSFWorkbook wb = new HSSFWorkbook(poiFs);		
		HSSFSheet sheet = wb.getSheet("Sheet1");
		HSSFRow row = sheet.getRow(0);
		HSSFCell cell = row.getCell((short)0);
		assertEquals("Test Template", cell.getStringCellValue());
	}
	
	public void testExcelWithTemplateAndCountryAndLanguage()
	throws Exception {				
		
		request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE,
				newDummyLocaleResolver("en","us"));		
		
		
		AbstractExcelView excelView = new AbstractExcelView() {			
			protected void buildExcelDocument(Map model, HSSFWorkbook wb,
				HttpServletRequest request, HttpServletResponse response)
			throws Exception {
				HSSFSheet sheet = wb.getSheet("Sheet1");
				
				// test all possible permutation of row or column not existing
				HSSFCell cell = getCell(sheet, 2, 4);				
				cell.setCellValue("Test Value");
				cell = getCell(sheet, 2, 3);
				setText(cell, "Test Value");
				cell = getCell(sheet, 3, 4);
				setText(cell, "Test Value");
				cell = getCell(sheet, 2, 4);
				setText(cell, "Test Value");				
			}
			
		};
		
		excelView.setApplicationContext(webAppCtx);		
		
		excelView.setUrl("template");
		excelView.render(new HashMap(), request, response);
		
		POIFSFileSystem poiFs = new POIFSFileSystem(new ByteArrayInputStream(response.getContentAsByteArray()));
		HSSFWorkbook wb = new HSSFWorkbook(poiFs);		
		HSSFSheet sheet = wb.getSheet("Sheet1");
		HSSFRow row = sheet.getRow(0);
		HSSFCell cell = row.getCell((short)0);
		assertEquals("Test Template American English", cell.getStringCellValue());
	}
	
	public void testExcelWithTemplateAndLanguage()
	throws Exception {				
		
		request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE,
				newDummyLocaleResolver("de", ""));		
		
		
		AbstractExcelView excelView = new AbstractExcelView() {			
			protected void buildExcelDocument(Map model, HSSFWorkbook wb,
				HttpServletRequest request, HttpServletResponse response)
			throws Exception {
				HSSFSheet sheet = wb.getSheet("Sheet1");
				
				// test all possible permutation of row or column not existing
				HSSFCell cell = getCell(sheet, 2, 4);				
				cell.setCellValue("Test Value");
				cell = getCell(sheet, 2, 3);
				setText(cell, "Test Value");
				cell = getCell(sheet, 3, 4);
				setText(cell, "Test Value");
				cell = getCell(sheet, 2, 4);
				setText(cell, "Test Value");				
			}
			
		};
		
		excelView.setApplicationContext(webAppCtx);		
		
		excelView.setUrl("template");
		excelView.render(new HashMap(), request, response);
		
		POIFSFileSystem poiFs = new POIFSFileSystem(new ByteArrayInputStream(response.getContentAsByteArray()));
		HSSFWorkbook wb = new HSSFWorkbook(poiFs);		
		HSSFSheet sheet = wb.getSheet("Sheet1");
		HSSFRow row = sheet.getRow(0);
		HSSFCell cell = row.getCell((short)0);
		assertEquals("Test Template auf Deutsch", cell.getStringCellValue());
	}
	
	private LocaleResolver newDummyLocaleResolver(final String lang, final String country) {
		return new LocaleResolver() {
			public Locale resolveLocale(HttpServletRequest request) {
				return new Locale(lang, country);
			}
			public void setLocale(HttpServletRequest request,
					HttpServletResponse response, Locale locale) {
				// not supported!

			}
		};
	}

}
