package algorithms;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import dataStructure.DGraph;
import dataStructure.edge_data;
import dataStructure.graph;
import dataStructure.node_data;
/**
 * This class represents a set of graph-theory algorithms.
 * it includes:
 * 1) init from file (Load).
 * 2) save.
 * 3) shortest path between two nodes in the graph.
 * 4) tsp
 * 5) and more..
 * @author oriel 
 *
 */
public class Graph_Algo implements graph_algorithms, Serializable{

	private static final long serialVersionUID = -5021372918641989775L;
	private DGraph g;
	/**
	 * constructes a new empty graph for this class.
	 */
	public Graph_Algo() {
		g = new DGraph();
	}
	
	public Graph_Algo(graph g) {
		try {
			this.g = (DGraph)g;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Init this set of algorithms on the parameter - graph.
	 * @param g
	 */
	@Override
	public void init(graph g) {
		try {
			DGraph newG = (DGraph)g;
			this.g = newG;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Init a graph from file
	 * @param file_name
	 */	

	@Override
	public void init(String file_name) {
		try {
			FileInputStream streamIn = new FileInputStream(file_name);
			ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
			Graph_Algo readCase = (Graph_Algo) objectinputstream.readObject();
			this.g = (DGraph) readCase.copy();
			objectinputstream.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	/** Saves the graph to a file.
	 * 
	 * @param file_name
	 */

	@Override
	public void save(String file_name) {
		ObjectOutputStream oos;
		try {
			FileOutputStream fout = new FileOutputStream(file_name, false);
			oos = new ObjectOutputStream(fout);
			oos.writeObject(this);
			oos.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Returns true if and only if (iff) there is a valid path from EVREY node to each
	 * other node.
	 * @return
	 */

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		Collection<node_data> c1 = g.getV();
		Iterator<node_data> itr1 = c1.iterator();
		while(itr1.hasNext()) {
			node_data n1 = itr1.next();
			Collection<node_data> c2 = g.getV();
			Iterator<node_data> itr2 = c2.iterator();
			while(itr2.hasNext()) {
				node_data n2 = itr2.next();
				double d = shortestPathDist(n1.getKey(), n2.getKey());
				if(d<0) {
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * returns the length of the shortest path between src to dest.
	 * if there is no path between them it returns -1.
	 * @param src - start node
	 * @param dest - end (target) node
	 * @return
	 */

	@Override
	public double shortestPathDist(int src, int dest) {
		// TODO Auto-generated method stub
		ArrayList<node_data> a = (ArrayList<node_data>) shortestPath(src, dest);
		if(a==null) {
			return -1;
		}
		double sum = 0;
		for(int i = 0; i<a.size()-1; i++) {
			edge_data e = g.getEdge(a.get(i).getKey(), a.get(i+1).getKey());
			sum += e.getWeight();
		}
		return sum;
	}
	/**
	 * returns the the shortest path between src to dest - as an ordered List of nodes:
	 * src--> n1-->n2-->...dest
	 * see: https://en.wikipedia.org/wiki/Shortest_path_problem
	 * if there is no path betweem src to dest it will return null.
	 * if the src and dest are same node it will return a List which contains
	 * this node.
	 * @param src - start node
	 * @param dest - end (target) node
	 * @return
	 */

	@Override
	public List<node_data> shortestPath(int src, int dest) {
		if(g.getNode(src)==null || g.getNode(dest)==null)
			return null;
		try {
			ArrayList<node_data> ans = new ArrayList<node_data>();
			if(g.getNode(src).equals(g.getNode(dest))) {
				ans.add(g.getNode(src));
				return ans;
			}
			PriorityQueue <node_data> notVisited = new PriorityQueue <node_data> (g.nodeSize(),new Node_Comparator());
			Collection<node_data> c = g.getV();
			Iterator<node_data> itr = c.iterator();
			while(itr.hasNext()) {
				node_data n = itr.next();
				if(n.getKey()==src) {
					n.setWeight(0);

				} else {
					n.setWeight(Integer.MAX_VALUE);

				}
				n.setInfo("");
				n.setTag(0);
				notVisited.add(n);
			}
			while(!notVisited.isEmpty()) {
				//			System.out.println("src:"+src+", dest: "+dest+" pq: "+notVisited);
				node_data n = notVisited.poll();
				if(n.getKey()==dest && !n.getInfo().equals("")) {
					ans.add(n);
					while(!n.getInfo().equals("")) {
						node_data newNode = g.getNode(Integer.parseInt(n.getInfo()));
						ans.add(0, newNode);
						n = newNode;
					}
					//				ans.sort(new Node_Comparator());
					return ans;	
				}
				Collection<edge_data> outOfn = g.getE(n.getKey());
				Iterator<edge_data> itr2 = outOfn.iterator();
				while(itr2.hasNext()) {
					edge_data edge = itr2.next();
					node_data d = g.getNode(edge.getDest());
					if(d.getTag()==0) {
						if(d.getWeight()>(n.getWeight() + edge.getWeight())) {
							d.setWeight(n.getWeight() + edge.getWeight());
							d.setInfo(""+n.getKey());
							notVisited.remove(d);
							notVisited.add(d);
						} 
					}	
				}
				n.setTag(1);
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * computes a relatively short path which visit each node in the targets List.
	 * Note: this is NOT the classical traveling salesman problem, 
	 * as you can visit a node more than once, and there is no need to return to source node - 
	 * just a simple path going over all nodes in the list.
	 * if there is no path that visit every node in the tergets List it will return null. 
	 * @param targets
	 * @return
	 */

	@Override
	public List<node_data> TSP(List<Integer> targets) {
		// TODO Auto-generated method stub
		ArrayList<node_data> left = new ArrayList<node_data>();
		ArrayList<node_data> ans = new ArrayList<node_data>();
		Iterator<Integer> itr = targets.iterator();
		while(itr.hasNext()) {
			int id = itr.next();
			if(g.getNode(id)==null) {
				return null;
			}
			left.add(g.getNode(id));
		}

		for(int i = 0; i<left.size()-1; i++) {
			ArrayList<node_data> temp = (ArrayList<node_data>) shortestPath(left.get(i).getKey(), left.get(i+1).getKey());
			if(temp==null) {
				return null;
			}
			for(int j =0; j<temp.size(); j++) {
				if(!ans.contains(temp.get(j)))
					ans.add(temp.get(j));
			}
		}
		return ans;	
	}
	/** 
	 * Compute a deep copy of this graph.
	 * uses the copy method of DGraph.
	 * @return
	 */

	@Override
	public graph copy() {
		// TODO Auto-generated method stub
		return g.copy();
	}
	/**
	 * checks if this the graph in this class is equal to ob.
	 * uses the equal method of DGraph.
	 */

	public boolean equals(Object ob) {
		return this.g.equals(ob);
	}
	/**
	 * returns a string that represents the graph in this class.
	 * uses the toString method of DGraph.
	 */

	public String toString() {
		return g.toString();
	}
}




