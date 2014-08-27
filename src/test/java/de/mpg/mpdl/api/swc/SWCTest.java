package de.mpg.mpdl.api.swc;

import de.mpg.mpdl.api.swc.ServiceConfiguration.Pathes;
import de.mpg.mpdl.api.swc.process.RestProcessUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly2.servlet.GrizzlyWebContainerFactory;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;


public class SWCTest extends JerseyTest{

    final static String SWC_TEST_FILE_NAME = "HB060602_3ptSoma.swc";
    static String SWC_CONTENT = null;
    static String SWC_URL = null;
    static FormDataMultiPart SWC_MULTIPART = null;

    final static MediaType PNG_MEDIA_TYPE = new MediaType("image", "png");

    @Override
    protected Application configure() {
        return new MyApplication();
    }



   @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {

        return new TestContainerFactory() {
            @Override
            public TestContainer create(final URI baseUri, DeploymentContext deploymentContext) throws IllegalArgumentException {
                return new TestContainer() {
                    private HttpServer server;

                    @Override
                    public ClientConfig getClientConfig() {
                        return null;
                    }

                    @Override
                    public URI getBaseUri() {
                        return baseUri;
                    }

                    @Override
                    public void start() {
                        try {
                            this.server = GrizzlyWebContainerFactory.create(
                                    baseUri, Collections.singletonMap("jersey.config.server.provider.packages", "de.mpg.mpdl.api.swc")
                            );
                        } catch (ProcessingException e) {
                            throw new TestContainerException(e);
                        } catch (IOException e) {
                            throw new TestContainerException(e);
                        }
                    }

                    @Override
                    public void stop() {
                        this.server.stop();
                    }
                };

            }

        };
    }

    @BeforeClass
    public static void loadSwcFileFromResources() {

        FileDataBodyPart filePart = null;
        try {
            SWC_CONTENT = RestProcessUtils.getResourceAsString(SWC_TEST_FILE_NAME);
            final URI uri = RestProcessUtils.getResourceAsURL(SWC_TEST_FILE_NAME).toURI();
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

    }

    @Test
    public void testTextareaViewIn3D() throws IOException {
        testTextarea(new Form()
                        .param("portable", "false"),
                Pathes.PATH_VIEW,
                MediaType.TEXT_HTML_TYPE
        );
    }

    @Test
    public void testTextareaViewIn3DPortable() throws IOException {
        testTextarea(new Form()
                        .param("portable", "true"),
                Pathes.PATH_VIEW,
                MediaType.TEXT_HTML_TYPE
        );
    }

    @Test
    public void testTextareaGenerateThumbnail() throws IOException {

        testTextarea(new Form()
                        .param("portable", "false"),
                Pathes.PATH_THUMB,
                PNG_MEDIA_TYPE
        );

    }

    @Test
    public void testTextareaAnalyze() throws IOException {

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

    @Test
    public void testUrlViewIn3D() throws IOException {
        testUrl(
                target(Pathes.PATH_VIEW)
                .queryParam("portable", "false"),
                MediaType.TEXT_HTML_TYPE
        );
    }



    @Test
    public void testUrlViewIn3DPortable() throws IOException {
        testUrl(
                target(Pathes.PATH_VIEW)
                .queryParam("portable", "true"),
                MediaType.TEXT_HTML_TYPE
        );
    }

    @Test
    public void testUrlGenerateThumbnail() throws IOException {
        testUrl(
                target(Pathes.PATH_THUMB)
                .queryParam("portable", "false"),
                PNG_MEDIA_TYPE
        );
    }

    @Test
    public void testUrlAnalyze() throws IOException {

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


    @Test
    public void testFileViewIn3D() throws IOException {

        testFile(SWC_MULTIPART
                        .field("portable", "false"),
                Pathes.PATH_VIEW,
                MediaType.TEXT_HTML_TYPE
        );
    }

    @Test
    public void testFileViewIn3DPortable() throws IOException {

        testFile(SWC_MULTIPART
                        .field("portable", "true"),
                Pathes.PATH_VIEW,
                MediaType.TEXT_HTML_TYPE
        );
    }

    @Test
    public void testFileGenerateThumbnail() throws IOException {

        
        testFile(SWC_MULTIPART
                        .field("portable", "false"),
                Pathes.PATH_THUMB,
                PNG_MEDIA_TYPE
        );
    }

    @Test
    public void testFileAnalyze() throws IOException {

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

        assertEquals(200, response.getStatus());
        assertThat(response.readEntity(String.class), not(isEmptyOrNullString()));

    }



}