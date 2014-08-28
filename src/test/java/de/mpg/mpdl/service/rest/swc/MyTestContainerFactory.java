package de.mpg.mpdl.service.rest.swc;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly2.servlet.GrizzlyWebContainerFactory;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import javax.ws.rs.ProcessingException;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;

/**
 * The class overrides TestContainerFactory with GrizzlyWebContainerFactory. The GrizzlyWebContainer fully
 * supports HttpServlet, that is needed to test @Context injection variables in tests
*
 * @author vmakarenko
 *
* */
public class MyTestContainerFactory implements TestContainerFactory {
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
                            baseUri, Collections.singletonMap("jersey.config.server.provider.packages", "de.mpg.mpdl.service.rest.swc")
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

}
