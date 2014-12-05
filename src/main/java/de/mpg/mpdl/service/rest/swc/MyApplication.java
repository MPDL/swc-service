package de.mpg.mpdl.service.rest.swc;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.json.stream.JsonGenerator;

/**
 * @author vmakarenko
 */
public class MyApplication extends ResourceConfig {



    public MyApplication() {
        packages("de.mpg.mpdl.service.rest.swc");
//        register(LoggingFilter.class);
        register(MultiPartFeature.class);
//        register(new MyBinder());
        property(JsonGenerator.PRETTY_PRINTING, true);
    }

/*
    public static class  MyBinder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(RestProcessUtils.class).to(RestProcessUtils.class);
        }
    }
*/


}
