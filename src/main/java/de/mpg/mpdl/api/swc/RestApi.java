package de.mpg.mpdl.api.swc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
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

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import de.mpg.mpdl.api.swc.ServiceConfiguration.Pathes;
import de.mpg.mpdl.api.swc.process.LMeasure;
import de.mpg.mpdl.api.swc.process.RestProcessUtils;

@Singleton
@Path("/")
public class RestApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestApi.class);


    @GET
    @Path("hello")
    public String getHello() {
        return "Hello World!";
    }

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
	public Response getViewFromFiles(@Context HttpServletRequest request
	) throws IOException, FileUploadException {
        return RestProcessUtils.generateViewFromFiles(request);
	}

	@POST
	@Path(Pathes.PATH_VIEW)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	public Response getViewFromTextarea(@FormParam("swc") String swc,
			@DefaultValue("false") @FormParam("portable") boolean portable)
			throws IOException {
		return RestProcessUtils.generateViewFromTextarea(swc, portable);
	}

	/*
	 * /view{?portable=true}
	 */
	@GET
	@Path(Pathes.PATH_VIEW)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.TEXT_HTML)
	public Response getViewFromUrl(@QueryParam("url") String url,
			@DefaultValue("false") @QueryParam("portable") boolean portable)
			throws IOException {
        return RestProcessUtils.generateViewFromUrl(url, portable);

	}

	@POST
	@Path(Pathes.PATH_THUMB)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("image/png")
	public Response getThumbnailFromTextarea(@FormParam("swc") String swc)
			throws IOException {
		return RestProcessUtils.generateThumbnailFromTextarea(swc);
	}

	@POST
	@Path(Pathes.PATH_THUMB)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("image/png")
	public Response getThumbnailFromFiles(@Context HttpServletRequest request)
			throws IOException, FileUploadException {
		return RestProcessUtils.generateThumbnailFromFiles(request);
	}

	@GET
	@Path(Pathes.PATH_THUMB)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces("image/png")
	public Response getThumbnailFromUrl(@QueryParam("url") String url)
			throws IOException {
		return RestProcessUtils.generateThumbnailFromUrl(url);
	}

	@POST
	@Path(Pathes.PATH_ANALYZE)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAnalyzeFromTextarea(@FormParam("swc") String swc,
			@FormParam("numberOfBins") String numberOfBins,
			@FormParam("typeOfBins") String typeOfBins,
			@FormParam("query") String query) throws IOException {
		return RestProcessUtils.generateAnalyzeFromTextArea(swc, query, Integer
				.parseInt(numberOfBins), "width".equals(typeOfBins));
	}

	@POST
	@Path(Pathes.PATH_ANALYZE)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAnalyzeFromFiles(@Context HttpServletRequest request)
			throws IOException, FileUploadException {
		return RestProcessUtils.generateAnalyzeFromFiles(request);
	}

	@GET
	@Path(Pathes.PATH_ANALYZE)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAnalyzeFromUrl(@QueryParam("url") String url,
			@QueryParam("numberOfBins") String numberOfBins,
			@QueryParam("typeOfBins") String typeOfBins,
			@QueryParam("query") String query) throws IOException {
		return RestProcessUtils.generateAnalyzeFromUrl(url, query, Integer
				.parseInt(numberOfBins), "width".equals(typeOfBins));
	}
}
