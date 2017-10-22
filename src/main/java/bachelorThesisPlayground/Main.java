package bachelorThesisPlayground;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.educationalProject.surfacePathfinder.visualization.DisplayMode;
import org.educationalProject.surfacePathfinder.visualization.NetworkVisualizer;
import org.educationalProject.surfacePathfinder.visualization.Screenshooter;
import org.educationalProject.surfacePathfinder.visualization.SwingWindow;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import bachelorThesisPlayground.normalizers.ConvergenceImprover;
import bachelorThesisPlayground.normalizers.Normalizer;
import bachelorThesisPlayground.readers.CSVGraphReader;
import bachelorThesisPlayground.readers.ExcelGraphReader;
import bachelorThesisPlayground.readers.JsonGraphReader;
import bachelorThesisPlayground.writers.JSONWriter;

public class Main {

	public static void main(String[] args) {
		//writeJSONGraphForD3();
		//we do layout in d3 and get FINAL_ALL_POINTS.json
		
		ArrayList<Vertex> points = JsonGraphReader.readNodes("C:\\Users\\test\\Desktop\\������\\FINAL_ALL_POINTS.json");
		ArrayList<Edge> edges = JsonGraphReader.readEdges("C:\\Users\\test\\Desktop\\������\\d3full.json");

		HashMap<Integer,Edge> idToEdge = new HashMap<>(edges.size());
		for(Edge e : edges)
			idToEdge.put(e.id, e);
		
		ExcelGraphReader.filterUnusedEdges(idToEdge);
		ConvergenceImprover.removeSmallConnectedComponents(idToEdge, true, 16);
		
		edges = new ArrayList<Edge>(idToEdge.values());
		Collections.sort(edges, (a,b)->Integer.compare(a.id, b.id));
		
		Set<Vertex> usedPoints = new HashSet<Vertex>(points.size());
		for (Edge e : edges){
			usedPoints.add(e.a);
			usedPoints.add(e.b);		
		}
		
		points.removeIf(v->!usedPoints.contains(v));
		
	    double minX = points.stream().mapToDouble(p->p.x).min().getAsDouble();
	    double minY = points.stream().mapToDouble(p->p.y).min().getAsDouble();
	   
	    points.stream()
		    .forEach(v->{
		    	v.x -= minX;
		    	v.y -= minY;
		    });
		
		DefaultDirectedWeightedGraph<Vertex,Edge> graph = new DefaultDirectedWeightedGraph<Vertex,Edge>(Edge.class);
		
		HashMap<Integer, Vertex> idToVertex = new HashMap<Integer, Vertex>(points.size());
		for (Vertex p : points){
			idToVertex.put(p.id, p);
			graph.addVertex(p);
		}
		
		for (Edge e: edges) {
			e.a = idToVertex.get(e.a.id);
			e.b = idToVertex.get(e.b.id);
			graph.addEdge(e.a, e.b, e);
			System.out.println("filling graph with edge: " + e);
		}
		
		//DefaultDirectedWeightedGraph<Vertex,Edge> graph = new DefaultDirectedWeightedGraph<Vertex,Edge>(Edge.class);
		//graph.addVertex(new Vertex(2,3,4));
		
		DisplayMode.setMode("screenshot");
		
		NetworkVisualizer visScreenshot = new NetworkVisualizer()
				.setData(graph)
				.setDefaultWidth(10000)
				.calculateWeightAndHeight();
		Screenshooter.start(visScreenshot, visScreenshot.getWidth() + 50, visScreenshot.getHeight()+ 50);

		DisplayMode.setMode("screen");
		
		NetworkVisualizer vis = new NetworkVisualizer()
				.setData(graph)
				.setDefaultWidth(800)
				.calculateWeightAndHeight();
		SwingWindow.start(vis, vis.getWidth() + 50, vis.getHeight()+ 50, "pipes");
		
	}
	
	//������ �����, ����� � ����� ���� ��� d3 force v4
	static void writeJSONGraphForD3() {
		//����� ����� ����� � �������������� �� �����-������
		Map<Integer, Edge> edges = ExcelGraphReader.getEdgeSkeletons("C:\\Users\\test\\Desktop\\������\\2016_07_25_�����_���_�_�����_4_��������.xlsx");
		//����� �������������� ��������� ����� - �����, ������� � ��
		ExcelGraphReader.populateEdgeParameters(edges, "C:\\Users\\test\\Desktop\\������\\2016_07_25_����_4_��������_�����.xlsx");
		//����� ���������� �����-������ �����
		CSVGraphReader.populateVertexCoordinates(edges, "C:\\Users\\test\\Desktop\\������\\����� ���� ����� � ������������.csv");
		//GraphReader.filterUnusedEdges(edges);
		
		//� ����� ���� ��������� ����������� �������� �����, ����������
		Iterator<Integer> it = edges.keySet().iterator();
		while(it.hasNext()){
			Integer id = it.next();
			if(edges.get(id).diameter == 0)
				it.remove();
		}		
		
		//������� ���������� ���������, ��� ��� �� ����� ������������� �����
		//������� ����� ������� ��������� ����������
		ConvergenceImprover.removeSmallConnectedComponents(edges, false, 50);
		
		//�������� �������������� ����� � ���� 0...n-1
		Normalizer.normalizeIds(edges);
		
		//�������� ���������� � ��������� [0,m_x/m_y],
		//�������� ��������������� ���������
		Normalizer.normalizeCoords(edges);
		
		//�������� ���������� d3 � ������� �������������� ����
		//<<��� ��������������� ����� ������ � ����������� ������� ���������������>>
		//�������� ������: ������ �������� fixed � ������
		ConvergenceImprover.assignCoordsOld(edges);
		
		/* �������� ��� �����, � ������� ����� ���������� ������ ���������
		int threshold = 3000;
		Iterator<Map.Entry<Integer, Edge>> iter = edges.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<Integer, Edge> entry = iter.next();
			if(entry.getValue().a.x > threshold || entry.getValue().b.x > threshold 
					|| entry.getValue().a.y > threshold || entry.getValue().b.y > threshold
					)		
				iter.remove();	
		}
		*/
		
		//����������� ������ �����, ��� �3
		JSONWriter.write(edges, "C:\\Users\\test\\Desktop\\������\\d3.json");
		//����� ������ ������ �����, ����� ������ ��� ��������
		JSONWriter.writeFull(edges, "C:\\Users\\test\\Desktop\\������\\d3full.json");
		
		System.out.println(edges.values().stream().mapToDouble(e->Math.max(e.a.x, e.b.x)).max().getAsDouble());
		System.out.println(edges.values().stream().mapToDouble(e->Math.max(e.a.y, e.b.y)).max().getAsDouble());
		
	}
	
}
