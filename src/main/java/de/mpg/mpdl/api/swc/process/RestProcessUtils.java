package de.mpg.mpdl.api.swc.process;

import com.google.common.io.CharStreams;

import de.mpg.mpdl.api.swc.ServiceConfiguration;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class RestProcessUtils {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RestProcessUtils.class);

	private static final String JAVAX_SERVLET_CONTEXT_TEMPDIR = "javax.servlet.context.tempdir";
	private static final String SWC_VIEW_HTML_TEMPLATE_FILE_NAME = "swc_view_template.html";
	private static final ServiceConfiguration config = new ServiceConfiguration();

	public static List<FileItem> uploadFiles(HttpServletRequest request)
			throws FileUploadException {
		List<FileItem> items = null;
		if (ServletFileUpload.isMultipartContent(request)) {
			ServletContext servletContext = request.getServletContext();
			File repository = (File) servletContext
					.getAttribute(JAVAX_SERVLET_CONTEXT_TEMPDIR);
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setRepository(repository);
			ServletFileUpload fileUpload = new ServletFileUpload(factory);
			items = fileUpload.parseRequest(request);
		}
		return items;
	}

	public static String generateHtmlRepresentationFromFiles(
			HttpServletRequest request) throws FileUploadException, IOException {
		List<FileItem> fileItems = uploadFiles(request);
		LOGGER.info("files uploaded...");
		String swc = null;
		// get only first processed file!
		forloop: for (FileItem fileItem : fileItems) {
			if (!fileItem.isFormField()) {
				swc = getInputStreamAsString(fileItem.getInputStream());
				break forloop;
			}
		}
		return generateHtmlRepresentation(swc);
	}

	public static String generateHtmlRepresentation(String swc) {
		String swcHtmlTemplate = getResourceAsString(SWC_VIEW_HTML_TEMPLATE_FILE_NAME);
		return swcHtmlTemplate.replace("%SWC_CONTENT_PLACEHOLDER%", swc)
				.replace("%SWC_SERVICE_PLACEHOLDER%", config.getServiceUrl());
	}

	// Helpers
	public static Response buildHtmlResponse(String str) {
		return buildHtmlResponse(str, Status.OK);
	}

	public static Response buildHtmlResponse(String str, Status status) {
		return Response.status(status).entity(str).type(MediaType.TEXT_HTML)
				.build();
	}

	private static String getResourceAsString(String fileName) {
		InputStream stream = new RestProcessUtils().getClass().getClassLoader()
				.getResourceAsStream(fileName);
		return getInputStreamAsString(stream);
	}

	private static String getInputStreamAsString(InputStream stream) {
		String str = null;
		try {
			str = CharStreams.toString(new InputStreamReader(stream, "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}

}
