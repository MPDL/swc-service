package de.mpg.mpdl.service.rest.swc;

import de.mpg.mpdl.service.rest.swc.ServiceConfiguration.Pathes;
import de.mpg.mpdl.service.rest.swc.process.LMeasure;
import de.mpg.mpdl.service.rest.swc.process.RestProcessUtils;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static javax.ws.rs.core.Response.Status;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SWCTest extends JerseyTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(SWCTest.class);


//    final static String SWC_TEST_FILE_NAME = "HB060602_3ptSoma.swc";
    final static String SWC_TEST_FILE_NAME = "HB060602_3ptSoma_short.swc";
    final static String ANALYZE_TEST_FILE_NAME = "analyze_response.json";
    static String SWC_CONTENT = null;
    static String SWC_URL = null;
    static FormDataMultiPart SWC_MULTIPART = null;

    final static MediaType PNG_MEDIA_TYPE = new MediaType("image", "png");


    @Override
    protected Application configure() {
        return new MyApplication ();
    }


   @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new de.mpg.mpdl.service.rest.swc.MyTestContainerFactory();
    }

    /**
     * Initilize tests source file variables
     * */
    @BeforeClass
    public static void initilizeResources() throws IOException, URISyntaxException {

        //ping thumbnail service
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(new ServiceConfiguration().getScreenshotServiceUrl());
        Response response = target.request(MediaType.TEXT_HTML_TYPE).get();
        assertThat("Cannot access screenshot service", response.getStatus(),
                isOneOf(
                        Status.OK.getStatusCode(),
                        Status.FOUND.getStatusCode()
                )
        );
        response.close();

        //initilize all test file-related global variables
        FileDataBodyPart filePart = null;
        URI uri = null;
        try {
            SWC_CONTENT = RestProcessUtils.getResourceAsString(SWC_TEST_FILE_NAME);
            uri = RestProcessUtils.getResourceAsURL(SWC_TEST_FILE_NAME).toURI();
            SWC_URL = uri.toURL().toString();
            filePart = new FileDataBodyPart("file1", new File(uri));
            SWC_MULTIPART = new FormDataMultiPart();
            SWC_MULTIPART.bodyPart(filePart);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull("Cannot create URL to SWC file from resources: " + SWC_TEST_FILE_NAME, SWC_URL);
        assertNotNull("Cannot read SWC content from test resources: " + SWC_TEST_FILE_NAME, SWC_CONTENT);
        assertNotNull("Cannot create multipart body for SWC from test resources: " + SWC_TEST_FILE_NAME, filePart);

        //test L-Measure
        LMeasure lMeasure = new LMeasure();
        assertNotNull("Cannot execute L-Measure", lMeasure);
        lMeasure.execute(new File(uri), "", 0, false);
        assertThat("Wrong lmeasure processing: ", lMeasure.toJSON(),
            equalToIgnoringWhiteSpace((RestProcessUtils.getResourceAsString(ANALYZE_TEST_FILE_NAME).replaceAll("[\r\n]", ""))));
    }




    /**
     * Textarea tests
     * */
    @Test
    public void testTextareaViewIn3D() throws IOException {
        LOGGER.info("testTextareaViewIn3D...");
        testTextarea(new Form()
                        .param("portable", "false"),
                Pathes.PATH_VIEW,
                MediaType.TEXT_HTML_TYPE
        );
    }

    @Test
    public void testTextareaViewIn3DPortable() throws IOException {
        LOGGER.info("testTextareaViewIn3DPortable...");
        testTextarea(new Form()
                        .param("portable", "true"),
                Pathes.PATH_VIEW,
                MediaType.TEXT_HTML_TYPE
        );
    }

    @Test
    public void testTextareaGenerateThumbnail() throws IOException {
        LOGGER.info("testTextareaGenerateThumbnail...");
        testTextarea(new Form()
                        .param("portable", "false"),
                Pathes.PATH_THUMB,
                PNG_MEDIA_TYPE
        );

    }

    @Test
    public void testTextareaAnalyze() throws IOException {
        LOGGER.info("testTextareaAnalyze...");
        testTextarea(new Form()
                        .param("portable", "false")
                        .param("typeOfBins", "number")
                        .param("numberOfBins", "10"),
                Pathes.PATH_ANALYZE,
                MediaType.APPLICATION_JSON_TYPE
        );

        testTextarea(new Form()
                        .param("portable", "false")
                        .param("typeOfBins", "width")
                        .param("numberOfBins", "100"),
                Pathes.PATH_ANALYZE,
                MediaType.APPLICATION_JSON_TYPE
        );


    }

    /**
     * URL tests
     * */
    @Test
    public void testUrlViewIn3D() throws IOException {
        LOGGER.info("testUrlViewIn3D...");
        testUrl(
                target(Pathes.PATH_VIEW)
                .queryParam("portable", "false"),
                MediaType.TEXT_HTML_TYPE
        );
    }

    @Test
    public void testUrlViewIn3DPortable() throws IOException {
        LOGGER.info("testUrlViewIn3DPortable...");
        testUrl(
                target(Pathes.PATH_VIEW)
                .queryParam("portable", "true"),
                MediaType.TEXT_HTML_TYPE
        );
    }

    @Test
    public void testUrlGenerateThumbnail() throws IOException {
        LOGGER.info("testUrlGenerateThumbnail...");
        testUrl(
                target(Pathes.PATH_THUMB)
                .queryParam("portable", "false"),
                PNG_MEDIA_TYPE
        );
    }

    @Test
    public void testUrlAnalyze() throws IOException {
        LOGGER.info("testUrlAnalyze...");
        testUrl(
                target(Pathes.PATH_ANALYZE)
                .queryParam("portable", "false")
                .queryParam("typeOfBins", "number")
                .queryParam("numberOfBins", "10"),
                MediaType.APPLICATION_JSON_TYPE
        ); testUrl(
                target(Pathes.PATH_ANALYZE)
                .queryParam("portable", "false")
                .queryParam("typeOfBins", "width")
                .queryParam("numberOfBins", "100"),
                MediaType.APPLICATION_JSON_TYPE
        );

    }

    /**
     * File upload tests
     * */
    @Test
    public void testFileViewIn3D() throws IOException {
        LOGGER.info("testFileViewIn3D...");
        testFile(SWC_MULTIPART
                        .field("portable", "false"),
                Pathes.PATH_VIEW,
                MediaType.TEXT_HTML_TYPE
        );
    }

    @Test
    public void testFileViewIn3DPortable() throws IOException {
        LOGGER.info("testFileViewIn3DPortable...");
        testFile(SWC_MULTIPART
                        .field("portable", "true"),
                Pathes.PATH_VIEW,
                MediaType.TEXT_HTML_TYPE
        );
    }

    @Test
    public void testFileGenerateThumbnail() throws IOException {
        LOGGER.info("testFileGenerateThumbnail...");
        testFile(SWC_MULTIPART
                        .field("portable", "false"),
                Pathes.PATH_THUMB,
                PNG_MEDIA_TYPE
        );
    }

    @Test
    public void testFileAnalyze() throws IOException {
        LOGGER.info("testFileAnalyze...");
        testFile(SWC_MULTIPART
                        .field("portable", "false")
                        .field("typeOfBins", "number")
                        .field("numberOfBins", "10"),
                Pathes.PATH_ANALYZE,
                MediaType.APPLICATION_JSON_TYPE
        );
        testFile(SWC_MULTIPART
                        .field("portable", "false")
                        .field("typeOfBins", "width")
                        .field("numberOfBins", "100"),
                Pathes.PATH_ANALYZE,
                MediaType.APPLICATION_JSON_TYPE
        );

    }

    // HELPERS
    public void testTextarea(Form form, String path, MediaType responseMediaType) throws IOException {

        form.param("swc", SWC_CONTENT);

        Response response = target(path)
                .request(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(responseMediaType)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        assertEquals(200, response.getStatus());
        assertThat(response.readEntity(String.class), not(isEmptyOrNullString()));
    }

    private void testUrl(WebTarget webTarget, MediaType responseMediaType) {

        Response response = webTarget
                .queryParam("url", SWC_URL)
                .request(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(responseMediaType)
                .get();
        assertEquals(200, response.getStatus());
        assertThat(response.readEntity(String.class), not(isEmptyOrNullString()));

    }

    private void testFile(FormDataMultiPart multipart, String path, MediaType responseMediaType) {


        Response response = target(path)
                .register(MultiPartFeature.class)
                .request(MediaType.MULTIPART_FORM_DATA_TYPE)
                .accept(responseMediaType)
                .post(Entity.entity(multipart, multipart.getMediaType()));

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        assertThat(response.readEntity(String.class), not(isEmptyOrNullString()));

    }



}