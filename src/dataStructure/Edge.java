package dataStructure;

import java.io.Serializable;
/**
 * this class represents an edge in a directed graph.
 * every edge has two nodes, source and destination, and weight.
 * @author oriel
 *
 */
public class Edge implements edge_data, Serializable {
	
	private static final long serialVersionUID = -7427023708664735147L;
	private int src, dest, tag; 
	private double weight = 0;
	private String info = "";
	/**
	 * constructes an edge between src_id to dest_id.
	 * the weight remains 0.
	 * @param src_id
	 * @param des_id
	 */
	public Edge(int src_id, int dest_id) {
		src = src_id;
		dest = dest_id;
	}
	/**
	 * constructes an edge between src_id to dest_id with w as weight.
	 * @param src_id
	 * @param dest_id
	 * @param w
	 */
	public Edge(int src_id, int dest_id, double w) {
		src = src_id;
		dest = dest_id;
		weight = w;
	}
	/**
	 * return true if ob is instance of edge and the source, destination and weight 
	 * of ob are the same as this edge.
	 */
	
	public boolean equals(Object ob) {
		if(ob instanceof edge_data) {
			edge_data e = (edge_data)ob;
			return (src==e.getSrc())&&(dest==e.getDest())&&(weight==e.getWeight());
		}
		else
			return false;
	}
	/**
	 * returns the id of the source of this edge.
	 */

	@Override
	public int getSrc() {
		// TODO Auto-generated method stub
		return src;
	}
	/**
	 * returns the id of the destination of this edge.
	 */

	@Override
	public int getDest() {
		// TODO Auto-generated method stub
		return dest;
	}
	/**
	 * returns the weight of this edge.
	 */

	@Override
	public double getWeight() {
		// TODO Auto-generated method stub
		return weight;
	}
	/**
	 * sets the weight of this edge to w.
	 * @param w
	 */
	
	public void setWeight(double w) {
		// TODO Auto-generated method stub
		weight = w;
	}
	/**
	 * returns the info of this edge.
	 */

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return info;
	}
	/**
	 * sets the info of this edge to s.
	 */

	@Override
	public void setInfo(String s) {
		// TODO Auto-generated method stub
		info = s;
		
	}
	/**
	 * returns the tag of this edge.
	 */
	@Override
	public int getTag() {
		// TODO Auto-generated method stub
		return tag;
	}
	/**
	 * sets the tag of this edge to t.
	 */
	@Override
	public void setTag(int t) {
		// TODO Auto-generated method stub
		tag = t;	
	}
	/**
	 * returns a string that represents this edge.
	 * the string includes the source and destination of this edge
	 * but not its weight (for debug convenience).
	 */
	public String toString() {
		return "("+src+","+dest+")";
	}

}
