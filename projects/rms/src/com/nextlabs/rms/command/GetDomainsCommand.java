package com.nextlabs.rms.command;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nextlabs.rms.config.GlobalConfigManager;
import com.nextlabs.rms.json.JsonUtil;
import com.nextlabs.rms.util.StringUtils;

public class GetDomainsCommand extends AbstractCommand {
	static class Domain {
		private final String id;
		private final String name;

		public Domain(String id, String name) {
			this.id = id;
			this.name = name;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			} else if (obj instanceof Domain) {
				Domain oth = (Domain) obj;
				return StringUtils.equals(getId(), oth.getId());
			}
			return false;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		@Override
		public int hashCode() {
			int hash = getId() != null ? getId().hashCode() : 0;
			return hash;
		}
	}

	@Override
	public void doAction(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String[] domainNames = GlobalConfigManager.getInstance().getDomainNames();
		Set<Domain> domains = new LinkedHashSet<>(domainNames != null ? domainNames.length : 0);
		if (domainNames != null && domainNames.length > 0) {
			for (String s : domainNames) {
				if (StringUtils.hasText(s)) {
					domains.add(new Domain(s, s));
				}
			}
		}
		JsonUtil.writeJsonToResponse(domains, response);
	}
}
