package com.ireader.ml.fp.local;

 public class Pair<T_1,T_2> {
	   
	   private T_1 left;
	   private T_2 right;
	   
	   public Pair(){}
	   
	   public Pair(T_1 left,T_2 right ) {
		   this.left = left;
		   this.right = right;
	   }

	public T_1 getLeft() {
		return left;
	}

	public void setLeft(T_1 left) {
		this.left = left;
	}

	public T_2 getRight() {
		return right;
	}

	public void setRight(T_2 right) {
		this.right = right;
	}
}
