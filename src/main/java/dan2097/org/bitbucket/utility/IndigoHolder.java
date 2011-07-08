package dan2097.org.bitbucket.utility;

import com.ggasoftware.indigo.Indigo;

public class IndigoHolder {
	private IndigoHolder() {}
 
	private static class SingletonHolder { 
		public static final Indigo INSTANCE = new Indigo();
	}
 
	public static Indigo getInstance() {
		return SingletonHolder.INSTANCE;
	}
}
