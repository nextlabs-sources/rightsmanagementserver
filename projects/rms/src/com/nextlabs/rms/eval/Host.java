package com.nextlabs.rms.eval;

import java.io.Serializable;

public class Host implements Serializable, INamedAttribute {
	private static final long serialVersionUID = -3682915358637411395L;
	private final NamedAttributes attributes = new NamedAttributes("host");
	private Integer ipAddress;
	private String hostname;

	public Host(int ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Host(String hostname) {
		if (hostname == null) {
			throw new IllegalArgumentException("hostname is null");
		}
		this.hostname = hostname;
	}

	@Override
	public void addAttribute(String key, String value) {
		this.attributes.addAttribute(key, value);
	}

	@Override
	public NamedAttributes getAttributes() {
		return attributes;
	}

	public String getHostname() {
		return hostname;
	}

	public Integer getIpAddress() {
		return ipAddress;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setIpAddress(Integer ipAddress) {
		this.ipAddress = ipAddress;
	}
}
