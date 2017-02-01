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
	
  public ElasticClientUtils(String url, Integer port, String user, String pwd, String indexName) {

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

	public ElasticClientUtils(String urlWithPort, String indexName) {
		this.indexName = indexName;

		clientConfig = new HttpClientConfig.Builder(urlWithPort)
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

		JestResult result = bscOps.queryData(indexName, TYPE_NAME, query);

		List<TestExceptionDTO> exceptions = result.getSourceAsObjectList(TestExceptionDTO.class);

		if (exceptions.isEmpty()) {
			return -1;
		}

		TestExceptionDTO exception = exceptions.get(0);
		String groupId = exception.getGroup_id();
		
		if (groupId == null || groupId.isEmpty()) {
			return -1;
		}

		checkResults(excdto.getId(), excdto.getGroup_id(), groupId, exceptions);

		return Integer.valueOf(groupId);
	}

	public void indexData(List<Object> sources) throws Exception {
		bscOps.indexDataBulk(indexName, TYPE_NAME, sources);
	}
	
	public void deleteIndex() throws Exception {
		bscOps.deleteIndex(indexName);
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

	private void checkResults(String exceptionId, String exceptionGroupId, String groupId, List<TestExceptionDTO> similarFounds) throws Exception {
		boolean numberMatch = true;
		boolean consistentFind = true;
		
		QueryBuilder query = QueryBuilders.matchPhraseQuery(GROUP_ID, groupId);

		JestResult result = bscOps.queryData(indexName, TYPE_NAME, query);

		List<TestExceptionDTO> exceptions = result.getSourceAsObjectList(TestExceptionDTO.class);
		
		int exceptionsSize = exceptions.size();
		int similarFoundsSize = similarFounds.size();
		
		if (exceptionsSize != similarFoundsSize) {
			numberMatch = false;
		}
		
		StringBuilder checkOutput = new StringBuilder("Number of records: ");
		
		checkOutput.append(exceptions.size());
		checkOutput.append(" found as similar: ");
		checkOutput.append(similarFounds.size()); 

		for (TestExceptionDTO similarFound : similarFounds) {
			String foundId = similarFound.getGroup_id();
			if (!foundId.equals(groupId)) {
				checkOutput.append(" Wrong id found.");
				consistentFind = false;
				break;
			}
		}
		
		if (consistentFind) {
			checkOutput.append(" All ids matched.");
		}
		
		checkOutput.append(" For |exception id: ");
		checkOutput.append(exceptionId);
		checkOutput.append("|exception group id: ");
		checkOutput.append(exceptionGroupId);
		checkOutput.append("|group id: ");
		checkOutput.append(groupId);
		checkOutput.append("|");
		
		if (numberMatch && consistentFind) {
			System.out.println(checkOutput.toString());
		} else {
			System.err.println(checkOutput.toString());
		}
	}
	
	public void checkAgaintsClassifiedData(String path, Integer differencen, String similarity) throws Exception {
		String testIndexName = indexName + "_test";

		List<Object> sources = readExceptionsFromCsv(path);

		bscOps.indexDataBulk(testIndexName, TYPE_NAME, sources);

		Thread.sleep(2000);

		for (Object source : sources) {
			TestExceptionDTO record = (TestExceptionDTO) source;

			findGroupId(record, differencen, similarity);
		}

		bscOps.deleteIndex(testIndexName);
	}
	
	public Boolean checkAndRepair(TestExceptionDTO excdto) throws Exception {
		Boolean repair = false;
		
		QueryBuilder query = QueryBuilders.matchPhraseQuery(ID, excdto.getId());
		  
		JestResult result = bscOps.queryData(indexName, TYPE_NAME, query);
		 
		List<TestExceptionDTO> exceptions = result.getSourceAsObjectList(TestExceptionDTO.class);
		
		if (exceptions.isEmpty()) {
			repair = true;
		} else {
			TestExceptionDTO exception = exceptions.get(0);
			
			String groupId = exception.getGroup_id();
			String excdtoGroupId = excdto.getGroup_id();
			
			if (!groupId.equals(excdtoGroupId)) {
				repair = true;
			} else {
				String statckTrace = exception.getError_stack_trace();
				String excdtoStackTrace = excdto.getError_stack_trace();
				
				if (!statckTrace.equals(excdtoStackTrace)) {
					repair = true;
				}
			}
		}

		if (repair) {
			try {
				updateElasticDB(excdto);
				return true;
			} catch (Exception e) {
				return false;
			}
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
