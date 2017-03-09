package fuse.qe.tools.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import fuse.qe.tools.model.TestExceptionDTO;

/**
 * Created by sveres on 3/8/17.
 */
public class ParseUtils {

	private static final String REGEX_NORMAL = ".*at (.*)\\.[A-Z].*";
	private static final String REGEX_MORE = "... \\d+ more";
	private static final String REGEX_CAUSED = "Caused by.*";

	public List<String> getChainOfPackages(TestExceptionDTO exc) {

		List<String> zoz = new ArrayList<String>();

		final List<List<String>> sum = new ArrayList<List<String>>();

		final String est = exc.getErrorStackTrace();

		final String[] lines = this.splitString(est);

		sum.add(zoz);

//		System.out.println("LIST LENGTH lines : *" + lines.length + "*");

		for (String line : lines) {
			if (line.matches(REGEX_CAUSED)) {
				zoz = new ArrayList<String>();
				sum.add(zoz);
			} else if (line.matches(REGEX_MORE)) {
				//do nothing
			} else if (line.matches(REGEX_NORMAL)) {
				final String onePackage = line.replaceAll(REGEX_NORMAL, "$1");
				zoz.add(onePackage);
			}
		}

		final ListIterator li = sum.listIterator(sum.size());

//		Iterate in reverse.
		final List<String> ret = new ArrayList<String>();
		while (li.hasPrevious()) {

			final List<String> ls = (List<String>) li.previous();
//			System.out.println("LENGTH OF SUB_LIST: *" + ls.size() + "*");
			ret.addAll(ls);
		}

//		System.out.println("LENGTH OF FINAL LIST : *" + ret.size() + "*");
		return ret;
	}

	private String[] splitString(String str) {

		final String[] lines = str.split("\\r?\\n");

		return lines;
	}
}
