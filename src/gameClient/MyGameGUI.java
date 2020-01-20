package gameClient;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.json.JSONException;
import org.json.JSONObject;

import Server.Fruit;
import Server.Game_Server;
import Server.game_service;
import algorithms.Graph_Algo;
import dataStructure.DGraph;
import dataStructure.Vertex;
import dataStructure.edge_data;
import dataStructure.graph;
import dataStructure.node_data;
import oop_dataStructure.OOP_DGraph;
import oop_dataStructure.oop_edge_data;
import oop_dataStructure.oop_graph;
import oop_utils.OOP_Point3D;
import utils.Point3D;
import utils.Range;
/**
 * this class represents a GUI for Ex3 aka "The Maze Of Waze" game.
 * in this game the user chooses from the game server a scenario which includes:
 * a graph, number of robots, and number of fruits.
 * the mission is for the robots to eat as many fruits as they can in 30 or 60 secondes game.
 * the user can choose wether to play a custom game or an auto-game.
 * in the custom game the user  controls the movement of the robots, and in the auto-game the robots 
 * move automatic (based on an algorithm i developed).
 * @author oriel
 *
 */

public class MyGameGUI extends JFrame implements ActionListener, MouseListener, MouseMotionListener {
	private KML_Logger kml_logger;
	private int scenario;
	private final double EPSILON = 0.001;
	private graph g;
	private Graph_Algo algo;
	private game_service game = null;
	private edge_data edge_of_fruit = null;
	private int width = 1400, height = 600, numOfRobots = 0, grade = 0, fruit_type = 0;
	private Range rx = new Range(Integer.MAX_VALUE,Integer.MIN_VALUE);
	private Range ry = new Range(Integer.MAX_VALUE,Integer.MIN_VALUE);
	private ArrayList<String> fruits = new ArrayList<String>();
	private ArrayList<String> robots = new ArrayList<String>();
	private boolean afterAdapt = false, customGameStart = false, customGameRunning = false, fruitClicked = false;
	private long time_of_last_draw, current_time;
	private int kRADIUS = 5;
	private ArrayList<node_data> targets = new ArrayList<node_data>();
	private Hashtable<Integer, ArrayList<node_data>> missionControl = new Hashtable<Integer, ArrayList<node_data>>();
	
	/**
	 * constructor for the GUI class
	 */
	public MyGameGUI() {
		initGUI();
	}


	/**
	 * inits the GUI class with constent parameters
	 */
	private void initGUI() {
		this.setSize(width, height);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		MenuBar menuBar = new MenuBar();
		Menu menu = new Menu("Menu");
		menuBar.add(menu);
		this.setMenuBar(menuBar);

		MenuItem item1 = new MenuItem("Save As KML File");
		item1.addActionListener(this);

		MenuItem item2 = new MenuItem("New Custom Game");
		item2.addActionListener(this);

		MenuItem item3 = new MenuItem("New Auto Game");
		item3.addActionListener(this);

		menu.add(item1);
		menu.add(item2);
		menu.add(item3);


		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		g = new DGraph();
		algo = new Graph_Algo(g);

	}
	
	/**
	 * sets the rx and ry Range objects to the lowest and highest x and y values
	 * of the graph given by the game server, for further calculation.
	 */

	private void setRange() {
		Collection<node_data> c = g.getV();
		Iterator<node_data> itrV = c.iterator();
		while(itrV.hasNext()) {
			node_data n = itrV.next();
			Point3D p = n.getLocation();
			double x = p.x();
			double y = p.y();
			if(x<rx.get_min())
				rx.set_min(x);
			else if(x>rx.get_max())
				rx.set_max(x);
			if(y<ry.get_min())
				ry.set_min(y);
			else if(y>ry.get_max())
				ry.set_max(y);
		}
	}
	
	/**
	 * gets Point3D that cant be shown on the GUI and creates and returns fixed Point3D
	 * with x,y that can be shown on the GUI.
	 * @param pBefore
	 * @return
	 */

	public Point3D setScale(Point3D pBefore) {
		double offsetx = (pBefore.x() - rx.get_min())/(rx.get_max() - rx.get_min());
		double x = (width - 200) * offsetx + 100; 
		double offsety = (pBefore.y() - ry.get_min())/(ry.get_max() - ry.get_min());
		double y = (height - 200) * offsety;
		y = (height - 200 - y) + 100;
		Point3D pAfter = new Point3D(x, y);
		return pAfter;
	}
	
