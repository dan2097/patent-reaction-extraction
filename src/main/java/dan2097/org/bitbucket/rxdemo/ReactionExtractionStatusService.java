package dan2097.org.bitbucket.rxdemo;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.service.StatusService;

public class ReactionExtractionStatusService extends StatusService{

	public ReactionExtractionStatusService() {
		super(true);
	}
	
	public Representation getRepresentation(Status status, Request request, Response response) {
		if (status.isError()){
			return new StringRepresentation(status.getDescription());
		} 
		return null;
	}
}
