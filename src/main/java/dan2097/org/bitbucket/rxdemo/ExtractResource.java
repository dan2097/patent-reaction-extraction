package dan2097.org.bitbucket.rxdemo;

import nu.xom.Attribute;
import nu.xom.Element;

import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;


/**
 * @author sea36
 * @author dmj30
 */
public class ExtractResource extends ServerResource {

    @Post("form:xml")
    public Representation doForm(Form form) {
    	String experimentalParsingErrorMessage ="";
    	try{
	    	Element heading = new Element("heading");
	        heading.addAttribute(new Attribute("title", form.getFirstValue("heading")));
	        Element para = new Element("p");
	        para.appendChild(form.getFirstValue("body"));
	    	heading.appendChild(para);
//	    	ExperimentParser parser = null;
//	    	try {
//				parser = new ExperimentParser(heading);
//			} catch (Exception e) {
//				experimentalParsingErrorMessage=e.getMessage();
//				e.printStackTrace();
//			}
			//String html = HtmlGenerator.generateHtml(parser.getReaction());
	    	String html="";
			return new StringRepresentation(html, MediaType.TEXT_HTML, null, CharacterSet.UTF_8);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    		throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Parsing error message: " + experimentalParsingErrorMessage + ". Other error message: "+ e.getMessage());
		}
    	
//    	String heading = form.getFirstValue("heading");
//        String body = form.getFirstValue("body");
//        return new StringRepresentation("heading: "+heading, MediaType.TEXT_PLAIN);
    }


}
