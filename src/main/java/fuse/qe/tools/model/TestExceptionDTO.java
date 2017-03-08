package fuse.qe.tools.model;

import java.io.Serializable;
import java.util.Date;

import io.searchbox.annotations.JestId;
import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

public class TestExceptionDTO implements Serializable {

	private static final long serialVersionUID = 797891176701190412L;

	@JestId
	private String id;

	private String errorStackTrace;

	private String groupId;

	private Date createdOn;

	public TestExceptionDTO() {
		this.createdOn = new Date();
	}
	
	public TestExceptionDTO(String id, String est) {
		this.id = id;
		this.errorStackTrace = est;
		this.createdOn = new Date();
	}

	public TestExceptionDTO(String id, String est, String gId) {
		this.id = id;
		this.errorStackTrace = est;
		this.groupId = gId;
		this.createdOn = new Date();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getErrorStackTrace() {
		return errorStackTrace;
	}

	public void setErrorStackTrace(String error_stack_trace) {
		this.errorStackTrace = error_stack_trace;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String gid) {
		this.groupId = gid;
	}
	
	@Override
	public String toString() {
		JSONBuilder builder = new JSONStringer();
		builder.object()
			.key("id").value(id)
			.key("errorStackTrace").value(errorStackTrace)
			.key("groupId").value(groupId)
			.key("createdOn").value(createdOn)
		.endObject();
				
		return builder.toString();
	}
}
