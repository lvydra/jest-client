package com.kaw;

import io.searchbox.annotations.JestId;

import java.io.Serializable;
import java.util.Date;

public class RecordDTO implements Serializable {
	private static final long serialVersionUID = 797891176701190412L;

	@JestId
    private String id;

    private String record;

    private Date createdOn;

    public RecordDTO(String record) {
        this.record = record;
        this.createdOn = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecord() {
        return record;
    }

    public void setNote(String record) {
        this.record = record;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String toString() {
        return "Record [id=" + id + ", record=" + record + ", createdOn=" + createdOn + "]";
    }

}
