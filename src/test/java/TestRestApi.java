import de.mpg.mpdl.service.rest.swc.process.RestProcessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by vlad on 9/26/14.
 */
@Singleton
@Path("/")
public class TestRestApi {

        private static final Logger LOGGER = LoggerFactory.getLogger(TestRestApi.class);

        /**
         * The static explain is resolved by UrlRewriteRule
         *
         * @GET @Path(Pathes.PATH_EXPLAIN)
         * @Produces(MediaType.TEXT_HTML) public Response getExplain() { return
         *                                RestProcessUtils.getExplain(); }
         */

        @GET
        @Path("/screenshot")
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Produces(MediaType.TEXT_HTML)
        public Response testScreenshot()
                {
            return RestProcessUtils.buildHtmlResponse("HALLOOO!");

        }

    }



