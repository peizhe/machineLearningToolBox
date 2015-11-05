package com.ireader.ml.fp.local;

import java.util.ArrayList;
import java.util.List;

public class TreeNode implements Comparable<TreeNode> {
	
	private String name; 								// 节点名称
	private int count; 									// 计数
	private TreeNode parent; 					        // 父节点
	private List<TreeNode> children; 		            // 子节点
	private TreeNode nextHomonym;                       // 下一个同名节点
	
	public TreeNode() {
		
	}
	
	public TreeNode(String name){
		this.name = name;
	}
	
	/*============================service function=============================================*/
	//添加child node
	public void addChildren(TreeNode child) {
		if(this.getChildren() == null) {
			List<TreeNode> list = new ArrayList<TreeNode>();
			list.add(child);
			this.setChildren(list);
		}else{
			this.getChildren().add(child);
		}
	}
	
	//查找children node
	public TreeNode findChild(String name) {
		List<TreeNode> children = this.getChildren();
		if(children != null) {
			for(TreeNode child : children) {
				if(child.getName().equals(name)) {
					return child;
				}
			}
		}
		return null;
	}
	
	//打印子节点的名称
	public void printChildrenName() {
		List<TreeNode> children = this.getChildren();
		if(children != null) {
			for (TreeNode child : children) {
				System.out.print(child.getName() + " ");
			}
		}else {
			System.out.println("null");
		}
	}
	
	//增加计数
	public void countIncrement(int n) {
		this.count += n;
	}
	
	public int compareTo(TreeNode o) {
		int count0 = o.getCount();
		//跟默认的比较大小相反，导致调用Arrays.sort() 是按照降序排列
		return count0 - this.count;
	}

	/*============================getter and setter=======================================*/
	
	
	public List<TreeNode> getChildren() {
		return children;
	}
	
	public void setChildren(List<TreeNode> children) {
		this.children = children;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}


	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public TreeNode getParent() {
		return parent;
	}

	public void setParent(TreeNode parent) {
		this.parent = parent;
	}


	public TreeNode getNextHomonym() {
		return nextHomonym;
	}

	public void setNextHomonym(TreeNode nextHomonym) {
		this.nextHomonym = nextHomonym;
	}

}