	/**
	 * this function does setScale on every node of the given graph
	 * so it can be shown on the gui.
	 */

	private void adaptRangeToGUI() {
		setRange();
		System.out.println("fixed rx: "+rx);
		System.out.println("fixed ry: "+ry);
		Collection<node_data> c = g.getV();
		Iterator<node_data> itrV = c.iterator();
		while(itrV.hasNext()) {
			node_data n = itrV.next();
			Point3D pBefore = n.getLocation();
			Point3D pAfter = setScale(pBefore);
			node_data fixedn = new Vertex(pAfter, n.getKey());
			DGraph dirG = (DGraph)g;
			dirG.fixNodeScale(fixedn);
			g = dirG;
		}
		if(g.nodeSize()>0)
			afterAdapt = true;

	}
	
	/**
	 * clears the gui window and calls the paint method
	 */


	public void repaint() {
		Graphics k;
		k = getGraphics();
		k.clearRect(0, 0, width, height);
		paint(k);
	}
	
	/**
	 * paints the graph, the robots, the fruits and the instructions of the game.
	 * this is the most importent method in this class.
	 */


	public void paint(Graphics k) {
		if(game != null)
			time_of_last_draw = game.timeToEnd();
		super.paint(k);
		if(!afterAdapt) {
			adaptRangeToGUI();
		}
		Image bufferimage= createImage(width, height);
		Graphics dbg= bufferimage.getGraphics();
		Font font = dbg.getFont().deriveFont((float) 16.5);
		dbg.setFont(font);
		Collection<node_data> c1 = g.getV();
		Iterator<node_data> itrV = c1.iterator();
		while(itrV.hasNext()) {
			node_data n = itrV.next();
			Point3D p = n.getLocation();
			dbg.setColor(Color.BLUE);
			dbg.fillOval((int)p.x() - kRADIUS, (int)p.y() - kRADIUS, 2 * kRADIUS, 2 * kRADIUS);
			dbg.drawString(n.getKey()+"", (int)p.x() - kRADIUS, (int)p.y() - kRADIUS-2);
			Collection<edge_data> c2 = g.getE(n.getKey());
			Iterator<edge_data> itrE = c2.iterator();
			dbg.setColor(Color.RED);
			while(itrE.hasNext()) {
				edge_data e = itrE.next();
				Point3D ps = g.getNode(e.getSrc()).getLocation();
				Point3D pf = g.getNode(e.getDest()).getLocation();
				dbg.drawLine(ps.ix(), ps.iy(), pf.ix(), pf.iy());
				dbg.setColor(Color.YELLOW);
				int xdir = (int)(0.8*pf.x() + 0.2*ps.x());
				int ydir = (int)(0.8*pf.y() + 0.2*ps.y());
				dbg.fillOval(xdir - kRADIUS , ydir - kRADIUS , 2 * kRADIUS, 2 * kRADIUS);
				xdir = (int)(0.7*pf.x() + 0.3*ps.x());
				ydir = (int)(0.7*pf.y() + 0.3*ps.y()-4);
				dbg.setColor(Color.RED);
				dbg.drawString(String.format("%.1f", e.getWeight()), xdir, ydir);
			}
		}

		for(int i = 0; i<robots.size(); i++) {
			try {
				String robot_json = robots.get(i);
				JSONObject line = new JSONObject(robot_json);
				JSONObject r = line.getJSONObject("Robot");
				String pos = r.getString("pos");
				Point3D pBefore = new Point3D(pos);
				Point3D pAfter = setScale(pBefore);
				kml_logger.addRobot(pBefore);
				final BufferedImage image = ImageIO.read(new File("data/robot.png"));
				dbg.drawImage(image, pAfter.ix() - 3*kRADIUS, pAfter.iy() - 3*kRADIUS, null);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for(int i = 0; i<fruits.size(); i++) {
			try {
				String fruit_json = fruits.get(i);
				JSONObject line = new JSONObject(fruit_json);
				JSONObject f = line.getJSONObject("Fruit");
				String pos = f.getString("pos");
				int type = f.getInt("type");
				Point3D pBefore = new Point3D(pos);
				Point3D pAfter = setScale(pBefore);
				final BufferedImage image;
				if(type<0) {
					image = ImageIO.read(new File("data/banana.png"));
					kml_logger.addFruit(-1, pBefore);
				}
				else {
					image = ImageIO.read(new File("data/apple.png"));
					kml_logger.addFruit(1, pBefore);
				}
				dbg.drawImage(image, pAfter.ix() - 3*kRADIUS, pAfter.iy() - 3*kRADIUS, null);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if(customGameStart && targets.size()<numOfRobots) {
			dbg.setColor(Color.BLACK);
			if(numOfRobots==1) {
				dbg.drawString("INSTRUCTIONS:", 50, 70);
				dbg.drawString("Choose a node location for your robot", 50, 90);
			}else {
				dbg.drawString("INSTRUCTIONS:", 50, 70);
				dbg.drawString("Choose "+(numOfRobots-targets.size())+" node locations for your robots", 50, 90);
			}
		}
		if(customGameRunning && game.isRunning()) {
			dbg.setColor(Color.BLACK);
			dbg.drawString("INSTRUCTIONS:", 50, 70);
			dbg.drawString("click on any robot and then choose a fruit for him to eat!", 50, 90);
			dbg.drawString("click on the screen when the game is over", 50, 110);

		}
		
		if(!customGameRunning && !robots.isEmpty() && game!=null && game.isRunning()) {
			dbg.setColor(Color.BLACK);
			dbg.drawString("INSTRUCTIONS:", 50, 70);
			dbg.drawString("click on the screen when the game is over", 50, 90);
		}

		if(game!= null && game.isRunning()) {
			dbg.setColor(Color.BLACK);
			dbg.drawString("Time Untill The End Of The Game: "+(game.timeToEnd()/1000), 700, 80);
			String info = game.toString();
			JSONObject line;
			try {
				line = new JSONObject(info);
				JSONObject ttt = line.getJSONObject("GameServer");
				grade = ttt.getInt("grade");
			} catch (Exception e1) {e1.printStackTrace();}
			dbg.drawString("Current Score: "+ grade, 1150, 80);
		}
		if(customGameRunning && !game.isRunning() || !robots.isEmpty() && game!=null && !game.isRunning()) {
//			kml_logger.create_kml("data/kml files/"+scenario+".kml");
			dbg.setColor(Color.RED);
			font = dbg.getFont().deriveFont((float) 30);
			dbg.setFont(font);
			dbg.drawString("GAME OVER!", width/2 - 100, height/2);
			String info = game.toString();
			JSONObject line;
			try {
				line = new JSONObject(info);
				JSONObject ttt = line.getJSONObject("GameServer");
				grade = ttt.getInt("grade");
			} catch (Exception e1) {e1.printStackTrace();}
			dbg.drawString("Final Score: "+ grade, width/2 - 100, height-500);
			font = dbg.getFont().deriveFont((float) 16.5);
			dbg.setFont(font);
		}
		k.drawImage(bufferimage,0,0,this);
		dbg.dispose();
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		String str = e.getActionCommand();

		if (str.equals("Save As KML File")) {
			FileDialog chooser = new FileDialog(this, "Save Your Game", FileDialog.SAVE);
			chooser.setVisible(true);
			String filename = chooser.getFile();
			String path = chooser.getDirectory();
			System.out.println(filename);
			if(filename!=null) {
				System.out.println(path);
				System.out.println(filename);
				kml_logger.create_kml(path + filename + ".kml");
			}
		}
		else if(str.equals("New Custom Game")) {
			clear();
			customGameStart = true;
			scenario = Integer.parseInt(JOptionPane.showInputDialog("Enter senario number between 0-23"));
			kml_logger = new KML_Logger("user-scenario: "+scenario);
			game = Game_Server.getServer(scenario); // you have [0,23] games
			String gr = game.getGraph();
			DGraph dg = new DGraph();
			dg.init(gr);
			this.g = dg;
			kml_logger.addGraph(g);
			fruits = (ArrayList<String>) game.getFruits();
			String info = game.toString();
			JSONObject line;
			try {
				line = new JSONObject(info);
				JSONObject ttt = line.getJSONObject("GameServer");
				numOfRobots = ttt.getInt("robots");
			} catch (Exception e1) {e1.printStackTrace();}
			repaint();
		}
		else if(str.equals("New Auto Game")) {
			clear();
			scenario = Integer.parseInt(JOptionPane.showInputDialog("Enter senario number between 0-23"));
			kml_logger = new KML_Logger("Auto-scenario: "+scenario);
			game = Game_Server.getServer(scenario); // you have [0,23] games
			String gr = game.getGraph();
			DGraph dg = new DGraph();
			dg.init(gr);
			this.g = dg;
			kml_logger.addGraph(g); 
			algo.init(g);
			AutoGame_Thread auto = new AutoGame_Thread(game, fruits, robots, this);
			auto.start();
		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		System.out.println("mouseClicked");
	}
	

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		node_data toChoose = null;
		Point3D temp = new Point3D(x,y);
		double min_dist = (kRADIUS * 4);
		double best_dist = 100000;
		Collection<node_data> c = g.getV();
		Iterator<node_data> itr = c.iterator();
		while(itr.hasNext()) {
			node_data n = itr.next();
			Point3D p = n.getLocation();
			double dist = temp.distance2D(p);
			if(dist<min_dist && dist<best_dist) {
				best_dist = dist;
				toChoose = n;
			}
		}
		if(getEdgeOfFruit(temp)!=null) {
			fruitClicked = true;
		}
		if(toChoose!=null) {
			System.out.println("robot clicked!");
			targets.add(toChoose);
		}
		if(customGameRunning && targets.size()==2) {
			targets.remove(1);
		}
		if(customGameStart && targets.size()==numOfRobots)
			userGame();
		repaint();

		System.out.println("mousePressed");
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		System.out.println("mouseReleased");
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		System.out.println("mouseEntered");

	}

	@Override
	public void mouseExited(MouseEvent e) {
		System.out.println("mouseExited");
	}

	@Override
	public void mouseDragged(MouseEvent mouseEvent) {

	}

	@Override
	public void mouseMoved(MouseEvent mouseEvent) {

	}


	public graph getGraph() {
		return g;
	}

	public static void main(String[] args) {
		MyGameGUI gameGUI = new MyGameGUI();
		gameGUI.setVisible(true);	
	}
	
	/**
	 * this method starts the custom game.
	 * it uses the moveRobots method in a thread so the user can play the game
	 * and change games while it is still running.
	 */

	public void userGame() {
		for(int i = 0;i<numOfRobots;i++) {
			game.addRobot(targets.get(i).getKey());
		}
		customGameStart = false;
		customGameRunning = true;
		targets.clear();
		System.out.println(game.getFruits());

		game.startGame();

		Runnable r = new Runnable() {

			@Override
			public void run() {
				while(game.isRunning()) {
					myMoveRobots();
				}			
			}
		};
		Thread move = new Thread(r);
		move.start();
		String results = game.toString();
		System.out.println("Game Over: "+results);


	}
	
	/**
	 * sets all the objets and parameters of this class for a new game.
	 */


	private void clear() {
		game = null;
		rx = new Range(Integer.MAX_VALUE,Integer.MIN_VALUE);
		ry = new Range(Integer.MAX_VALUE,Integer.MIN_VALUE);
		fruits.clear();
		robots.clear();
		afterAdapt = false;
		time_of_last_draw = -1;
		current_time = -1;
		customGameStart = false;
		customGameRunning = false;
		fruitClicked = false;
		fruit_type = 0;
		targets.clear();
	}
	
	/**
	 * this function move the robots in the game.
	 * if the dest param of a robot is -1 it means that it is not moving and
	 * it will wait until it gets a new mission (fruit to eat) by the user.
	 */

	public void myMoveRobots() {
		algo.init(g);
		List<String> log = game.move();
		robots = (ArrayList<String>) log;
		fruits = (ArrayList<String>) game.getFruits();
		if(log!=null) {
			if(time_of_last_draw<0)
				repaint();
			current_time = game.timeToEnd();
			if(time_of_last_draw-current_time>100) {
				repaint();
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

						if(dest==-1 && targets.size()==1 && targets.get(0).getKey() == src && fruitClicked) {
							fruitClicked = false;
							missionControl.put(rid, (ArrayList<node_data>) getMissionList(src, edge_of_fruit));
							System.out.println("mission enterd! from "+src+" to "+edge_of_fruit+" for robot: "+rid);
							targets.clear();
							edge_of_fruit = null;
							fruit_type = 0;
						}
						if(dest!=-1) {
							game.chooseNextEdge(rid, dest);
							System.out.println("Turn to node: "+dest+"  time to end:"+(current_time/1000));
							System.out.println(ttt);
						}
					}
				} 
				catch (JSONException e) {e.printStackTrace();}
			}
			if(targets.size()==2)
				targets.clear();
		}
	}
	
	/**
	 * gets a location of a fruit in the graph and returns the edge of this fruit.
	 * if the location is not a location of a fruit it will return null.
	 * @param pos
	 * @return
	 */

	public edge_data getEdgeOfFruit(Point3D pos) {
		Point3D p = isFruit(pos);
		if(p==null) {
			return null;
		}
		Collection<node_data> c = g.getV();
		Iterator<node_data> itr = c.iterator();
		while(itr.hasNext()) {
			node_data n = itr.next();
			Collection<edge_data> e = g.getE(n.getKey());
			Iterator<edge_data> itr2 = e.iterator();
			while(itr2.hasNext()) {
				edge_data edge = itr2.next();
				node_data src = g.getNode(edge.getSrc());
				node_data dest = g.getNode(edge.getDest());
				Point3D src_location = src.getLocation();
				Point3D dest_location = dest.getLocation();
				double fulldest = src_location.distance2D(dest_location);
				double dest1 = src_location.distance2D(p);
				double dest2 = p.distance2D(dest_location);
				double maxDest = Math.max(fulldest, dest1+dest2);
				double minDest = Math.min(fulldest, dest1+dest2);
				if((maxDest-minDest)<EPSILON) {
					edge_of_fruit = edge;
					return edge;
				}
			}
		}
		return null;
	}
	
	/**
	 * gets a location and checks if this location is a location of a fruit.
	 * @param p
	 * @return
	 */

	public Point3D isFruit(Point3D p) {
		Collection<String> fruits = game.getFruits();
		Iterator<String> itr = fruits.iterator();
		while(itr.hasNext()) {
			try {
				String info = itr.next();
				JSONObject line = new JSONObject(info);
				JSONObject fruit = line.getJSONObject("Fruit");
				int t = fruit.getInt("type");
				Point3D p2 = setScale(new Point3D(fruit.getString("pos")));
				double dest = p.distance2D(p2);
				if(dest<20) {
					fruit_type = t;
					return p2;
				}
			} catch(Exception e) {e.printStackTrace();}
		}
		return null;
	}
	
	/**
	 * gets src of a robot end the an edge of a fruit and creates a List of nodes which
	 * will be the mission of this robot.
	 * @param src
	 * @param e
	 * @return
	 */
	
	public List<node_data> getMissionList(int src, edge_data e){
		ArrayList<node_data> ans = null;
		if(fruit_type==1) {
			ans = (ArrayList<node_data>) algo.shortestPath(src, Math.min(e.getSrc(), e.getDest()));
			ans.add(g.getNode(Math.max(e.getSrc(), e.getDest())));
		}
		if(fruit_type==-1) {
			ans = (ArrayList<node_data>) algo.shortestPath(src, Math.max(e.getSrc(), e.getDest()));
			ans.add(g.getNode(Math.min(e.getSrc(), e.getDest())));
		}
		return ans;
	}
	
	/**
	 * gets src of a robot and edge of a fruit and returns the shortest destination from
	 * the robot to the fruit.
	 * @param src
	 * @param e
	 * @return
	 */
	
	public double getMissionDist(int src, edge_data e){
		double ans = 0;
		if(fruit_type==1) {
			ans = algo.shortestPathDist(src, Math.min(e.getSrc(), e.getDest())) + e.getWeight() ;
		}
		if(fruit_type==-1) {
			ans = algo.shortestPathDist(src, Math.max(e.getSrc(), e.getDest())) + e.getWeight();
		}
		return ans;
	}
}
