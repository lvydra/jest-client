package fuse.qe.tools.utils;

import java.util.List;

import fuse.qe.tools.BasicOperations;

public abstract class AbstractElasticUtils {

	protected BasicOperations bscOps;

	protected String indexName;
	
	protected String typeName;

	public AbstractElasticUtils (BasicOperations bscOps, String indexName, String typeName) {
		this.bscOps = bscOps;
		this.indexName = indexName;
		this.typeName = typeName;
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
