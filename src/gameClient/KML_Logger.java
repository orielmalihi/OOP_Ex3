package gameClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Iterator;

import dataStructure.edge_data;
import dataStructure.graph;
import dataStructure.node_data;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import utils.Point3D;
/**
 * this class represents a way to export the data of a game to a KML file.
 * this enable the user to see the result of the game in apps like google-eart and more.
 * more about kml: https://developers.google.com/kml/documentation/kml_tut
 * @author oriel
 *
 */
public class KML_Logger {

	private Kml kml;
	private String kml_String = "";
	private Document doc;

	/**
	 * crates the kml logger.
	 * @param app_name
	 */
	public KML_Logger(String app_name) {
		kml = new Kml();
		doc = kml.createAndSetDocument().withName(app_name).withOpen(true);
	}

	/**
	 * adds a graph to the kml.
	 * the nodes will be shown as blue-pushpin.
	 * @param g
	 */

	public void addGraph(graph g) {
		Collection<node_data> c = g.getV();
		Iterator<node_data> itr = c.iterator();
		while(itr.hasNext()) {
			Placemark point = doc.createAndAddPlacemark();
			Icon icon = new Icon().withHref("http://maps.google.com/mapfiles/kml/pushpin/blue-pushpin.png");
			point.createAndAddStyle().createAndSetIconStyle().withScale(1.0).withIcon(icon);
			node_data n = itr.next();
			Point3D p = n.getLocation();
			point.createAndSetPoint().addToCoordinates(p.x(),p.y());
			Collection<edge_data> edges = g.getE(n.getKey());
			Iterator<edge_data> itr2 = edges.iterator();
			while(itr2.hasNext()) {
				Placemark line = doc.createAndAddPlacemark();
				edge_data e = itr2.next();
				Point3D src = g.getNode(e.getSrc()).getLocation();
				Point3D dest = g.getNode(e.getDest()).getLocation();
				line.createAndSetLineString().addToCoordinates(src.x(), src.y()).addToCoordinates(dest.x(), dest.y());
			}
		}
	}

	/**
	 * adds a robot to the kml.
	 * the robot will be shown as white-pushpin.
	 * the robot will also have a time-stamp which will enable the
	 * user to see where the robot was in each moment of the game.
	 * @param g
	 */

	public void addRobot(Point3D p) {
		LocalDateTime time = LocalDateTime.now();
		Placemark robot = doc.createAndAddPlacemark();
		Icon icon = new Icon().withHref("http://maps.google.com/mapfiles/kml/pushpin/wht-pushpin.png");
		robot.createAndAddStyle().createAndSetIconStyle().withScale(1.0).withIcon(icon);
		robot.createAndSetPoint().addToCoordinates(p.x(), p.y());
		robot.createAndSetTimeStamp().withWhen(time.toString());

	}

	/**
	 * adds a fruit to the kml.
	 * the fruit will be shown as yellow-pushpin if it is a banana or
	 * a red-pushpin if it is an apple.
	 * the fruit will also have a time-stamp which will enable the
	 * user to see where the fruit was in each moment of the game.
	 * @param g
	 */

	public void addFruit(int type, Point3D p) {
		LocalDateTime time = LocalDateTime.now();
		Placemark fruit = doc.createAndAddPlacemark();
		Icon icon = new Icon();
		if(type == -1)
			icon.withHref("http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png");
		else
			icon.withHref("http://maps.google.com/mapfiles/kml/pushpin/red-pushpin.png");
		fruit.createAndAddStyle().createAndSetIconStyle().withScale(1.0).withIcon(icon);
		fruit.createAndSetPoint().addToCoordinates(p.x(), p.y());
		fruit.createAndSetTimeStamp().withWhen(time.toString());	
	}

	/**
	 * crates the kml file with the param name and returns a String 
	 * representing this kml file.
	 * @param name
	 */

	public String create_kml(String path) {
		try {
			kml.marshal(new File(path));
			initKml_String(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return kml_String;
	}
	
	/**
	 * inits a String that represnts this kml
	 * @param path
	 */

	private void initKml_String(String path) {
		File fileName = new File(path);
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName)); 

			String st; 
			while ((st = br.readLine()) != null) {
				kml_String += st;
			}
		} catch (Exception e) {e.printStackTrace();}
	}
}
