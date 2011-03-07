package dan2097.org.bitbucket.rxdemo;

import org.apache.commons.io.IOUtils;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author sea36
 */
public class DefaultResource extends ServerResource {

    @Get("html")
    public Representation getIndex() throws IOException {
        InputStream is = getClass().getResourceAsStream("index.html");
        try {
            String s = IOUtils.toString(is);
            return new StringRepresentation(s, MediaType.TEXT_HTML);
        } finally {
            is.close();
        }
    }

}
