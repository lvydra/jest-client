package fuse.qe.tools.model;

import java.io.Serializable;
import java.util.Date;

import io.searchbox.annotations.JestId;

public class DependencyTreeDTO implements Serializable {

	private static final long serialVersionUID = -4278657992975315828L;

	@JestId
	private String id;

	private String dependencyTree;

	private String artifact;
	
	private String rootArtifact;

	private Date createdOn;

	public DependencyTreeDTO(String dependencyTree, String artifact, String rootArtifact) {
		this.dependencyTree = dependencyTree;
		this.artifact = artifact;
		this.rootArtifact = rootArtifact;
		this.createdOn = new Date();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDependencyTree() {
		return dependencyTree;
	}

	public void setDependencyTree(String dependencyTree) {
		this.dependencyTree = dependencyTree;
	}

	public String getArtifact() {
		return artifact;
	}

	public void setArtifact(String artifact) {
		this.artifact = artifact;
	}

	public String getRootArtifact() {
		return rootArtifact;
	}

	public void setRootArtifact(String rootArtifact) {
		this.rootArtifact = rootArtifact;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	@Override
	public String toString() {
		return "DependencyTreeDTO [id=" + id + ", dependencyTree=" + dependencyTree + ", artifact=" + artifact + ", rootArtifact=" + rootArtifact + ", createdOn=" + createdOn + "]";
	}
}
