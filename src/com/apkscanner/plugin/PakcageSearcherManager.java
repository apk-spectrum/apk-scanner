package com.apkscanner.plugin;

import java.io.IOException;
import java.util.ArrayList;

import com.apkscanner.plugin.sample.SimpleSearcher;
import com.apkscanner.util.ClassFinder;
import com.apkscanner.util.Log;

public class PakcageSearcherManager {
	
	private ArrayList<IPackageSearcher> searchers = new ArrayList<IPackageSearcher>();

	public void add(IPackageSearcher searcher) {
		synchronized(searchers) {
			if(!searchers.contains(searcher)) {
				System.out.println(searcher.getClass().getName());
				searchers.add(searcher);
				//searcher.launch(null, IStoreSearcher.SEARCHER_TYPE_APP_NAME, "���");
			}
		}
	}

	public void loadPlugIn() {
    	add(new SimpleSearcher());
    	
		try {
			for(Class<?> cls : ClassFinder.getClasses("com.apkscanner.plugin")) {
				if(cls.isMemberClass() || cls.isInterface()) continue;
				Log.e("cls " + cls.getName());
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public IPackageSearcher[] getList() {
		return searchers.toArray(new IPackageSearcher[searchers.size()]);
	}
	
    public static void main(String[] args) throws IOException {
    	PakcageSearcherManager manager = new PakcageSearcherManager();
    	manager.loadPlugIn();
    }
}
