package Tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dataStructure.DGraph;
import dataStructure.Edge;
import dataStructure.Vertex;
import dataStructure.edge_data;
import dataStructure.node_data;

class DGraphTest {
	Vertex[] v = {
			new Vertex(1,2),
			new Vertex(2,2),
			new Vertex(1,3),
			new Vertex(1,4),
			new Vertex(3,5)};
	Edge[] e = {
			new Edge(v[0].getKey(), v[1].getKey(), 3),
			new Edge(v[0].getKey(), v[2].getKey(), 2),
			new Edge(v[0].getKey(), v[3].getKey(), 7),
			new Edge(v[1].getKey(), v[2].getKey(), 9),
			new Edge(v[1].getKey(), v[4].getKey(), 5),
			new Edge(v[4].getKey(), v[0].getKey(), 4)};
	DGraph g = new DGraph();

	@BeforeEach
	void setUp() throws Exception {
		for(int i=0; i<v.length; i++)
			g.addNode(v[i]);
		for(int i = 0; i<e.length; i++) {
			g.connect(e[i].getSrc(), e[i].getDest(), e[i].getWeight());
		}
	}


	@Test
	void testGetNode() {
		int key = v[0].getKey();
		node_data n = g.getNode(key);
		assertEquals(n, v[0]);
	}

	@Test
	void testGetEdge() {
		Edge edge = e[0];
		edge_data edgeAns = g.getEdge(e[0].getSrc(), e[0].getDest());
		assertEquals(edgeAns, edge);
	}

	@Test
	void testAddNode() {
		node_data actual = new Vertex(3,3);
		g.addNode(actual);
		node_data expected = g.getNode(actual.getKey()) ;
		assertEquals(expected, actual);
	}

	@Test
	void testConnect() {
		int src = v[2].getKey();
		int dest = v[3].getKey();
		edge_data actual = new Edge(src, dest, 22);
		g.connect(src, dest, 22);
		edge_data expected = g.getEdge(src, dest);
		System.out.println(actual);
		System.out.println(expected);
		assertEquals(expected, actual);
	}

	@Test
	void testGetV() {
		Collection<node_data> c = g.getV();
		int expected = c.size();
		int actual = g.nodeSize();
		assertEquals(expected, actual);
	}

	@Test
	void testGetE() {
		Collection<edge_data> c = g.getE(v[0].getKey());
		int expected = c.size();
		int actual = 3;
		assertEquals(expected, actual);
		c = g.getE(v[1].getKey());
		expected = c.size();
		actual = 2;
		assertEquals(expected, actual);
	}

	@Test
	void testRemoveNode() {
		int nodeSize = g.nodeSize();
		int edgeSize = g.edgeSize();
		g.removeNode(v[0].getKey());
		int expected = nodeSize-1;
		int actual = g.nodeSize();
		assertEquals(expected, actual);
		expected = edgeSize-4;
		actual = g.edgeSize();
		assertEquals(expected, actual);
		
	}

	@Test
	void testRemoveEdge() {
		edge_data edge = e[0];
		int edgeSize = g.edgeSize();
		g.removeEdge(edge.getSrc(), edge.getDest());
		int expected = edgeSize-1;
		int actual = g.edgeSize();
		assertEquals(expected, actual);
		edge_data edge2 = g.getEdge(edge.getSrc(), edge.getDest());
		assertEquals(edge2, null);
		
	}

	@Test
	void testNodeSize() {
		int actual = v.length;
		int expected = g.nodeSize();
		assertEquals(expected, actual);
	}

	@Test
	void testEdgeSize() {
		int ans = g.edgeSize();
		int actual = e.length;
		assertEquals(ans, actual);
	}

	@Test
	void testGetMC() {
		int firstMC = g.getMC();
		g.addNode(new Vertex(5,5));
		assertEquals(firstMC+1, g.getMC());
		g.connect(v[2].getKey(), v[3].getKey(), 30);
		assertEquals(firstMC+2, g.getMC());
		g.removeEdge(v[2].getKey(), v[3].getKey());
		assertEquals(firstMC+3, g.getMC());
		g.removeNode(v[0].getKey());
		assertEquals(firstMC+4, g.getMC());
	}
	
	@Test
	void testCopy() {
		DGraph g2 = (DGraph) g.copy();
//		System.out.println(g);
//		System.out.println("******************");
//		System.out.println(g2);
		assertEquals(g2, g);
	}
	
	@Test
	void testMilyon() {
		DGraph milyon = new DGraph();
		for(int i =0; i<999995; i++) {
			node_data n = new Vertex(i,i);
			g.addNode(n);
		}
		assertEquals(1000000, g.nodeSize());
	}

}
