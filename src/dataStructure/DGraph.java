package dataStructure;

import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import utils.Point3D;
/**
 * this class represents a directed graph G(V,E).
 * the graph has sets of vertexes and edges.
 * @author oriel
 *
 */

public class DGraph implements graph, Serializable{
	
	private static final long serialVersionUID = 7586302042198106174L;
	private Hashtable<Integer, node_data> vBank = new Hashtable<Integer, node_data>(1000000);
	private Hashtable<Integer,Hashtable<Integer,edge_data>> eBank = new Hashtable<Integer,Hashtable<Integer,edge_data>>(1000000);
	private int edge_size;
	private static int MC = 0;
	
	/**
	 * constructes an epty graph.
	 */
	public DGraph() {
		// TODO Auto-generated constructor stub
		edge_size = 0;
	}
	
	public void init(String json) {
		try {
			JSONObject line = new JSONObject(json);
			JSONArray nodes = line.getJSONArray("Nodes");
			for(int i = 0 ; i<nodes.length(); i++) {
				JSONObject node = nodes.getJSONObject(i);
				int id = node.getInt("id");
				String pos = node.getString("pos");
				Point3D p = new Point3D(pos);
				node_data n = new Vertex(p, id);
				this.addNode(n);
			}
			JSONArray edges = line.getJSONArray("Edges");
			for(int i = 0; i<edges.length(); i++) {
				JSONObject edge = edges.getJSONObject(i);
				int src = edge.getInt("src");
				int dest = edge.getInt("dest");
				double w = edge.getDouble("w");
				this.connect(src, dest, w);
			}			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * checks if this graph is egual to ob. 
	 * this method uses the equals method of String
	 * and therefore might not be accurate. but it
	 * extremly useful for debugging purposes.
	 */

	public boolean equals(Object ob) {
		return this.toString().equals(ob.toString());
	}
	/**
	 * creates a deep copy of this graph.
	 * @return
	 */

	public graph copy() {
		DGraph g = new DGraph();
		Collection<node_data> v = vBank.values();
		Iterator<node_data> itr = v.iterator();
		while(itr.hasNext()) {
			node_data n = itr.next();
			g.addNode(n);
		}
		Collection<Hashtable<Integer,edge_data>> e1 = eBank.values();
		Iterator<Hashtable<Integer,edge_data>> itr1 = e1.iterator();
		while(itr1.hasNext()) {
			Hashtable<Integer, edge_data> h = itr1.next();
			Collection<edge_data> e2 = h.values();
			Iterator<edge_data> itr2 = e2.iterator();
			while(itr2.hasNext()) {
				edge_data edge = itr2.next();
				g.connect(edge.getSrc(), edge.getDest(), edge.getWeight());
			}
		}
		g.edge_size = this.edge_size;
		g.MC = this.MC;
		return g;
	}
	/**
	 * returns the node that its id equal to the key.
	 * if there is not a node with this id returns null.
	 * works in O(1).
	 */
	@Override
	public node_data getNode(int key) {
		// TODO Auto-generated method stub
		return vBank.get(key);
	}
	/**
	 * gets the edge that its source is equal to src, and its destination is
	 * equal to dest. if there is not an edge with this parameters returns null.
	 * works in O(1).
	 */

	@Override
	public edge_data getEdge(int src, int dest) {
		// TODO Auto-generated method stub
		return eBank.get(src).get(dest);
	}
	/**
	 * add node to this graph in O(1).
	 */

	@Override
	public synchronized void addNode(node_data n) {
		// TODO Auto-generated method stub
		vBank.put(n.getKey(), n);
		eBank.put(n.getKey(), new Hashtable<Integer, edge_data>());
		MC++;
		this.notifyAll();
	}
	/**
	 * adds an edge to this graph. the edge source is src
	 * and the edge destination is dest. if thr src or dest nodes are not
	 * in this graph it will do nothing.
	 */

	@Override
	public synchronized void connect(int src, int dest, double w) {
		// TODO Auto-generated method stub
		if(vBank.get(src)!=null && vBank.get(dest)!=null && src != dest) {
			Edge e = new Edge(src,dest, w);
			eBank.get(src).put(dest, e);
			edge_size++;
			MC++;
			this.notifyAll();
		}

	}
	/**
	 * This method returns a pointer (shallow copy) for the
	 * collection representing all the nodes in the graph. 
	 * this method runs in O(1) time.
	 * @return Collection<node_data>
	 */

	@Override
	public Collection<node_data> getV() {
		// TODO Auto-generated method stub
		return vBank.values();
	}
	/**
	 * This method returns a pointer (shallow copy) for the
	 * collection representing all the edges getting out of 
	 * the given node (all the edges starting (source) at the given node). 
	 * this method runs in O(1) time.
	 * @return Collection<edge_data>
	 */

	@Override
	public Collection<edge_data> getE(int node_id) {
		// TODO Auto-generated method stub
		return eBank.get(node_id).values();
	}
	/**
	 * Delete the node (with the given ID) from the graph -
	 * and removes all edges which starts or ends at this node.
	 * This method runs in O(n), |V|=n, as all the edges should be removed.
	 * @return the data of the removed node (null if none). 
	 * @param key
	 */

	@Override
	public synchronized node_data removeNode(int key) {
		// TODO Auto-generated method stub
		Collection<node_data> c =vBank.values();
		Iterator<node_data> itr = c.iterator();
		while(itr.hasNext()) {
			node_data n = itr.next();
			if(eBank.get(n.getKey()).get(key)!=null) {
				eBank.get(n.getKey()).remove(key);
				edge_size--;	
			}
		}
		MC++;
		this.notifyAll();
		int minus = 0;
		if(eBank.get(key)!=null)
			minus = eBank.get(key).size();
		eBank.remove(key);
		edge_size -= minus;
		return vBank.remove(key);
	}
/**
	 * Delete the edge from the graph, 
	 * Note: this method should run in O(1) time.
	 * @param src
	 * @param dest
	 * @return the data of the removed edge (null if none).
 */

	@Override
	public synchronized edge_data removeEdge(int src, int dest) {
		// TODO Auto-generated method stub
		edge_size--;
		MC++;
		this.notifyAll();
		return eBank.get(src).remove(dest);
	}
	/** 
	 * return the number of edges (assume directional graph).
	 * Note: this method should run in O(1) time.
	 * @return
	 */

	@Override
	public int nodeSize() {
		// TODO Auto-generated method stub
		return vBank.size();
	}
	/**
	 * return the Mode Count - for testing changes in the graph.
	 * @return
	 */

	@Override
	public int edgeSize() {
		// TODO Auto-generated method stub
		return edge_size;
	}
	/**
	 * returns the MC parameter of this graph.
	 */

	@Override
	public int getMC() {
		// TODO Auto-generated method stub
		return MC;
	}
	/**
	 * returns a representaion of this graph as a String.
	 * the representayion includes its nodes, edges, number of nodes,
	 * number of edges, and the MC parameter.
	 */

	public synchronized String toString() {
		String ans = "Vertexes: "+vBank.values()+"\nEdges: ";
		//Edges: "+eBank.values()+"\nEdge size: "+edge_size+"\nMC: "+MC;
		Iterator<Hashtable<Integer, edge_data>> itr = eBank.values().iterator();
		while(itr.hasNext()) {
			Hashtable<Integer, edge_data> h = itr.next();
			Collection<edge_data> c = h.values();
			if(!c.isEmpty()) {
				ans += c;
			}
		}
		ans += "\nNumber of Vertixes: "+this.nodeSize()+"\nNumber of Edges: "+edge_size+"\nMC: "+MC;
		return ans;
	}
}


