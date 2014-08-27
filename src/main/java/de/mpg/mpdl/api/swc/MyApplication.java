package de.mpg.mpdl.api.swc;

import javax.json.stream.JsonGenerator;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author vmakarenko
 */
public class MyApplication extends ResourceConfig {

    public MyApplication() {
        packages("de.mpg.mpdl.api.swc");
        register(LoggingFilter.class);
        register(MultiPartFeature.class);
        property(JsonGenerator.PRETTY_PRINTING, true);
    }
}
