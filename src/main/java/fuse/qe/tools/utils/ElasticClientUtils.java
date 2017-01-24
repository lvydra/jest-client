package fuse.qe.tools.utils;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.opencsv.CSVReader;

import fuse.qe.tools.BasicOperations;
import fuse.qe.tools.model.TestExceptionDTO;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;

/**
 * Created by sveres on 1/17/17.
 */
public class ElasticClientUtils {


	private JestClientFactory factory = new JestClientFactory();

	private HttpClientConfig clientConfig;

	private JestClient jestClient;

	private BasicOperations bscOps;

	private String typeName;

	private String indexName;
	
	private static final String NAME = "error_stack_trace";
	
	private static final String GROUP_ID = "group_id";
	
	public ElasticClientUtils(String url, int port, String user, String pwd, String indexName, String typeName) {
		this.indexName = indexName;
		this.typeName = typeName;
		
		BasicCredentialsProvider customCredentialsProvider = new BasicCredentialsProvider();
		customCredentialsProvider.setCredentials(
		        new AuthScope(url, port),
		        new UsernamePasswordCredentials(user, pwd)
		);
		
		clientConfig = new HttpClientConfig.Builder(url)
				.credentialsProvider(customCredentialsProvider)
				.multiThreaded(true)
				.build();

		factory.setHttpClientConfig(clientConfig);

		jestClient = factory.getObject();

		bscOps = new BasicOperations(jestClient);
	}
	
	public ElasticClientUtils(String url, String indexName, String typeName) {
		this.indexName = indexName;
		this.typeName = typeName;
		
		clientConfig = new HttpClientConfig.Builder(url)
				.multiThreaded(true)
				.build();

		factory.setHttpClientConfig(clientConfig);

		jestClient = factory.getObject();

		bscOps = new BasicOperations(jestClient);
	}

	/**
	 * This method will update ES database with new exception DTO.
	 * 
	 * @param excdto
	 * @return
	 * @throws Exception 
	 **/
	public void updateElasticDB(TestExceptionDTO excdto) throws Exception {
		bscOps.indexData(indexName, typeName, excdto);
	}

	/**
	 * Returns group id of that exception, via following algorithm:
	 * if the difference from the known exception (with group_id already specified)
	 * is < than e.g. 30 words, it is the same group_id, id there is no such exception (new exception case)
	 * it returns -1
	 * 
	 * @param excdto
	 * @return
	 * @throws Exception
	 **/
	public Integer findGroupId(TestExceptionDTO excdto, int difference, String minimumShouldMatch) throws Exception {
		QueryBuilder query = QueryBuilders.matchQuery(NAME, excdto.getError_stack_trace()).slop(difference).minimumShouldMatch(minimumShouldMatch);
    	
		JestResult result = bscOps.queryData(indexName, typeName, query, 100);
		
		List<TestExceptionDTO> exceptions = result.getSourceAsObjectList(TestExceptionDTO.class);
		
		TestExceptionDTO exception = exceptions.get(0);
		String groupId = exception.getGroup_id();
		
		checkResults(groupId, exceptions);
		
		return Integer.valueOf(groupId);
	}
	
	public void indexData(List<Object> sources) throws Exception {
		bscOps.indexDataBulk(indexName, typeName, sources);
	}
	
	public void indexDataFromCsv(String path) throws Exception {
		List<Object> sources = readExceptionsFromCsv(path);
		
		bscOps.indexDataBulk(indexName, typeName, sources);
	}
	
	public List<Object> readExceptionsFromCsv(String path) {
		List<Object> sources = new ArrayList<Object>();
		
		String csvFile = path;
		
		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(csvFile));
			String[] line;
			line = reader.readNext();
			while ((line = reader.readNext()) != null) {
				TestExceptionDTO exception = new TestExceptionDTO(line[0], line[1], line[2]);
				sources.add(exception);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		 
		return sources;
	}
	
	private void checkResults(String groupId, List<TestExceptionDTO> similarFounds) throws Exception {
		QueryBuilder query = QueryBuilders.matchPhraseQuery(GROUP_ID, groupId);
		
		JestResult result = bscOps.queryData(indexName, typeName, query, 100);
		
		List<TestExceptionDTO> exceptions = result.getSourceAsObjectList(TestExceptionDTO.class);
		
		System.out.println("Number of records: " + exceptions.size() + " found as similar: " + similarFounds.size());
		
		for (TestExceptionDTO similarFound : similarFounds) {
			String foundId = similarFound.getGroup_id();
			if (!foundId.equals(groupId)) {
				System.out.println("Wrong id found.");
				break;
			}
		}
		System.out.println("All group ids matched.");
	}
	
	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public JestClientFactory getFactory() {
		return factory;
	}

	public void setFactory(JestClientFactory factory) {
		this.factory = factory;
	}

	public void deleteIndex() throws Exception {
		bscOps.deleteIndex(indexName);
	}

	public HttpClientConfig getClientConfig() {
		return clientConfig;
	}

	public void setClientConfig(HttpClientConfig clientConfig) {
		this.clientConfig = clientConfig;
	}

	public JestClient getJestClient() {
		return jestClient;
	}

	public void setJestClient(JestClient jestClient) {
		this.jestClient = jestClient;
	}

	public BasicOperations getBscOps() {
		return bscOps;
	}

	public void setBscOps(BasicOperations bscOps) {
		this.bscOps = bscOps;
	}
	
	/** TODO 3. **/
	public static Boolean deepCheckAndRepair(TestExceptionDTO excdto) {
		
		return true;
	}
	
	/** TODO 4. **/
	public static Boolean shallowCheckAndRepair(TestExceptionDTO excdto) {
		
		return true;
	}

}
