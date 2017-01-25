package fuse.qe.tools.utils;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

	private String indexName;

	private static final String TYPE_NAME = "error";
	
	private static final String NAME = "error_stack_trace";
	
	private static final String GROUP_ID = "group_id";
	
	private static final String ID = "id";
	
	public ElasticClientUtils(String url, int port, String user, String pwd, String indexName) {
		this.indexName = indexName;
		
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
	
	public ElasticClientUtils(String url, String indexName) {
		this.indexName = indexName;
		
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
	 */
	public void updateElasticDB(TestExceptionDTO excdto) throws Exception {
		bscOps.indexData(indexName, TYPE_NAME, excdto);
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
	 */
	public Integer findGroupId(TestExceptionDTO excdto, Integer difference, String minimumShouldMatch) throws Exception {
		QueryBuilder query = QueryBuilders.matchQuery(NAME, excdto.getError_stack_trace()).slop(difference).minimumShouldMatch(minimumShouldMatch);
    	
		JestResult result = bscOps.queryData(indexName, TYPE_NAME, query, 100);
		
		List<TestExceptionDTO> exceptions = result.getSourceAsObjectList(TestExceptionDTO.class);

		if (exceptions.isEmpty()) {
			return -1;
		}

		TestExceptionDTO exception = exceptions.get(0);
		String groupId = exception.getGroup_id();

    checkResults(groupId, exceptions);
		
		return Integer.valueOf(groupId);
	}

	public void indexData(List<Object> sources) throws Exception {
		bscOps.indexDataBulk(indexName, TYPE_NAME, sources);
	}

	public void indexDataFromCsv(String path) throws Exception {
		List<Object> sources = readExceptionsFromCsv(path);
    
		bscOps.indexDataBulk(indexName, TYPE_NAME, sources);
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
		
		JestResult result = bscOps.queryData(indexName, TYPE_NAME, query, 100);
		
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
	
	public Boolean checkAndRepair(TestExceptionDTO excdto) throws Exception {
		QueryBuilder query = QueryBuilders.matchPhraseQuery(ID, excdto.getId());
		  
		JestResult result = bscOps.queryData(indexName, TYPE_NAME, query);
		 
		List<TestExceptionDTO> exceptions = result.getSourceAsObjectList(TestExceptionDTO.class);
		
		if (exceptions.isEmpty()) {
			return false;
		}
		  
		TestExceptionDTO exception = exceptions.get(0);
		  
		String groupId = exception.getGroup_id();
		String excdtoGroupId = excdto.getGroup_id();
		
		if (!groupId.equals(excdtoGroupId)) {
			return false;
		}
		  
		String statckTrace = exception.getError_stack_trace();
		String excdtoStackTrace = excdto.getError_stack_trace();
		
		if (!statckTrace.equals(excdtoStackTrace)) {
			return false;
		}
		  
		return true;
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
}