package fuse.qe.tools;

import java.io.IOException;

import fuse.qe.tools.utils.ElasticClientUtils;

public final class ElasticApp {

	private static final String INDEX_NAME = "error_db";

	private ElasticApp() {
	}

	public static void main(String[] args) throws IOException {
		try {
			ElasticClientUtils elasticClientUtils = new ElasticClientUtils("http://localhost:9200", INDEX_NAME);

			elasticClientUtils.checkAgaintsClassifiedData("/home/lvydra/Stažené/error_stack_tace.csv", 5, "98%");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
