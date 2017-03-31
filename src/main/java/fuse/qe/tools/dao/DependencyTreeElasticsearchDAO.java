package fuse.qe.tools.dao;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import fuse.qe.tools.deptree.Node;
import fuse.qe.tools.deptree.Parser;
import fuse.qe.tools.model.DependencyTreeDTO;
import fuse.qe.tools.utils.ParseUtils;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;

public class DependencyTreeElasticsearchDAO extends AbstractElasticsearchDAO {
	
	private static final String TYPE_NAME = "leaf";
	private static final String ARTIFACT_NAME = "artifact";
	private static final String GROUP_ID_NAME = "groupId";
	private static final String REGEX_VALID_LINE = "^\\s*[+|\\\\].*";
	private static final String REGEX_FIRST_LINE = "^[+].*";
	
	public DependencyTreeElasticsearchDAO(JestClient jestClient, String indexName) {
		super(jestClient, indexName, TYPE_NAME);
	}
	
	public List<DependencyTreeDTO> getLeafsByArtifact(String artifact) throws Exception {
		QueryBuilder query = QueryBuilders.matchQuery(ARTIFACT_NAME, artifact);
		
		JestResult result = bscOps.queryData(indexName, TYPE_NAME, query);

		List<DependencyTreeDTO> leafs = result.getSourceAsObjectList(DependencyTreeDTO.class);
		
		return leafs;
	}
	
	public List<DependencyTreeDTO> getLeafsByGroupId(String groupId) throws Exception {
		QueryBuilder query = QueryBuilders.matchQuery(GROUP_ID_NAME, groupId);
		
		JestResult result = bscOps.queryData(indexName, TYPE_NAME, query);

		List<DependencyTreeDTO> leafs = result.getSourceAsObjectList(DependencyTreeDTO.class);
		
		return leafs;
	}

	public void indexTreeData(String content) throws Exception {
		List<String> forrest = cleanForrest(content);
		
		for (String forrestTree : forrest) {
			StringReader stringReader = new StringReader(forrestTree);
			Parser parser = new Parser();
			Node tree = parser.parse(stringReader);
			
			try {
				indexLeafs("", "", tree, tree.getArtifactId());
			} catch (Exception e) {
				System.out.println(forrestTree);
				System.out.println(e);
			}
		}
	}

	private void indexLeafs(String parentPath, String parentGroupIdPath, Node node, String rootArtifact) throws Exception {
		String path = parentPath + "/" + node.getArtifactId();
		String groupIdPath = parentGroupIdPath + "/" + node.getGroupId();
		
		DependencyTreeDTO dependencyTreeDTO = new DependencyTreeDTO(path, groupIdPath, node.getArtifactId(), node.getGroupId(), rootArtifact);
		bscOps.indexData(indexName, TYPE_NAME, dependencyTreeDTO);
		
		LinkedList<Node> childNodes = node.getChildNodes();
		
		for (Node childNode : childNodes) {
			indexLeafs(path, groupIdPath, childNode, rootArtifact);
		}
	}

	public List<String> cleanForrest(String content) {
		String cleanTree;
		List<String> forrest = new ArrayList<String>();

		content = content.replaceAll("\\[INFO\\] ", "");

		String[] lines = ParseUtils.splitString(content);
		
		StringBuilder cleanTreeBuilder = new StringBuilder();
		
		for (int i = 0; i < lines.length-1; i++) {
			String line = lines[i]; 
			String nextLine = lines[i+1];
			if (line.matches(REGEX_VALID_LINE)) {
				cleanTreeBuilder.append(line);
				cleanTreeBuilder.append(System.getProperty("line.separator"));
			} else if (nextLine.matches(REGEX_FIRST_LINE)) {
				cleanTree = cleanTreeBuilder.toString();
				if (!cleanTree.isEmpty()) {
					forrest.add(cleanTree);
				}

				cleanTreeBuilder = new StringBuilder();
				cleanTreeBuilder.append(line);
				cleanTreeBuilder.append(System.getProperty("line.separator"));
			}
		}
		
		cleanTree = cleanTreeBuilder.toString();
		if (!cleanTree.isEmpty()) {
			forrest.add(cleanTree);
		}
		
		return forrest;
	}
}
