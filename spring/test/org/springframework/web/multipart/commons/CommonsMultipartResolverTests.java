package org.springframework.web.multipart.commons;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.mockobjects.servlet.MockFilterConfig;
import com.mockobjects.servlet.MockFilterChain;
import junit.framework.TestCase;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.mock.MockServletContext;
import org.springframework.web.mock.MockHttpServletResponse;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.WebUtils;

/**
 * @author Juergen Hoeller
 * @since 08.10.2003
 */
public class CommonsMultipartResolverTests extends TestCase {

	public void testWithApplicationContext() throws MultipartException {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		MockServletContext sc = new MockServletContext();
		sc.setAttribute(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File("mytemp"));
		wac.setServletContext(sc);
		CommonsMultipartResolver resolver = new MockCommonsMultipartResolver();
		DiskFileUpload fileUpload = resolver.getFileUpload();
		resolver.setMaximumFileSize(1000);
		resolver.setMaximumInMemorySize(100);
		resolver.setHeaderEncoding("enc");
		resolver.setApplicationContext(wac);
		assertEquals(1000, fileUpload.getSizeMax());
		assertEquals(100, fileUpload.getSizeThreshold());
		assertEquals("enc", fileUpload.getHeaderEncoding());
		assertTrue(fileUpload.getRepositoryPath().endsWith("mytemp"));

		MockHttpServletRequest originalRequest = new MockHttpServletRequest(null, null, null);
		originalRequest.setContentType("multipart/form-data");
		originalRequest.addHeader("Content-type", "multipart/form-data");
		assertTrue(resolver.isMultipart(originalRequest));
		MultipartHttpServletRequest request = resolver.resolveMultipart(originalRequest);

		Set parameterNames = new HashSet();
		Enumeration parameterEnum = request.getParameterNames();
		while (parameterEnum.hasMoreElements()) {
			parameterNames.add(parameterEnum.nextElement());
		}
		assertEquals(2, parameterNames.size());
		assertTrue(parameterNames.contains("field3"));
		assertTrue(parameterNames.contains("field4"));
		List parameterValues = Arrays.asList(request.getParameterValues("field3"));
		assertEquals(1, parameterValues.size());
		assertTrue(parameterValues.contains("value3"));
		parameterValues = Arrays.asList(request.getParameterValues("field4"));
		assertEquals(2, parameterValues.size());
		assertTrue(parameterValues.contains("value4"));
		assertTrue(parameterValues.contains("value5"));
		List parameterMapKeys = new ArrayList();
		List parameterMapValues = new ArrayList();
		for (Iterator parameterMapIter = request.getParameterMap().keySet().iterator(); parameterMapIter.hasNext();) {
			String key = (String) parameterMapIter.next();
			parameterMapKeys.add(key);
			parameterMapValues.add(request.getParameterMap().get(key));
		}
		assertEquals(2, parameterMapKeys.size());
		assertEquals(2, parameterMapValues.size());
		int field3Index = parameterMapKeys.indexOf("field3");
		int field4Index = parameterMapKeys.indexOf("field4");
		assertTrue(field3Index != -1);
		assertTrue(field4Index != -1);
		parameterValues = Arrays.asList((String[]) parameterMapValues.get(field3Index));
		assertEquals(1, parameterValues.size());
		assertTrue(parameterValues.contains("value3"));
		parameterValues = Arrays.asList((String[]) parameterMapValues.get(field4Index));
		assertEquals(2, parameterValues.size());
		assertTrue(parameterValues.contains("value4"));
		assertTrue(parameterValues.contains("value5"));

		Set fileNames = new HashSet();
		Iterator fileIter = request.getFileNames();
		while (fileIter.hasNext()) {
			fileNames.add(fileIter.next());
		}
		assertEquals(2, fileNames.size());
		assertTrue(fileNames.contains("field1"));
		assertTrue(fileNames.contains("field2"));
		CommonsMultipartFile file1 = (CommonsMultipartFile) request.getFile("field1");
		CommonsMultipartFile file2 = (CommonsMultipartFile) request.getFile("field2");
		List fileMapKeys = new ArrayList();
		List fileMapValues = new ArrayList();
		for (Iterator fileMapIter = request.getFileMap().keySet().iterator(); fileMapIter.hasNext();) {
			String key = (String) fileMapIter.next();
			fileMapKeys.add(key);
			fileMapValues.add(request.getFileMap().get(key));
		}
		assertEquals(2, fileMapKeys.size());
		assertEquals(2, fileMapValues.size());
		int field1Index = fileMapKeys.indexOf("field1");
		int field2Index = fileMapKeys.indexOf("field2");
		assertTrue(field1Index != -1);
		assertTrue(field2Index != -1);
		MultipartFile mapFile1 = (MultipartFile) fileMapValues.get(field1Index);
		MultipartFile mapFile2 = (MultipartFile) fileMapValues.get(field2Index);
		assertEquals(mapFile1, file1);
		assertEquals(mapFile2, file2);

		assertEquals("type1", file1.getContentType());
		assertEquals("type2", file2.getContentType());
		assertEquals("file1.txt", file1.getOriginalFileName());
		assertEquals("file2.txt", file2.getOriginalFileName());
		assertEquals("text1", new String(file1.getBytes()));
		assertEquals("text2", new String(file2.getBytes()));
		assertEquals(5, file1.getSize());
		assertEquals(5, file2.getSize());
		assertTrue(file1.getInputStream() instanceof ByteArrayInputStream);
		assertTrue(file2.getInputStream() instanceof ByteArrayInputStream);
		File transfer1 = new File("C:/transfer1");
		File transfer2 = new File("C:/transfer2");
		file1.transferTo(transfer1);
		file2.transferTo(transfer2);
		assertEquals(transfer1, ((MockFileItem) file1.getFileItem()).writtenFile);
		assertEquals(transfer2, ((MockFileItem) file2.getFileItem()).writtenFile);

		resolver.cleanupMultipart(request);
		assertTrue(((MockFileItem) file1.getFileItem()).deleted);
		assertTrue(((MockFileItem) file2.getFileItem()).deleted);
	}

