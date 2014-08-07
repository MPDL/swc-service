package de.mpg.mpdl.api.swc;

import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.spi.resource.Singleton;

import de.mpg.mpdl.api.swc.ServiceConfiguration.Pathes;
import de.mpg.mpdl.api.swc.process.RestProcessUtils;

@Singleton
@Path("/")
public class RestApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestApi.class);
	private ServiceConfiguration config = new ServiceConfiguration();

	/**
	 * The static explain is resolved by UrlRewriteRule
	 * 
	 * @GET @Path(Pathes.PATH_EXPLAIN)
	 * @Produces(MediaType.TEXT_HTML) public Response getExplain() { return
	 *                                RestProcessUtils.getExplain(); }
	 */
	@POST
	@Path(Pathes.PATH_VIEW)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	public Response getViewFromFiles(@Context HttpServletRequest request) {
		Status status = Status.OK;
		String htmlStr;

		try {
			htmlStr = RestProcessUtils
					.generateHtmlRepresentationFromFiles(request);
		} catch (Exception e) {
			status = Status.INTERNAL_SERVER_ERROR;
			htmlStr = "<html><body><h1>Something wrong by SWC rendering:</h1>"
					+ "<p>" + Arrays.toString(e.getStackTrace()) + "</p>"
					+ "</body></html>";
		}
		return RestProcessUtils.buildHtmlResponse(htmlStr, status);
	}

	@POST
	@Path(Pathes.PATH_VIEW)
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.TEXT_HTML)
	public Response getViewFromSingleFile(@Context HttpServletRequest request)
			throws IOException {
		Status status = Status.OK;
		String htmlStr = RestProcessUtils.generateHtmlRepresentation(IOUtils
				.toString(request.getInputStream()));
		return RestProcessUtils.buildHtmlResponse(htmlStr, status);
	}

	@POST
	@Path(Pathes.PATH_VIEW)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	public Response getViewFromTextarea(@FormParam("swc") String swc) {
		String htmlStr = RestProcessUtils.generateHtmlRepresentation(swc);
		return RestProcessUtils.buildHtmlResponse(htmlStr);
	}

	@GET
	@Path(Pathes.PATH_VIEW)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	public Response getViewFromUrl(@QueryParam("url") String url) {
		String htmlStr = "<html><body><h1>Rendered SWC</h1></body></html>";
		LOGGER.info(".swc url: " + url);
		// TODO: url resolution to be implemented!!!
		return RestProcessUtils.buildHtmlResponse(htmlStr);
	}

	@POST
	@Path(Pathes.PATH_THUMB)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("image/png")
	public Response getThumbnail(@Context HttpServletRequest request) {
		try {
			List<FileItem> fileItems = RestProcessUtils.uploadFiles(request);
			LOGGER.info("files uploaded...");
			URLConnection screenshotConn = URI
					.create(config.getScreenshotServiceUrl()).toURL()
					.openConnection();
			screenshotConn.setDoOutput(true);
			URLConnection viewConn = URI
					.create(config.getServiceApiUrl() + Pathes.PATH_VIEW)
					.toURL().openConnection();
			viewConn.setDoOutput(true);
			viewConn.setRequestProperty("Content-Type",
					MediaType.APPLICATION_OCTET_STREAM);
			// Get only the 1st item (multiple item is not relevant so far)
			FileItem item = fileItems.get(0);
			// Send the content of the item to 3D view swc service
			IOUtils.copy(item.getInputStream(), viewConn.getOutputStream());
			// Send the response of the 3D view swc service to the screenshot
			// service
			IOUtils.copy(viewConn.getInputStream(),
					screenshotConn.getOutputStream());
			// Return the response of the screenshot service
			return Response
					.status(Status.OK)
					.entity(IOUtils.toByteArray(screenshotConn.getInputStream()))
					.type("image/png").build();

		} catch (Exception e) {
			String htmlStr = "<html><body><h1>Something wrong by SWC generating picture</h1>"
					+ "<p>"
					+ Arrays.toString(e.getStackTrace())
					+ "</p>"
					+ "</body></html>";
			return RestProcessUtils.buildHtmlResponse(htmlStr,
					Status.INTERNAL_SERVER_ERROR);
		}
	}
}
