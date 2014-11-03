package de.mpg.mpdl.service.rest.swc.process;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Closer;
import de.mpg.mpdl.service.rest.swc.ServiceConfiguration;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class RestProcessUtils {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RestProcessUtils.class);

	private static final String JAVAX_SERVLET_CONTEXT_TEMPDIR = "javax.servlet.context.tempdir";
	private static final String SWC_VIEW_HTML_TEMPLATE_FILE_NAME = "swc_view_template.html";
	private static final String SWC_VIEW_THUMB_HTML_TEMPLATE_FILE_NAME = "swc_view_template_thumb.html";
	private static final String JS_LIBS_LINKED_FILE_NAME = "js-linked.html";
	private static final String JS_LIBS_PORTABLE_FILE_NAME = "js-portable.html";

    private static ServiceConfiguration config = new ServiceConfiguration();

    public static ServiceConfiguration getConfig() {
        return config;
    }

	public static Response generateViewFromTextarea(String swc, boolean portable)
			throws IOException {
		return buildHtmlResponse(generateResponseHtml(swc, portable, false));
	}

	public static Response generateViewFromUrl(String url, boolean portable)
			throws IOException {
		// TODO: jersey-client
		URLConnection swcSourceConnection = UriBuilder
                .fromPath(url)
                .build()
                .toURL()
				.openConnection();
		return buildHtmlResponse(generateResponseHtml(
				getInputStreamAsString(swcSourceConnection.getInputStream()),
				portable, false));
	}

	public static Response generateViewFromFiles(HttpServletRequest request)
			throws IOException, FileUploadException {
		return buildHtmlResponse(generateHtmlFromFiles(request));
	}

	public static String generateHtmlFromFiles(HttpServletRequest request)
			throws FileUploadException, IOException {
		List<FileItem> fileItems = uploadFiles(request);
		LOGGER.info("files uploaded...");
		return generateResponseHtml(
				getInputStreamAsString(getFirstFileItem(fileItems)
						.getInputStream()), isPortable(fileItems), false);
	}

	private static boolean isPortable(List<FileItem> fileItems) {
		for (FileItem fileItem : fileItems) {
			if (fileItem.isFormField()
					&& "portable".equals(fileItem.getFieldName())) {
				return fileItem.getString() != null
						&& Boolean.parseBoolean(fileItem.getString());
			}
		}
		return false;
	}

	public static Response generateThumbnailFromFiles(HttpServletRequest request)
			throws FileUploadException, IOException {

		// upload files
		List<FileItem> fileItems = uploadFiles(request);
		LOGGER.info("files uploaded...");
		// Get only the 1st item (multiple items is not relevant so far)
		FileItem item = getFirstFileItem(fileItems);

		return generateThumbnail(item.getInputStream());

	}

	public static Response generateThumbnailFromUrl(String url)
            throws IOException {

		// get de.mpg.mpdl.service.rest.swc from url (input stream)
		URLConnection swcSourceConnection = UriBuilder
                .fromUri(url)
                .build()
                .toURL()
				.openConnection();
		return generateThumbnail(swcSourceConnection.getInputStream());

	}

	public static Response generateThumbnailFromTextarea(String swc)
			throws IOException {
		// get de.mpg.mpdl.service.rest.swc from String
		return generateThumbnail(new ByteArrayInputStream(
				swc.getBytes(StandardCharsets.UTF_8)));
	}

	private static Response generateThumbnail(InputStream inputStream)
			throws IOException {
		Closer closer = Closer.create();
		closer.register(inputStream);
        byte[] bytes = null;


		try {
            /*URLConnection screenshotConn = null;
			// screenshot service connection
			screenshotConn = UriBuilder
                    .fromUri(config.getScreenshotServiceUrl())
                    .path("take")
//                    .queryParam("useFireFox", "true")
                    .build()
                    .toURL()
					.openConnection();
			screenshotConn.setDoOutput(true);

          	// build response entity directly from .swc inputStream
			bytes = generateResponseHtml(getInputStreamAsString(inputStream),
					true, true).getBytes(StandardCharsets.UTF_8);

            LOGGER.info(new String(bytes));

			InputStream swcResponseInputStream = closer
					.register(new ByteArrayInputStream(bytes));

			ByteStreams.copy(swcResponseInputStream,
					closer.register(screenshotConn.getOutputStream()));

			bytes = ByteStreams.toByteArray(screenshotConn.getInputStream());*/

            // jersey-client implementation:


            Client client = ClientBuilder.newClient();
            WebTarget target = client
                    .target(config.getScreenshotServiceUrl())
                    .path("take");

            Form form = new Form()
                    .param("html",
                        generateResponseHtml(getInputStreamAsString(inputStream),true, true))
                    .param("format", "png");

            Response response = target
                    .request(MediaType.APPLICATION_OCTET_STREAM_TYPE, MediaType.TEXT_HTML_TYPE)
                    .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

            bytes = ByteStreams.toByteArray((InputStream)response.getEntity());

		} catch (Throwable e) {
			throw closer.rethrow(e);
		} finally {
			closer.close();
		}

		// Return the response of the screenshot service
		return Response.status(Status.OK).entity(bytes).type("image/png")
				.build();
	}

	public static String generateResponseHtml(String swc, boolean portable,
			boolean thumb) throws IOException {
		// get js libs block
		String chunk = getResourceAsString(portable ? JS_LIBS_PORTABLE_FILE_NAME
				: JS_LIBS_LINKED_FILE_NAME);
		// insert js libs block
		chunk = getResourceAsString(
				thumb ? SWC_VIEW_THUMB_HTML_TEMPLATE_FILE_NAME
						: SWC_VIEW_HTML_TEMPLATE_FILE_NAME).replace(
				"%JS_LIBS_PLACEHOLDER%", chunk);
		// repalce other placeholders
		return chunk.replace("%SWC_CONTENT_PLACEHOLDER%", swc).replace(
				"%SWC_SERVICE_PLACEHOLDER%", config.getServiceUrl());
	}

	// Helpers

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

	public static Response buildHtmlResponse(String str) {
		return buildHtmlResponse(str, Status.OK);
	}

	public static Response buildHtmlResponse(String str, Status status) {
		return Response.status(status).entity(str).type(MediaType.TEXT_HTML)
				.build();
	}

	public static Response buildJSONResponse(String str, Status status) {
		return Response.status(status).entity(str)
				.type(MediaType.APPLICATION_JSON).build();
	}

	public static String getResourceAsString(String fileName)
			throws IOException {
		return getInputStreamAsString(getResourceAsInputStream(fileName));
	}

	public static InputStream getResourceAsInputStream(String fileName)
			throws IOException {
        return new RestProcessUtils().getClass().getClassLoader()
				.getResourceAsStream(fileName);
	}

	public static URL getResourceAsURL(String fileName)
			throws IOException {
        return new RestProcessUtils().getClass().getClassLoader()
				.getResource(fileName);
	}


	private static String getInputStreamAsString(InputStream stream)
			throws IOException {
		Closer closer = Closer.create();
		closer.register(stream);
		String string = null;
		try {
			string = CharStreams.toString(new InputStreamReader(stream,
					StandardCharsets.UTF_8));
		} catch (Throwable e) {
			closer.rethrow(e);
		} finally {
			closer.close();
		}
		return string;
	}

	private static File getInputStreamAsFile(InputStream stream)
			throws IOException {
		Closer closer = Closer.create();
		closer.register(stream);
		File f = File.createTempFile("swc", ".swc");
		try {
			ByteStreams.copy(stream, new FileOutputStream(f));
		} catch (Throwable e) {
			closer.rethrow(e);
		} finally {
			closer.close();
		}
		return f;
	}

	// get only first processed file!
	public static FileItem getFirstFileItem(List<FileItem> fileItems)
			throws IOException {

		if (LOGGER.isDebugEnabled()) {
			for (FileItem fileItem : fileItems) {
				if (fileItem.isFormField()) {
					LOGGER.debug("fileItem.getFieldName():"
							+ fileItem.getFieldName());
					LOGGER.debug("value:" + fileItem.getString());
				}
			}
		}

		for (FileItem fileItem : fileItems) {
			if (!fileItem.isFormField()) {
				return fileItem;
			}
		}
		return null;
	}

    // Try to resolve path to file according to running context in following order:
    // 1) from absolute path
    // 2) from local resources
    public static String resolvePath(String path) throws IOException, URISyntaxException {
        return Files.exists(Paths.get(path)) ?
                path : new URI(getResourceAsURL(path).toString()).getPath();
    }

	/**************************
	 *
	 * SWC ANALYZE
	 *
	 **************************/

	public static Response generateAnalyzeFromFiles(HttpServletRequest request)
            throws IOException, FileUploadException, URISyntaxException {
		LMeasure lMeasure = new LMeasure();
		List<FileItem> items = uploadFiles(request);
		lMeasure.execute(getInputStreamAsFile(getFirstFileItem(items)
				.getInputStream()), getQuery(items), getNumberOfBins(items),
				getWidthOfBins(items));
		return buildJSONResponse(lMeasure.toJSON(), Status.OK);
	}

	public static Response generateAnalyzeFromTextArea(String swc,
			String query, int numberOfBins, boolean widthOfBins)
            throws IOException, URISyntaxException {
		LMeasure lMeasure = new LMeasure();
		lMeasure.execute(
				getInputStreamAsFile(new ByteArrayInputStream(swc
						.getBytes(StandardCharsets.UTF_8))), "", numberOfBins,
				widthOfBins);
		return buildJSONResponse(lMeasure.toJSON(), Status.OK);
	}

	public static Response generateAnalyzeFromUrl(String url, String query,
			int numberOfBins, boolean widthOfBins) throws IOException, URISyntaxException {
		URLConnection swcSourceConnection = UriBuilder
                .fromUri(url)
                .build()
                .toURL()
				.openConnection();
		LMeasure lMeasure = new LMeasure();
		lMeasure.execute(
				getInputStreamAsFile(swcSourceConnection.getInputStream()),
				query, numberOfBins, widthOfBins);
		return buildJSONResponse(lMeasure.toJSON(), Status.OK);
	}

	private static int getNumberOfBins(List<FileItem> items) {
		for (FileItem item : items)
			if (item.isFormField()
					&& "numberOfBins".equals(item.getFieldName()))
				return Integer.parseInt(item.getString());
		return 0;
	}
    private static boolean getWidthOfBins(List<FileItem> items) {
		for (FileItem item : items)
			if (item.isFormField() && "typeOfBins".equals(item.getFieldName()))
				return item.getString().equals("width");
		return false;
	}

	private static String getQuery(List<FileItem> items) {
		for (FileItem item : items)
			if (item.isFormField() && "query".equals(item.getFieldName()))
				return item.getString();
		return null;
	}

}
