package com.stacksync.syncservice.exceptions;

import com.stacksync.commons.models.Item;

public class CommitExistantVersion extends Exception {

	private static final long serialVersionUID = 3790965344729620693L;
	
	private Item item;
	private long version;

	public CommitExistantVersion() {
		super();
	}

	public CommitExistantVersion(String message) {
		super(message);
	}
	
	public CommitExistantVersion(String message, Item item, long version) {
		super(message);
		this.item = item;
		this.version = version;
	}

	public CommitExistantVersion(String message, Throwable cause) {
		super(message, cause);
	}

	public CommitExistantVersion(Throwable cause) {
		super(cause);
	}
	
	public Item getItem() {
		return item;
	}

	public long getVersion() {
		return version;
	}

}
