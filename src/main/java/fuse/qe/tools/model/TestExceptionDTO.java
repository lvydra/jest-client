package fuse.qe.tools.model;

import java.io.Serializable;
import java.util.Date;

import io.searchbox.annotations.JestId;

public class TestExceptionDTO implements Serializable {

	private static final long serialVersionUID = 797891176701190412L;

	@JestId
	private String id;

	private String error_stack_trace;

	private String group_id;

	private Date createdOn;

	public TestExceptionDTO() {
		this.createdOn = new Date();
	}
	
	public TestExceptionDTO(String id, String est) {
		this.id = id;
		this.error_stack_trace = est;
		this.createdOn = new Date();
	}

	public TestExceptionDTO(String id, String est, String gId) {
		this.id = id;
		this.error_stack_trace = est;
		this.group_id = gId;
		this.createdOn = new Date();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getError_stack_trace() {
		return error_stack_trace;
	}

	public void setError_stack_trace(String error_stack_trace) {
		this.error_stack_trace = error_stack_trace;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getGroup_id() {
		return group_id;
	}

	public void setGroup_id(String gid) {
		this.group_id = gid;
	}
	
	@Override
	public String toString() {
		return "Record [id=" + id + ", error_stack_trace=" + error_stack_trace + ", createdOn=" + createdOn + "]";
	}
}
