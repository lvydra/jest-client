package fuse.qe.tools.dao;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.opencsv.CSVReader;

import fuse.qe.tools.model.TestExceptionDTO;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;

public class TestExceptionElasticsearchDAO extends AbstractElasticsearchDAO {
	
	private static final String TYPE_NAME = "error";

	private static final String NAME = "errorStackTrace";

	private static final String GROUP_ID = "groupId";

	private static final String ID = "id";
	
	private static int reassignedGroupIds = 0;
	
	public TestExceptionElasticsearchDAO(JestClient jestClient, String indexName) {
		super(jestClient, indexName, TYPE_NAME);
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
		QueryBuilder query = QueryBuilders.matchQuery(NAME, excdto.getErrorStackTrace()).slop(difference).minimumShouldMatch(minimumShouldMatch);

		JestResult result = bscOps.queryData(indexName, TYPE_NAME, query);

		List<TestExceptionDTO> exceptions = result.getSourceAsObjectList(TestExceptionDTO.class);

		if (exceptions.isEmpty()) {
			return -1;
		}

		TestExceptionDTO exception = exceptions.get(0);
		String groupId = exception.getGroupId();
		
		if (groupId == null || groupId.isEmpty()) {
			return -1;
		}

		return Integer.valueOf(groupId);
	}
	
	public Integer findGroupId(TestExceptionDTO excdto, Integer difference, String minimumShouldMatch, boolean check) throws Exception {
		QueryBuilder query = QueryBuilders.matchQuery(NAME, excdto.getErrorStackTrace()).slop(difference).minimumShouldMatch(minimumShouldMatch);

		JestResult result = bscOps.queryData(indexName, TYPE_NAME, query);

		List<TestExceptionDTO> exceptions = result.getSourceAsObjectList(TestExceptionDTO.class);

		if (exceptions.isEmpty()) {
			return -1;
		}

		TestExceptionDTO exception = exceptions.get(0);
		String groupId = exception.getGroupId();
		
		if (groupId == null || groupId.isEmpty()) {
			return -1;
		}

		if (check) {
			checkResults(excdto.getId(), excdto.getGroupId(), groupId, exceptions);
		}

		return Integer.valueOf(groupId);
	}
	
	public void indexExceptionDataFromCsv(String path) throws Exception {
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
		boolean consistentFind = true;
		
		QueryBuilder query = QueryBuilders.matchPhraseQuery(GROUP_ID, groupId);

		JestResult result = bscOps.queryData(indexName, TYPE_NAME, query);

		List<TestExceptionDTO> exceptions = result.getSourceAsObjectList(TestExceptionDTO.class);
		
		int exceptionsSize = exceptions.size();
		int similarFoundsSize = similarFounds.size();
		
		StringBuilder checkOutput = new StringBuilder("Number of records: ");
		
		checkOutput.append(exceptionsSize);
		checkOutput.append(" found as similar: ");
		checkOutput.append(similarFoundsSize); 

		for (TestExceptionDTO similarFound : similarFounds) {
			String foundId = similarFound.getGroupId();
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
		
		if (exceptionGroupId.equals(groupId)) {
			System.out.println(checkOutput.toString());
		} else {
			System.err.println(checkOutput.toString());
			reassignedGroupIds++;
		}
	}
	
	public void checkAgaintsClassifiedData(String path, Integer differencen, String similarity) throws Exception {
		reassignedGroupIds = 0;
		
		//String testIndexName = indexName + "_test";

		List<Object> sources = readExceptionsFromCsv(path);

		bscOps.indexDataBulk(indexName, TYPE_NAME, sources);

		Thread.sleep(2000);

		for (Object source : sources) {
			TestExceptionDTO record = (TestExceptionDTO) source;

			findGroupId(record, differencen, similarity, true);
		}

		bscOps.deleteIndex(indexName);
		
		System.err.println(reassignedGroupIds + " exception group ids reasign.");
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
			
			String groupId = exception.getGroupId();
			String excdtoGroupId = excdto.getGroupId();
			
			if (!groupId.equals(excdtoGroupId)) {
				repair = true;
			} else {
				String statckTrace = exception.getErrorStackTrace();
				String excdtoStackTrace = excdto.getErrorStackTrace();
				
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
	
	public static int getReassignedGroupIds() {
		return reassignedGroupIds;
	}

	public static void setReassignedGroupIds(int reassignedGroupIds) {
		TestExceptionElasticsearchDAO.reassignedGroupIds = reassignedGroupIds;
	}
}
