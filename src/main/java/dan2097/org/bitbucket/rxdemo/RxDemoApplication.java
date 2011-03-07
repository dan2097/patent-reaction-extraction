package dan2097.org.bitbucket.rxdemo;

import java.io.File;
import java.io.IOException;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

/**
 * @author sea36
 */
public class RxDemoApplication extends Application {

	public RxDemoApplication() {
	     this.setStatusService(new ReactionExtractionStatusService());
	}
    @Override
    public Restlet createInboundRoot() {
        Router router = new Router();
        router.attach("/extract", ExtractResource.class);
        router.attach("/", DefaultResource.class);
        return router;
    }

    public static void main(String[] args) throws Exception {
        Component c = new Component();
        c.getClients().add(Protocol.FILE);
        c.getServers().add(Protocol.HTTP, 8182);
        c.getDefaultHost().attachDefault(new RxDemoApplication());
        c.getDefaultHost().attach("/images/", application);  
        c.start();
    }
    
    static Application application = new Application() {
    	@Override  
    	public Restlet createRoot() {
    		String pathToTempFolder="";
			try {
				pathToTempFolder = File.createTempFile("foo", ".tmp").getParent();
			} catch (IOException e) {
				e.printStackTrace();
			}
    		return new Directory(getContext(), "file:///" +pathToTempFolder);  
    	}  
    }; 
    
}
