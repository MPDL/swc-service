package de.mpg.mpdl.api.swc;

import com.sun.jersey.core.header.ContentDisposition;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
import de.mpg.mpdl.api.swc.process.RestProcessGeneric;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.core.Response;


@Singleton
@Path("/")
public class RestApi implements Pathes {


	private static final Logger LOGGER = LoggerFactory.getLogger(RestApi.class);

    private static final String SERVER_UPLOAD_LOCATION_FOLDER = "/home/vlad/Desktop/upload/";

    private static final String SUCCESS_RESPONSE = "Successful";
    private static final String FAILED_RESPONSE = "Failed";



    @GET
    @Path(Pathes.PATH_HELLO_WORLD)
	@Produces(MediaType.TEXT_HTML)
	public Response getHelloWorld() {
        return RestProcessGeneric.hallo();
    }

    /**
     * The static explain
     * will be resolved by UrlRewriteRule

	@GET @Path(Pathes.PATH_EXPLAIN)
	@Produces(MediaType.TEXT_HTML)
	public Response getExplain() {
        return RestProcessGeneric.getExplain();
    }
    */

	@POST
    @Path(Pathes.PATH_VIEW)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public Response getView(
        @Context HttpServletRequest request
    ) {

        String responseStatus = SUCCESS_RESPONSE;


        if (ServletFileUpload.isMultipartContent(request))
        {
            final FileItemFactory factory = new DiskFileItemFactory();
            final ServletFileUpload fileUpload = new ServletFileUpload(factory);
            try
            {
                /*
                 * parseRequest returns a list of FileItem
                 * but in old (pre-java5) style
                 */
                final List items = fileUpload.parseRequest(request);

                if (items != null)
                {
                    final Iterator iter = items.iterator();
                    while (iter.hasNext())
                    {
                        final FileItem item = (FileItem) iter.next();
                        final String itemName = item.getName();

                        final File savedFile = new File(SERVER_UPLOAD_LOCATION_FOLDER + File.separator
                                + itemName);
                        LOGGER.info("Saving the file: " + savedFile.getName());
                        item.write(savedFile);

                    }
                }
            }
            catch (FileUploadException fue)
            {
                responseStatus = FAILED_RESPONSE;
                fue.printStackTrace();
            }
            catch (Exception e)
            {
                responseStatus = FAILED_RESPONSE;
                e.printStackTrace();
            }
        }


        /*RestProcessGeneric.uploadFile(fileInputStream, filePath);

        String output = "File saved to server location : " + filePath;
*/

        return Response.status(200).entity(responseStatus).build();

	}

	@GET
    @Path(Pathes.PATH_THUMB)
	@Produces("image/png")
	public Response getThumbnail() {
		return null;
	}

}
