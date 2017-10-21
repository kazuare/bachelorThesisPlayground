package bachelorThesisPlayground.normalizers;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import bachelorThesisPlayground.Edge;

public class Normalizer {
	public static void normalizeIds(Map<Integer, Edge> edges){
		Set<Integer> pointIds = new HashSet<>();
		for (Edge e: edges.values()) {
			if(!pointIds.contains(e.a.id)) pointIds.add(e.a.id);
			if(!pointIds.contains(e.b.id)) pointIds.add(e.b.id);
		}
		Integer[] array = pointIds.toArray(new Integer[pointIds.size()]);
		List<Integer> list = Arrays.asList(array);
		Collections.sort(list);
		
		for (Edge e: edges.values()) {
			int newId;
			newId= Collections.binarySearch(list, e.a.id);
			System.out.println(e.a.id + " --->> " + newId);
			e.a.id = newId;
			
			newId= Collections.binarySearch(list, e.b.id);
			System.out.println(e.b.id + " --->> " + newId);
			e.b.id = newId;
			
		}
	}
	public static void normalizeCoords(Map<Integer, Edge> edges){
		double minX = Math.min(
			edges.values().stream().map(e->e.a).filter(p->p.x>0).mapToDouble(p->{return p.x;}).min().getAsDouble(),
			edges.values().stream().map(e->e.b).filter(p->p.x>0).mapToDouble(p->{return p.x;}).min().getAsDouble()
		);
		double minY = Math.min(
			edges.values().stream().map(e->e.a).filter(p->p.y>0).mapToDouble(p->{return p.y;}).min().getAsDouble(),
			edges.values().stream().map(e->e.b).filter(p->p.y>0).mapToDouble(p->{return p.y;}).min().getAsDouble()
		);
		for (Edge e : edges.values()) {
			if (e.a.x > 0 && e.a.y > 0) {
				System.out.print(e.a.x + " " + e.a.y + " -->> ");
				e.a.x -= minX;
				e.a.y -= minY;
				
				e.a.x /= 1000;
				e.a.y /= 1000;
				
				System.out.println(e.a.x + " " + e.a.y);
			}
			if (e.b.x > 0 && e.b.y > 0) {
				System.out.print(e.b.x + " " + e.b.y + " -->> ");
				e.b.x -= minX;
				e.b.y -= minY;

				e.b.x /= 1000;
				e.b.y /= 1000;
				
				System.out.println(e.b.x + " " + e.b.y);
			}
		}
	}
}
