package fuse.qe.tools.dao;

import java.util.List;

import fuse.qe.tools.BasicOperations;
import io.searchbox.client.JestClient;

public abstract class AbstractElasticsearchDAO {

	protected BasicOperations bscOps;

	protected String indexName;
	
	protected String typeName;

	public AbstractElasticsearchDAO (JestClient jestClient, String indexName, String typeName) {
		this.indexName = indexName;
		this.typeName = typeName;
		
		bscOps = new BasicOperations(jestClient);
	}

	public void indexData(List<Object> sources) throws Exception {
		bscOps.indexDataBulk(indexName, typeName, sources);
	}
	
	public void deleteIndex() throws Exception {
		bscOps.deleteIndex(indexName);
	}
	
	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public BasicOperations getBscOps() {
		return bscOps;
	}

	public void setBscOps(BasicOperations bscOps) {
		this.bscOps = bscOps;
	}
}
