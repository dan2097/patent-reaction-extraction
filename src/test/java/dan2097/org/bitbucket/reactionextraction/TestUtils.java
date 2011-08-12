package dan2097.org.bitbucket.reactionextraction;

import dan2097.org.bitbucket.utility.Utils;
import nu.xom.Element;

public class TestUtils {

	public static Element stringToXom(String xmlAsText) {
		try{
			return Utils.buildXmlFromString(xmlAsText).getRootElement();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
