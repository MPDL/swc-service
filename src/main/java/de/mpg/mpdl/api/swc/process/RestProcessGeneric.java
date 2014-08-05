package de.mpg.mpdl.api.swc.process;

import com.sun.jersey.core.header.FormDataContentDisposition;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.*;
import java.util.Iterator;
import java.util.List;

public class RestProcessGeneric {
	
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RestProcessGeneric.class);
	
	
	public static Response hallo() {
        LOGGER.info("HALLO!!!!");
        return buildHtml("Hello World!!!");
	}

    public static Response getExplain() {
        return  buildHtml("<html><body>" +
                "<h1>SWC Viewer</h1>" +
                "</body></html>");
    }

    private static Response buildHtml(String str) {
        return Response
                .status(Status.OK)
                .entity(str)
                .type(MediaType.TEXT_HTML)
                .build();
    }





    public static void uploadFile(InputStream uploadedInputStream,
                                  String serverLocation) {

        try {
            OutputStream outputStream;
            int read;
            byte[] bytes = new byte[1024];

            outputStream = new FileOutputStream(new File(serverLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
