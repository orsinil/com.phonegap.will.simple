package com.wacom.ink.utils;

public class SharedResource<T> {
	private T resource;
	private Owner<T> owner;
	
	public SharedResource(T resource){
		this.resource = resource;
	}
	
	public void set(T resource){
		this.resource = resource;
	}

	public T aquire(SharedResource.Owner<T> owner){
		if (this.owner!=null && this.owner!=owner){
			this.owner.release(resource);
		}
		this.owner = owner;
//		owner.onAquired();
		return resource;
	}

//	public void release(){
//		if (owner!=null){
//			owner.release();
//		}
//	}
	
	public void release() {
		if (owner!=null){
			owner.release(resource);
			owner = null;
		}
	}
	
	public static interface Owner<T>{
		void release(T resource);
//		void onAquired();
	}

	public boolean hasOwner() {
		return owner!=null;
	}
}
