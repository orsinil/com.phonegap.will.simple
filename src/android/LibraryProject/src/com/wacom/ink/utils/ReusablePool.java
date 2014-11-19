package com.wacom.ink.utils;

import java.util.LinkedList;

public abstract class ReusablePool<T> {
	private LinkedList<SharedResource<T>> pool;
	private int maxsize;
	
	public ReusablePool(int maxsize){
		this.maxsize = maxsize;
	}
	
	public void getSlot(){
		SharedResource<T> res = null;
		if (pool.size()==maxsize){
			for (SharedResource<T> resource: pool){
				if (!resource.hasOwner()){
					res = resource;
					break;
				}
			}
			if (res==null){
				res = pool.getFirst();
				res.release();
			}
		} else {
			pool.add(createSlot());
		}
	}
	
	protected abstract SharedResource<T> createSlot();
}
