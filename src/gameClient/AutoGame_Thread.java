package gameClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

/**
 * this class represents a thread that controls the auto-game.
 * @author oriel
 *
 */
public class AutoGame_Thread extends Thread {
	private MyGameGUI gui;
	private int numOfRobots = 0;
	private final int const_dt = 100;
	private boolean border_spliting = true;
	private int min_dt;
	private long time_of_last_draw = 0, current_time = 0;
	private game_service game;
	private ArrayList<String> fruits;
	private ArrayList<String> robots;
	private HashMap<Integer, Point3D> targets;
	private Hashtable<Integer, ArrayList<node_data>> missionControl = new Hashtable<Integer, ArrayList<node_data>>();

	/**
	 * this is constructor for the thread.
	 * it must get this parameters so it will be possible to show this
	 * game on the GUI class.
	 * @param game
	 * @param fruits
	 * @param robots
	 * @param gui
	 */
	public AutoGame_Thread(game_service game, ArrayList<String> fruits, ArrayList<String> robots, MyGameGUI gui) {
		this.game = game;
		this.fruits = fruits;
		this.robots = robots;
		this.gui = gui;
	}

	/**
	 * this funtion runs the game.
	 */

	public void run(){
		String info = game.toString();
		JSONObject line;
		try {
			line = new JSONObject(info);
			JSONObject ttt = line.getJSONObject("GameServer");
			numOfRobots = ttt.getInt("robots");
		} catch (Exception e1) {e1.printStackTrace();}
		gui.repaint();
		
		try {
		int check = Integer.parseInt(JOptionPane.showInputDialog("Enter 1 for a border spliting or 0 for a free run"));
		if(check == 1)
			border_spliting = true;
		else
			border_spliting = false;
		} catch (Exception e) { border_spliting = false;}
		targets = new HashMap<Integer, Point3D>(numOfRobots);

		System.out.println(game.getFruits());

		chooseLocationsForRobots();

		game.startGame();
		time_of_last_draw = game.timeToEnd();

		while(game.isRunning()) {
				moveRobots();
			try {
				System.out.println("waiting "+min_dt);
				if(min_dt>10000) min_dt = 10;
				Thread.sleep(min_dt);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}			

		String results = game.toString();
		System.out.println("Game Over: "+results);

	}

	/**
	 * this function check if a destonation of a robot is -1, if it is
	 * it means it does not move and it will give it a mission to the closest fruit to him.
	 * if the robot has not finished its mission yet it will not get a new mission.
	 */

	private void moveRobots() {
		min_dt = Integer.MAX_VALUE;
		double partOfWidth = gui.getWidth()/numOfRobots;
		double border = partOfWidth;
		List<String> log = game.move();
		initList(robots, log);
		initList(fruits, game.getFruits());
		if(log!=null) {
			current_time = game.timeToEnd();
			if(time_of_last_draw-current_time>100) {
				gui.repaint();
				time_of_last_draw = game.timeToEnd();
			}
			for(int i=0;i<log.size();i++) {
				int offset = 0;
				String robot_json = log.get(i);

				try {
					JSONObject line = new JSONObject(robot_json);
					JSONObject ttt = line.getJSONObject("Robot");
					int rid = ttt.getInt("id");
					int src = ttt.getInt("src");
					int dest = ttt.getInt("dest");
					int speed = ttt.getInt("speed");
					System.out.println("src: "+src+",dest: "+dest);

					if(dest == -1) {
						ArrayList<node_data> robot_mission = missionControl.get(rid);
						if(robot_mission!=null) {
							while(!robot_mission.isEmpty()) {
								node_data n = robot_mission.remove(0);
								if(n.getKey()==src && robot_mission.size()>0) {
									dest = robot_mission.get(0).getKey();
									if(robot_mission.size()==1) {
										timeToNextDest(n, robot_mission.get(0), speed, targets.get(rid));
									}
									else
										timeToNextDest(n, robot_mission.get(0), speed, null);
									break;
								}
							}
						}
					}

					if(dest == -1) {
						double minDist = Double.MAX_VALUE;
						double MaxVal = 0;
						ArrayList<node_data> robot_mission = null;
						Point3D pTarget = null;
						List<String> list = game.getFruits();
						Iterator<String> itr = list.iterator();
						while(itr.hasNext()) {
							String info = itr.next();
							JSONObject t = new JSONObject(info);
							JSONObject fruit = t.getJSONObject("Fruit");
							double val = fruit.getDouble("value");
							Point3D p = gui.setScale(new Point3D(fruit.getString("pos")));
							if(!isTargeted(p) && borderCheck(p.ix(), border-partOfWidth+offset, border)) {
								edge_data e = gui.getEdgeOfFruit(p);
								double dis = gui.getMissionDist(src, e);
								double finalVal = val/dis;
//								if(dis<minDist) {
								if(finalVal>MaxVal) {
									MaxVal = finalVal;
//									minDist = dis;
									pTarget = p;
									robot_mission = (ArrayList<node_data>) gui.getMissionList(src, e);
								}	
							}
							if(robot_mission!=null) {
								missionControl.put(rid, robot_mission);
								dest = robot_mission.get(1).getKey();
								targets.put(rid, pTarget);
								if(robot_mission.size()==2)
									timeToNextDest(robot_mission.get(0), robot_mission.get(1), speed, targets.get(rid));
								else
									timeToNextDest(robot_mission.get(0), robot_mission.get(1), speed, null);
								
							}
						}
					}

					if(dest != -1) {
						game.chooseNextEdge(rid, dest);
						System.out.println("Turn to node: "+dest+"  time to end:"+(current_time/1000));
						System.out.println(ttt);
					}
				} catch (Exception e) {e.printStackTrace();}
				border += partOfWidth;
//				offset += 850;
			}
		}
	}

	/**
	 * sets the target list to have all the values of the src list.
	 * this is necessary and can not be done by only changing the pointer
	 * because if i would change the pointer the "connection" to this list in the GUI class 
	 * would be lost.
	 * @param target
	 * @param src
	 */

	private void initList(List<String> target, List<String> src) {
		target.clear();
		Iterator<String> itr = src.iterator();
		while(itr.hasNext()) {
			String s = itr.next();
			target.add(s);
		}
	}

	/**
	 * this function chooses a location for the robots.
	 * the location will be as close as it can be to a fruit.
	 * if the number of robots is greater than the number of the fruits it 
	 * will choose for the extra robots a random location.
	 */

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

	/**
	 * this function check is a fruit is currently targeted by another robot.
	 * returns true if it is, and false if it is not.
	 * @param p
	 * @return
	 */

	private boolean isTargeted(Point3D p) {
		Collection<Point3D> c = targets.values();
		Iterator<Point3D> itr = c.iterator();
		while(itr.hasNext()) {
			Point3D p2 = itr.next();
			if(p2.equals(p))
				return true;
		}
		return false;
	}

	public void timeToNextDest(node_data src, node_data dest, int speed, Point3D fruit_loc) {
		graph g = gui.getGraph();
		edge_data e = g.getEdge(src.getKey(), dest.getKey());
		double time = e.getWeight();
		double ans = (time/speed)*1000;
		if(fruit_loc!=null) {
			double disToF = src.getLocation().distance2D(fruit_loc);
			double disToDest = src.getLocation().distance2D(dest.getLocation());
			double timeToFruit = disToF/disToDest;
			ans *= timeToFruit;		
		}
		System.out.println("ans is "+ans);
		if(ans<min_dt)
			min_dt = (int) ans;
	}
	
	private boolean borderCheck(int loc, double start, double finish) {
		if(border_spliting)
			return loc>start - 300 && loc<finish + 300;
		return true;		
	}
}

