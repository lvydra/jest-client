package com.kaw;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;

public class App {
	static BasicOperations basOp;
	
	private static final String TYPE_NAME = "records";
    private static final String INDEX_NAME = "record";
	
	public static void main(String args[]) throws IOException{
		try {
            HttpClientConfig clientConfig = new HttpClientConfig.Builder("http://localhost:9200").multiThreaded(true).build();
			
            JestClientFactory factory = new JestClientFactory();
            factory.setHttpClientConfig(clientConfig);
            JestClient jestClient = factory.getObject();

            BasicOperations bscOps = new BasicOperations(jestClient);
            
            final RecordDTO record1 = new RecordDTO("Record 1 - test 1");
            final RecordDTO record2 = new RecordDTO("Record 2 - test 2");
            final RecordDTO record3 = new RecordDTO("Record 3 - test 3");
            final RecordDTO record4 = new RecordDTO("Record 4 - test 4");
            
            List<Object> sources = new ArrayList<Object>();
            sources.add(record3);
            sources.add(record4);
            
            try {
            	bscOps.deleteIndex(INDEX_NAME);
            	
            	bscOps.createIndex(INDEX_NAME);
            	
            	bscOps.indexData(INDEX_NAME, TYPE_NAME, record1);
            	
            	bscOps.indexDataAsync(INDEX_NAME, TYPE_NAME, record2);
            	
            	bscOps.indexDataBulk(INDEX_NAME, TYPE_NAME, sources);
            	
            	Thread.sleep(2000);
            	
            	JestResult result = bscOps.readData(INDEX_NAME, TYPE_NAME, "record", "test");
            	
            	List<RecordDTO> records = result.getSourceAsObjectList(RecordDTO.class);
                for (RecordDTO record : records) {
                    System.out.println(record);
                }
            } finally {
                jestClient.shutdownClient();
            }

        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
}
