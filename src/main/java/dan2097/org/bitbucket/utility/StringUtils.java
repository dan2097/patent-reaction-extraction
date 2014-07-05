package dan2097.org.bitbucket.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringUtils {
	
	/**
	 * Converts a list of strings into a single string delimited by the given separator
	 *
	 * @param list A list of strings.
	 * @param separator
	 * @return The corresponding string.
	 */
	public static String stringListToString(List<String> list, String separator) {
		StringBuilder sb = new StringBuilder();
		int lastIndexOfList = list.size() - 1;
		for (int i = 0; i < lastIndexOfList; i++) {
			sb.append(list.get(i));
			sb.append(separator);
		}
		if (lastIndexOfList >= 0){
			sb.append(list.get(lastIndexOfList));
		}
		return sb.toString();
	}
	

	/**Converts a string array to an ArrayList.
	 *
	 * @param array The array.
	 * @return The ArrayList.
	 */
	public static List<String> arrayToList(String [] array) {
		List<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(array));
		return list;
	}

	/**
	 * Tests if this string start with the specified prefix ignoring case.
	 * @param str
	 * @param prefix
	 * @return
	 */
	public static boolean startsWithCaseInsensitive(String str, String prefix) {
		return str.regionMatches(true, 0, prefix, 0, prefix.length());
	}
	
	/**
	 * Tests if this string ends with the specified suffix ignoring case.
	 * @param str
	 * @param suffix
	 * @return
	 */
	public static boolean endsWithCaseInsensitive(String str, String suffix) {
		if (suffix.length() > str.length()) {
			return false;
		}
		int strOffset = str.length() - suffix.length();
		return str.regionMatches(true, strOffset, suffix, 0, suffix.length());
	}
}
