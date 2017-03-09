package fuse.qe.tools.dao;

import java.io.StringReader;
import java.util.LinkedList;

import fuse.qe.tools.deptree.Parser;
import fuse.qe.tools.deptree.Node;
import fuse.qe.tools.model.DependencyTreeDTO;
import io.searchbox.client.JestClient;

public class DependencyTreeElasticsearchDAO extends AbstractElasticsearchDAO {
	
	private static final String TYPE_NAME = "leaf";
	
	public DependencyTreeElasticsearchDAO(JestClient jestClient, String indexName) {
		super(jestClient, indexName, TYPE_NAME);
	}

	public void indexTreeData(String content) throws Exception {
		content = cleanTree(content);
		
		StringReader stringReader = new StringReader(content);
		Parser parser = new Parser();
		Node tree = parser.parse(stringReader);
		
		indexLeafs("", tree, tree.getArtifactId());
	}

	private void indexLeafs(String parentPath, Node node, String rootArtifact) throws Exception {
		String path = parentPath + "/" + node.getArtifactId();
		
		DependencyTreeDTO dependencyTreeDTO = new DependencyTreeDTO(path, node.getArtifactId(), rootArtifact);
		bscOps.indexData(indexName, TYPE_NAME, dependencyTreeDTO);
		
		LinkedList<Node> childNodes = node.getChildNodes();
		
		for (Node childNode : childNodes) {
			indexLeafs(path, childNode, rootArtifact);
		}
	}

	public String cleanTree(String content) {
		int index = content.indexOf("--- maven-dependency-plugin");
		content = content.substring(index);
		index = content.indexOf("\n")+1;
		content = content.substring(index);
		content = content.replaceAll("\\[INFO\\] ", "");
		
		return content;
	}
}
