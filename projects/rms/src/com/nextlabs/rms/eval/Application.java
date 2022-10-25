package com.nextlabs.rms.eval;

import java.io.Serializable;

public class Application implements Serializable, INamedAttribute {
	private static final long serialVersionUID = -3555471885887394436L;
	private final NamedAttributes attributes = new NamedAttributes("application");
	private final String name;
	private Long pid;
	private String path;

	public Application(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Name is required");
		}
		this.name = name;		
	}
	
	public Application(String name, Long pid, String path) {
		this(name, path);
		this.pid = pid;
	}
	
	public Application(String name, String path){
		this(name);
		this.path = path;
		// Currently CESDK consumes path as a custom attribute.
		if (path != null) {
			addAttribute("path", path);
		}
	}

	@Override
	public void addAttribute(String key, String value) {
		this.attributes.addAttribute(key, value);
	}

	@Override
	public NamedAttributes getAttributes() {
		return attributes;
	}

	public String getName() {
		return name;
	}

	public Long getPid() {
		return pid;
	}

	public String getPath() {
	return path;
	}

	public void setPath(String path) {
	this.path = path;
	}
}
