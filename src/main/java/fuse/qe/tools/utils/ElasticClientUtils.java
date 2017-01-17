package fuse.qe.tools.utils;

import fuse.qe.tools.BasicOperations;
import fuse.qe.tools.model.TestExceptionDTO;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

/**
 * Created by sveres on 1/17/17.
 */
public abstract class ElasticClientUtils {

	private static final JestClientFactory factory = new JestClientFactory();

	private static HttpClientConfig clientConfig;

	private static JestClient jestClient;

	private static BasicOperations bscOps;

	private static final String TYPE_NAME = "error";

	private static final String INDEX_NAME = "error_db";



	//0.
	// This method will set connection of ES client to ES server.
	public static void setConnection(String url, String user, String pwd) {

		clientConfig = new HttpClientConfig.Builder(url).multiThreaded(true).build();

		factory.setHttpClientConfig(clientConfig);

		jestClient = factory.getObject();

		bscOps = new BasicOperations(jestClient);
	}


	//1.
	// this method will update ES database with new exception DTO.
	public static void updateElasticDB(TestExceptionDTO excdto) {

//		1.check whether this exception is already there
//		...

//		2.if not, put it into ES 'database'.

	}

//	2.
//	 Returns group id of that exception, via following algorithm:
//	if the difference from the known exception (with group_id already specified)
//	is < than e.g. 30 words, it is the same group_id, id there is no such exception (new exception case)
//	it returns -1
	public static Integer findGroupId(TestExceptionDTO excdto) {

		return -1;
	}

}
