package com.apkspectrum.core.permissionmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.apkspectrum.util.XmlPath;

public class PermissionRepository {
	public final String url;
	public final String manifestPath;
	public final String resourcePath;
	public final String[] config;
	public final List<SourceCommit> sources;

	public class SourceCommit {
		int sdk;
		String commit;
		public SourceCommit(int sdk, String commit) {
			this.sdk = sdk;
			this.commit = commit;
		}
		public int getSdkVersion() { return sdk; }
		public String getCommitId() { return commit != null && !commit.isEmpty() ? commit : null; }
	}

	public PermissionRepository(XmlPath sourcesNode) {
		this(sourcesNode.getAttribute("repository"), sourcesNode.getAttribute("manifest"), sourcesNode.getAttribute("resources"), sourcesNode.getAttribute("config"));
		sourcesNode = sourcesNode.getNodeList("source");
		for(int i=sourcesNode.getCount()-1; i>=0; --i) {
			sources.add(new SourceCommit(Integer.parseInt(sourcesNode.getAttribute(i, "sdk")), sourcesNode.getAttribute(i, "commit")));
		}
		Collections.sort(sources, new Comparator<SourceCommit>() {
			@Override
			public int compare(SourceCommit arg0, SourceCommit arg1) {
				return arg0.sdk - arg1.sdk;
			}
		});
	}

	public PermissionRepository(String url, String manifestPath, String resourcePath, String config) {
		this.url = url;
		this.manifestPath = manifestPath;
		this.resourcePath = resourcePath;

		ArrayList<String> confList = new ArrayList<>();
		confList.add("default");
		for(String conf: config.split(";")) {
			conf = conf.trim();
			if(conf.isEmpty() || confList.contains(conf)) continue;
			confList.add(conf);
		}

		this.config = confList.toArray(new String[confList.size()]);
		this.sources = new ArrayList<>();
	}

	public void addCommit(int sdk, String commit) {
		for(SourceCommit src: sources) {
			if(src.sdk == sdk) {
				if(src.commit != commit) src.commit = commit;
				return;
			}
		}
		sources.add(new SourceCommit(sdk, commit));
		Collections.sort(sources, new Comparator<SourceCommit>() {
			@Override
			public int compare(SourceCommit arg0, SourceCommit arg1) {
				return arg0.sdk - arg1.sdk;
			}
		});
	}
}
