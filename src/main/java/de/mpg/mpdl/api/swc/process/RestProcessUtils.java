package de.mpg.mpdl.api.swc.process;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Closer;
import de.mpg.mpdl.api.swc.ServiceConfiguration;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.*;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;

public class RestProcessUtils {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(RestProcessUtils.class);

	private static final String JAVAX_SERVLET_CONTEXT_TEMPDIR = "javax.servlet.context.tempdir";
	private static final String SWC_VIEW_HTML_TEMPLATE_FILE_NAME = "swc_view_template.html";
	private static final String JS_LIBS_LINKED_FILE_NAME = "js-linked.html";
	private static final String JS_LIBS_PORTABLE_FILE_NAME = "js-portable.html";
	private static final ServiceConfiguration config = new ServiceConfiguration();


    public static Response generateViewFromTextarea(String swc, boolean portable) throws IOException {
        return buildHtmlResponse(generateResponseHtml(swc, portable));
    }

    public static Response generateViewFromUrl(String url, boolean portable) throws IOException {
        URLConnection swcSourceConnection = URI
            .create(url).toURL()
            .openConnection();
        return  buildHtmlResponse(
            generateResponseHtml(getInputStreamAsString(swcSourceConnection.getInputStream()), portable)
        );
    }

    public static Response generateViewFromFiles(HttpServletRequest request) throws IOException, FileUploadException {
        return buildHtmlResponse(generateHtmlFromFiles(request));
    }


    public static String generateHtmlFromFiles(
            HttpServletRequest request) throws FileUploadException, IOException {
		List<FileItem> fileItems = uploadFiles(request);
		LOGGER.info("files uploaded...");
        return generateResponseHtml(getInputStreamAsString(getFirstFileItem(fileItems).getInputStream()), isPortable(fileItems));
	}



    private static boolean isPortable(List<FileItem> fileItems) {
        for (FileItem fileItem: fileItems) {
            if (fileItem.isFormField() && "portable".equals(fileItem.getFieldName())  ) {
                return fileItem.getString() != null && Boolean.parseBoolean(fileItem.getString());
            }
        }
        return false;
    }


    public static Response generateThumbnailFromFiles(HttpServletRequest request) throws FileUploadException, IOException {

        //upload files
        List<FileItem> fileItems = uploadFiles(request);
        LOGGER.info("files uploaded...");
        // Get only the 1st item (multiple items is not relevant so far)
        FileItem item = getFirstFileItem(fileItems);

        return generateThumbnail(item.getInputStream());

    }

    public static Response generateThumbnailFromUrl(String url) throws IOException {

        //get swc from url (input stream)
        URLConnection swcSourceConnection = URI
                .create(url).toURL()
                .openConnection();

        return generateThumbnail(swcSourceConnection.getInputStream());

    }


    private static Response generateThumbnail(InputStream inputStream) throws IOException {

        Closer closer = Closer.create();
        closer.register(inputStream);

        URLConnection screenshotConn = null;
        byte[] bytes = null;
        try {
            //screenshot service connection
            screenshotConn = URI
                    .create(config.getScreenshotServiceUrl()).toURL()
                    .openConnection();
            screenshotConn.setDoOutput(true);


            //build response entity directly from .swc inputStream
            InputStream swcResponseInputStream = closer.register(
                    new ByteArrayInputStream(
                            generateResponseHtml(getInputStreamAsString(inputStream), true).getBytes("UTF-8")
                    )
           );

            ByteStreams.copy(swcResponseInputStream,
                    closer.register(screenshotConn.getOutputStream()));

            bytes = ByteStreams.toByteArray(screenshotConn.getInputStream());

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }



        // Return the response of the screenshot service
        return Response
                .status(Status.OK)
                .entity(bytes)
                .type("image/png").build();
    }


    public static String generateResponseHtml(String swc) throws IOException {
        return generateResponseHtml(swc, false);
    }

	public static String generateResponseHtml(String swc, boolean portable) throws IOException {
        //get js libs block
        String chunk = getResourceAsString(
                portable ? JS_LIBS_PORTABLE_FILE_NAME : JS_LIBS_LINKED_FILE_NAME
        );
        //insert js libs block
		chunk = getResourceAsString(SWC_VIEW_HTML_TEMPLATE_FILE_NAME)
                .replace("%JS_LIBS_PLACEHOLDER%", chunk);
        //repalce other placeholders
		return chunk.replace("%SWC_CONTENT_PLACEHOLDER%", swc)
		    .replace("%SWC_SERVICE_PLACEHOLDER%", config.getServiceUrl()
            );
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

	private static String getResourceAsString(String fileName) throws IOException {
		InputStream stream = new RestProcessUtils().getClass().getClassLoader()
				.getResourceAsStream(fileName);
		return getInputStreamAsString(stream);
	}

	private static String getInputStreamAsString(InputStream stream) throws IOException {
        Closer closer = Closer.create();
        closer.register(stream);
        String string = null;
        try {
            string = CharStreams.toString(new InputStreamReader(stream, "UTF-8"));
        } catch (Throwable e) {
            closer.rethrow(e);
        } finally {
            closer.close();
        }
        return string;
	}


    // get only first processed file!
    public static FileItem getFirstFileItem(List<FileItem> fileItems) throws IOException {

        for (FileItem fileItem : fileItems) {
            if (fileItem.isFormField()) {
                LOGGER.debug("fileItem.getFieldName():" + fileItem.getFieldName());
                LOGGER.debug("value:" + fileItem.getString());
            }
        }


        for (FileItem fileItem : fileItems) {
            if (!fileItem.isFormField()) {
                return fileItem;
            }
        }
        return null;
    }


}
