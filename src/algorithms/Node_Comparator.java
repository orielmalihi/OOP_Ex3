package algorithms;

import java.util.Comparator;

import dataStructure.node_data;


public class Node_Comparator implements Comparator<node_data> {

	public Node_Comparator() {;}
	public int compare(node_data n1, node_data n2) {
		double dp = n1.getWeight() - n2.getWeight();
		return (int) dp;
	}

	// ******** add your code below *********

}
