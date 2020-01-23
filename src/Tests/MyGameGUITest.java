package Tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import Server.Game_Server;
import Server.game_service;
import dataStructure.DGraph;
import dataStructure.Edge;
import dataStructure.edge_data;
import dataStructure.graph;
import gameClient.MyGameGUI;
import utils.Point3D;

class MyGameGUITest {
	private game_service game = Game_Server.getServer(0);
	private MyGameGUI gui = new MyGameGUI();

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@Test
	void testGetWidth() {
		int width = gui.getWidth();
		assertEquals(width, 1400);
	}

	@Test
	void testSetScale() {
		game.addRobot(9);
		game.startGame();
		List<String> list = game.getFruits();
		try {
			JSONObject line = new JSONObject(list.get(0));
			JSONObject fruit = line.getJSONObject("Fruit");
			Point3D p = new Point3D(fruit.getString("pos"));
			Point3D after = gui.setScale(p);
			if(p.x()<0 || p.x()>1400 || p.y()<0 || p.y()>600)
				fail("set scale did not work");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		game.stopGame();
	}

	@Test
	void testGetGraph() {
		graph g = gui.getGraph();
		DGraph dg = new DGraph();
		assertEquals(g, dg);
	}
}
