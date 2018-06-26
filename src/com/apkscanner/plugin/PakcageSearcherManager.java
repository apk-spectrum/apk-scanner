package com.apkscanner.plugin;

import java.util.ArrayList;
import java.util.Collection;

public class PakcageSearcherManager
{
	private ArrayList<IPackageSearcher> searchers = new ArrayList<IPackageSearcher>();

	public void add(IPackageSearcher searcher) {
		synchronized(searchers) {
			if(!searchers.contains(searcher)) {
				searchers.add(searcher);
			}
		}
	}

	public void remove(IPackageSearcher searcher) {
		synchronized(searchers) {
			if(searchers.contains(searcher)) {
				searchers.remove(searcher);
			}
		}
	}

	public void removeAll(Collection<IPackageSearcher> searchers) {
		synchronized(searchers) {
			searchers.removeAll(searchers);
		}
	}

	public void clear() {
		synchronized(searchers) {
			searchers.clear();
		}
	}

	public IPackageSearcher[] getList() {
		synchronized(searchers) {
			return searchers.toArray(new IPackageSearcher[searchers.size()]);
		}
	}

	public IPackageSearcher[] getList(int type) {
		synchronized(searchers) {
			ArrayList<IPackageSearcher> list = new ArrayList<IPackageSearcher>(searchers.size());
			for(IPackageSearcher searcher: searchers) {
				if((searcher.getSupportType() & type) == type) {
					list.add(searcher);
				}
			}
			return list.toArray(new IPackageSearcher[list.size()]);
		}
	}

	public IPackageSearcher[] getList(int type, String name) {
		synchronized(searchers) {
			ArrayList<IPackageSearcher> list = new ArrayList<IPackageSearcher>(searchers.size());
			for(IPackageSearcher searcher: searchers) {
				if(searcher.trySearch(type, name)) {
					list.add(searcher);
				}
			}
			return list.toArray(new IPackageSearcher[list.size()]);
		}
	}
}