	public void testWithServletContextAndFilter() throws ServletException, IOException {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.registerSingleton("filterMultipartResolver", MockCommonsMultipartResolver.class, new MutablePropertyValues());
		MockServletContext sc = new MockServletContext();
		sc.setAttribute(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File("mytemp"));
		wac.setServletContext(sc);
		CommonsMultipartResolver resolver = new CommonsMultipartResolver(sc);
		assertTrue(resolver.getFileUpload().getRepositoryPath().endsWith("mytemp"));

		MockFilterConfig filterConfig = new MockFilterConfig();
		filterConfig.setupGetServletContext(sc);
		final List files = new ArrayList();
		MockFilterChain filterChain = new MockFilterChain() {
			public void doFilter(ServletRequest originalRequest, ServletResponse response) {
				MultipartHttpServletRequest request = (MultipartHttpServletRequest) originalRequest;
				files.addAll(request.getFileMap().values());
			}
		};
		MultipartFilter filter = new MultipartFilter();
		filter.init(filterConfig);
		MockHttpServletRequest originalRequest = new MockHttpServletRequest(null, null, null);
		originalRequest.setContentType("multipart/form-data");
		originalRequest.addHeader("Content-type", "multipart/form-data");
		filter.doFilter(originalRequest, new MockHttpServletResponse(), filterChain);
		CommonsMultipartFile file1 = (CommonsMultipartFile) files.get(0);
		CommonsMultipartFile file2 = (CommonsMultipartFile) files.get(1);
		assertTrue(((MockFileItem) file1.getFileItem()).deleted);
		assertTrue(((MockFileItem) file2.getFileItem()).deleted);
	}

