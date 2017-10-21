package bachelorThesisPlayground.writers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bachelorThesisPlayground.Edge;

public class WriterForGephy {
	public static void writeCSV(Map<Integer, Edge> edges){
		Set<Integer> pointIds = new HashSet<>();
		for (Edge e: edges.values()) {
			if(!pointIds.contains(e.a.id)) pointIds.add(e.a.id);
			if(!pointIds.contains(e.b.id)) pointIds.add(e.b.id);
		}
		Integer[] array = pointIds.toArray(new Integer[pointIds.size()]);
		List<Integer> list = Arrays.asList(array);
		Collections.sort(list);
		double[][] matrix = new double[list.size()][list.size()];
		for(Edge e : edges.values()){
			matrix[e.a.id][e.b.id] = e.length;
			matrix[e.b.id][e.a.id] = e.length;
		}
		
		try {

			PrintWriter writer = new PrintWriter("C:\\Users\\test\\Desktop\\диплом\\graph_for_gephy.csv", "UTF-8");
			writer.print(";");
			for(int i = 0; i < list.size(); i++){
				writer.print("i");
				writer.print(list.get(i));
				if(i!=list.size()-1)writer.print(";");
			}	
			writer.println();
			
			for(int i = 0; i < list.size(); i++){
				writer.print("i");
				writer.print(list.get(i));
				writer.print(";");
				for(int j = 0; j < list.size(); j++){
					writer.print(matrix[i][j]);
					if(j!=list.size()-1)writer.print(";");
				}
				writer.println();
			}
			writer.close();			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
