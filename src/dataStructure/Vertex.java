package dataStructure;


import java.io.Serializable;

import utils.Point3D;
/**
 * this class represents a vertex (node) in a directed graph.
 * every node has a unique id (int), and location in space.
 * the location is saved with Point3D data structure.
 * @author oriel
 *
 */
public class Vertex implements node_data, Serializable {
	
	private static final long serialVersionUID = 6385196210904772317L;
	private static int key = 0;
	private int id = key++, tag = 0;
	private double weight = 0;
	private String info = "";
	private Point3D location;
	
	
	/**
	 * contstructes a node and inits it location to (x,y,0).
	 * the location is based Point3D data structure.
	 * @param x
	 * @param y
	 */
	public Vertex(double x, double y) {
		// TODO Auto-generated constructor stub
		location = new Point3D(x,y);
	}
	/**
	 *  contstructes a node and inits it location to (x,y,z).
	 *  the location is based Point3D data structure.
	 * @param x
	 * @param y
	 * @param z
	 */
	
	public Vertex(double x, double y, double z) {
		// TODO Auto-generated constructor stub
		location = new Point3D(x,y,z);
	}
	
	/**
	 * contstructes a node and inits it location to the Point3D p parameter.
	 * @param p
	 */
	
	public Vertex(Point3D p) {
		// TODO Auto-generated constructor stub
		location = new Point3D(p);
	}
	
	public Vertex(Point3D p, int id) {
		// TODO Auto-generated constructor stub
		location = new Point3D(p);
		this.id = id;
	}
	
	/**
	 * this function checks if this vertex is equal to the object ob.
	 * retrun true if ob is instance of vertex and has the same id as this vertex.
	 */
	
	public boolean equals(Object ob) {
		if(ob instanceof Vertex) {
			Vertex v = (Vertex)ob;
			return v.getKey()==id;
		}
		else
			return false;
	}
	/**
	 * retruns the id of this vertex.
	 */
	
	@Override
	public int getKey() {
		// TODO Auto-generated method stub
		return id;
	}
	/**
	 * returns the location of this vertex a Point3D data structire.
	 */

	@Override
	public Point3D getLocation() {
		// TODO Auto-generated method stub
		return new Point3D(location);
	}
	/**
	 * set the location of this vertex to p.
	 */

	@Override
	public void setLocation(Point3D p) {
		// TODO Auto-generated method stub
		location = new Point3D(p);
		
	}
	/**
	 * returns the weight of this vertex.
	 */

	@Override
	public double getWeight() {
		// TODO Auto-generated method stub
		return weight;
	}
	/**
	 * set the weight of this vertex to w.
	 */

	@Override
	public void setWeight(double w) {
		// TODO Auto-generated method stub
		weight = w;
	}
	/**
	 * returns the info of this vertex.
	 */

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return info;
	}
	/**
	 * sets the info of this vertex to s.
	 */

	@Override
	public void setInfo(String s) {
		// TODO Auto-generated method stub
		info = s;
	}
	/**
	 * returns the tag of this vertex.
	 */

	@Override
	public int getTag() {
		// TODO Auto-generated method stub
		return tag;
	}
	/**
	 * sets the tag of this vertex to t.
	 */

	@Override
	public void setTag(int t) {
		// TODO Auto-generated method stub
		tag = t;
	}
	/**
	 * returns a string that represents this node.
	 * the string includes only its id.
	 */
	
	public String toString() {
		return ""+id+"loc:"+location;
	}

}