	public void testWithServletContextAndFilterWithCustomBeanName() throws ServletException, IOException {
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.registerSingleton("myMultipartResolver", MockCommonsMultipartResolver.class, new MutablePropertyValues());
		MockServletContext sc = new MockServletContext();
		sc.setAttribute(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File("mytemp"));
		wac.setServletContext(sc);
		CommonsMultipartResolver resolver = new CommonsMultipartResolver(sc);
		assertTrue(resolver.getFileUpload().getRepositoryPath().endsWith("mytemp"));

		MockFilterConfig filterConfig = new MockFilterConfig() {
			public String getInitParameter(String s) {
				if (MultipartFilter.MULTIPART_RESOLVER_BEAN_NAME_PARAM.equals(s))
					return "myMultipartResolver";
				else
					return super.getInitParameter(s);
			}
		};
		filterConfig.setupGetServletContext(sc);
		final List files = new ArrayList();
		MockFilterChain filterChain = new MockFilterChain() {
			public void doFilter(ServletRequest originalRequest, ServletResponse response) {
				MultipartHttpServletRequest request = (MultipartHttpServletRequest) originalRequest;
				files.addAll(request.getFileMap().values());
			}
		};
		MultipartFilter filter = new MultipartFilter();
		filter.init(filterConfig);
		MockHttpServletRequest originalRequest = new MockHttpServletRequest(null, null, null);
		originalRequest.setContentType("multipart/form-data");
		originalRequest.addHeader("Content-type", "multipart/form-data");
		filter.doFilter(originalRequest, new MockHttpServletResponse(), filterChain);
		CommonsMultipartFile file1 = (CommonsMultipartFile) files.get(0);
		CommonsMultipartFile file2 = (CommonsMultipartFile) files.get(1);
		assertTrue(((MockFileItem) file1.getFileItem()).deleted);
		assertTrue(((MockFileItem) file2.getFileItem()).deleted);
	}


	public static class MockCommonsMultipartResolver extends CommonsMultipartResolver {

		protected DiskFileUpload initFileUpload() {
			return new MockDiskFileUpload();
		}
	}


	private static class MockDiskFileUpload extends DiskFileUpload {

		public List parseRequest(HttpServletRequest request) {
			List fileItems = new ArrayList();
			MockFileItem fileItem1 = new MockFileItem("field1", "type1", "file1.txt", "text1");
			MockFileItem fileItem2 = new MockFileItem("field2", "type2", "C:/file2.txt", "text2");
			MockFileItem fileItem3 = new MockFileItem("field3", null, null, "value3");
			MockFileItem fileItem4 = new MockFileItem("field4", null, null, "value4");
			MockFileItem fileItem5 = new MockFileItem("field4", null, null, "value5");
			fileItems.add(fileItem1);
			fileItems.add(fileItem2);
			fileItems.add(fileItem3);
			fileItems.add(fileItem4);
			fileItems.add(fileItem5);
			return fileItems;
		}
	}


	private static class MockFileItem implements FileItem {

		private String fieldName;
		private String contentType;
		private String name;
		private String value;

		private File writtenFile;
		private boolean deleted;

		public MockFileItem(String fieldName, String contentType, String name, String value) {
			this.fieldName = fieldName;
			this.contentType = contentType;
			this.name = name;
			this.value = value;
		}

		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(value.getBytes());
		}

		public String getContentType() {
			return contentType;
		}

		public String getName() {
			return name;
		}

		public boolean isInMemory() {
			return true;
		}

		public long getSize() {
			return value.length();
		}

		public byte[] get() {
			return value.getBytes();
		}

		public String getString(String encoding) throws UnsupportedEncodingException {
			return new String(encoding);
		}

		public String getString() {
			return value;
		}

		public void write(File file) throws Exception {
			this.writtenFile = file;
		}

		public File getWrittenFile() {
			return writtenFile;
		}

		public void delete() {
			this.deleted = true;
		}

		public boolean isDeleted() {
			return deleted;
		}

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String s) {
			this.fieldName = s;
		}

		public boolean isFormField() {
			return (this.name == null);
		}

		public void setFormField(boolean b) {
			throw new UnsupportedOperationException();
		}

		public OutputStream getOutputStream() throws IOException {
			throw new UnsupportedOperationException();
		}
	}

}
