package gameClient;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.JSONObject;

import Server.Game_Server;
import Server.game_service;
import algorithms.Graph_Algo;
import dataStructure.DGraph;
import dataStructure.edge_data;
import dataStructure.graph;
import dataStructure.node_data;
import utils.Point3D;

public class AutoGame_Thread extends Thread {
	private MyGameGUI gui;
	private int numOfRobots = 0;
	private long time_of_last_draw = 0, current_time = 0;
	private game_service game;
	private ArrayList<String> fruits;
	private ArrayList<String> robots;
	private Point3D [] targets;
	private Hashtable<Integer, ArrayList<node_data>> missionControl = new Hashtable<Integer, ArrayList<node_data>>();

	public AutoGame_Thread(game_service game, ArrayList<String> fruits, ArrayList<String> robots, MyGameGUI gui) {
		this.game = game;
		this.fruits = fruits;
		this.robots = robots;
		this.gui = gui;
	}

	public void run(){
		String info = game.toString();
		JSONObject line;
		try {
			line = new JSONObject(info);
			JSONObject ttt = line.getJSONObject("GameServer");
			numOfRobots = ttt.getInt("robots");
		} catch (Exception e1) {e1.printStackTrace();}
		gui.repaint();
		
		targets = new Point3D[numOfRobots];

		System.out.println(game.getFruits());

		chooseLocationsForRobots();

		game.startGame();
		time_of_last_draw = game.timeToEnd();

		while(game.isRunning()) {
			moveRobots();
		}			

		String results = game.toString();
		System.out.println("Game Over: "+results);

	}

	private void moveRobots() {
		List<String> log = game.move();
		initList(robots, log);
		initList(fruits, game.getFruits());
		//		robots = (ArrayList<String>) log;
		//		fruits = (ArrayList<String>) game.getFruits();
		if(log!=null) {
			current_time = game.timeToEnd();
			if(time_of_last_draw-current_time>100) {
				gui.repaint();
				time_of_last_draw = game.timeToEnd();
			}
			for(int i=0;i<log.size();i++) {
				String robot_json = log.get(i);

				try {
					JSONObject line = new JSONObject(robot_json);
					JSONObject ttt = line.getJSONObject("Robot");
					int rid = ttt.getInt("id");
					int src = ttt.getInt("src");
					int dest = ttt.getInt("dest");

					if(dest == -1) {
						ArrayList<node_data> robot_mission = missionControl.get(rid);
						if(robot_mission!=null) {
							while(!robot_mission.isEmpty()) {
								node_data n = robot_mission.remove(0);
								if(n.getKey()==src && robot_mission.size()>0) {
									dest = robot_mission.get(0).getKey();
									break;
								}
							}
						}
					}

					if(dest == -1) {
						double minDist = Double.MAX_VALUE;
						ArrayList<node_data> robot_mission = null;
						Point3D pTarget = null;
						List<String> list = game.getFruits();
						Iterator<String> itr = list.iterator();
						while(itr.hasNext()) {
							String info = itr.next();
							JSONObject t = new JSONObject(info);
							JSONObject fruit = t.getJSONObject("Fruit");
							Point3D p = gui.setScale(new Point3D(fruit.getString("pos")));
							if(!isTargeted(p)) {
								edge_data e = gui.getEdgeOfFruit(p);
								double dis = gui.getMissionDist(src, e);
								if(dis<minDist) {
									minDist = dis;
									pTarget = p;
									robot_mission = (ArrayList<node_data>) gui.getMissionList(src, e);
								}	
							}
							if(robot_mission!=null) {
								missionControl.put(rid, robot_mission);
								targets[rid % numOfRobots] = pTarget;
							}
						}
					}

						if(dest != -1) {
							game.chooseNextEdge(rid, dest);
							System.out.println("Turn to node: "+dest+"  time to end:"+(current_time/1000));
							System.out.println(ttt);
						}
				} catch (Exception e) {e.printStackTrace();}
			}
		}
	}

	private void initList(List<String> target, List<String> src) {
		target.clear();
		Iterator<String> itr = src.iterator();
		while(itr.hasNext()) {
			String s = itr.next();
			target.add(s);
		}
	}

	private void chooseLocationsForRobots() {
		int count = 0;
		List<String> list = game.getFruits();
		Iterator<String> itr = list.iterator();
		try {
			while(itr.hasNext() && count<numOfRobots) {
				String info = itr.next();
				JSONObject t = new JSONObject(info);
				JSONObject fruit = t.getJSONObject("Fruit");
				int type = fruit.getInt("type");
				Point3D p = gui.setScale(new Point3D(fruit.getString("pos")));
				edge_data e = gui.getEdgeOfFruit(p);
				System.out.println(e);
				if(type == -1)
					game.addRobot(Math.max(e.getSrc(), e.getDest()));
				else
					game.addRobot(Math.min(e.getSrc(), e.getDest()));
			}
			if(count<numOfRobots)
				game.addRobot(count++);
		} catch (Exception e) {e.printStackTrace();}
	}
	
	private boolean isTargeted(Point3D p) {
		for(int i =0; i<targets.length; i++) {
			if(targets[i]!=null && targets[i].equals(p))
				return true;
		}
		return false;
	}
}

