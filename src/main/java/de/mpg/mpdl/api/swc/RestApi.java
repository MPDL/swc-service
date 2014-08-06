package de.mpg.mpdl.api.swc;

import com.sun.jersey.spi.resource.Singleton;
import de.mpg.mpdl.api.swc.process.RestProcessUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.List;


@Singleton
@Path("/")
public class RestApi implements Pathes {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestApi.class);

    @GET
    @Path(Pathes.PATH_HELLO_WORLD)
	@Produces(MediaType.TEXT_HTML)
	public Response getHelloWorld() {
        return RestProcessUtils.buildHtmlResponse("Hello World!");
    }

    /**
     * The static explain
     * is resolved by UrlRewriteRule

	@GET @Path(Pathes.PATH_EXPLAIN)
	@Produces(MediaType.TEXT_HTML)
	public Response getExplain() {
        return RestProcessUtils.getExplain();
    }
    */

	@POST
    @Path(Pathes.PATH_VIEW)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public Response getViewFromFiles(
        @Context HttpServletRequest request
    ) {
        Status status = Status.OK;
        String htmlStr;

        try {
            htmlStr = RestProcessUtils.generateHtmlRepresentationFromFiles(request);
        } catch (Exception e) {
            status = Status.INTERNAL_SERVER_ERROR;
            htmlStr = "<html><body><h1>Something wrong by SWC rendering:</h1>" +
                   "<p>" +
                    Arrays.toString(e.getStackTrace()) +
                   "</p>" +
             "</body></html>";
        }
        return RestProcessUtils.buildHtmlResponse(htmlStr, status);
	}

    @POST
    @Path(Pathes.PATH_VIEW)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response getViewFromTextarea(
            @FormParam("swc") String swc
    ) {
        String htmlStr = RestProcessUtils.generateHtmlRepresentation(swc);
        return RestProcessUtils.buildHtmlResponse(htmlStr);
    }

    @GET
    @Path(Pathes.PATH_VIEW)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response getViewFromUrl(
            @QueryParam("url") String url
    ) {
        String htmlStr = "<html><body><h1>Rendered SWC</h1></body></html>";
        LOGGER.info(".swc url: " + url);
        //TODO: url resolution to be implemented!!!
        return RestProcessUtils.buildHtmlResponse(htmlStr);
    }



    //TODO: to be impelemented!!!
    @POST
    @Path(Pathes.PATH_THUMB)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("image/png")
    public Response getThumbnail(
            @Context HttpServletRequest request
    ) {
        Status status = Status.OK;
        String htmlStr = "<html><body><h1>Rendered SWC</h1></body></html>";

        try {
            List<FileItem> fileItems = RestProcessUtils.uploadFiles(request);
            LOGGER.info("files uploaded...");

            //here is the call of external service,
            //if success: filled
            // responseHtml = getSwcThumbnailRepresentation(fileItems);

        } catch (FileUploadException e) {
            status = Status.INTERNAL_SERVER_ERROR;
            htmlStr = "<html><body><h1>Something wrong by SWC rendering:</h1>" +
                    "<p>" +
                    Arrays.toString(e.getStackTrace()) +
                    "</p>" +
                    "</body></html>";

        }
        //TODO: entity is a png picture
        return RestProcessUtils.buildHtmlResponse(htmlStr, status);

    }


}
