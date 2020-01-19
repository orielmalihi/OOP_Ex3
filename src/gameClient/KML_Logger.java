package gameClient;

import java.io.File;
import java.io.FileNotFoundException;
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

public class KML_Logger {
	
	private Kml kml;
	private Document doc;
	
	
	public KML_Logger(String app_name) {
		kml = new Kml();
		doc = kml.createAndSetDocument().withName(app_name).withOpen(true);
		System.out.println("doc :"+doc);
	}
	
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
	
	public void addRobot(Point3D p) {
		LocalDateTime time = LocalDateTime.now();
		Placemark robot = doc.createAndAddPlacemark();
		Icon icon = new Icon().withHref("http://maps.google.com/mapfiles/kml/pushpin/wht-pushpin.png");
		robot.createAndAddStyle().createAndSetIconStyle().withScale(1.0).withIcon(icon);
		robot.createAndSetPoint().addToCoordinates(p.x(), p.y());
		robot.createAndSetTimeStamp().withWhen(time.toString());
		
	}
	
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
	
	public void create_kml(String name) {
		try {
			kml.marshal(new File(name));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
