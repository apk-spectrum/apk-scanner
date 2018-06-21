package com.apkscanner.plugin;

import java.io.IOException;
import java.util.ArrayList;

import com.apkscanner.util.ClassFinder;
import com.apkscanner.util.Log;

public class StoreSearcherManager {
	
	private ArrayList<IStoreSearcher> searchers = new ArrayList<IStoreSearcher>();

	public void add(IStoreSearcher searcher) {
		synchronized(searchers) {
			if(!searchers.contains(searcher)) {
				System.out.println(searcher.getClass().getName());
				searchers.add(searcher);
				//searcher.launch(null, IStoreSearcher.SEARCHER_TYPE_APP_NAME, "¸á·Ð");
			}
		}
	}

	public void loadPlugIn() {
    	add(new SimpleSearcher());
    	
		try {
			for(Class<?> cls : ClassFinder.getClasses("com")) {
				if(cls.isMemberClass() || cls.isInterface()) continue;
				Log.e("cls " + cls.getName());
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public IStoreSearcher[] getList() {
		return searchers.toArray(new IStoreSearcher[searchers.size()]);
	}
	
    public static void main(String[] args) throws IOException {
    	StoreSearcherManager manager = new StoreSearcherManager();
    	manager.loadPlugIn();
    }
}
