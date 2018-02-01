package bachelorThesisPlayground.graphBuilding;

import java.util.Iterator;
import java.util.Map;

import bachelorThesisPlayground.Edge;
import bachelorThesisPlayground.normalizers.ConvergenceImprover;
import bachelorThesisPlayground.normalizers.Normalizer;
import bachelorThesisPlayground.readers.CSVGraphReader;
import bachelorThesisPlayground.readers.ExcelGraphReader;
import bachelorThesisPlayground.writers.JSONWriter;

public class D3 {

	//������ �����, ����� � ����� ���� ��� d3 force v4
	static void writeJSONGraphForD3() {
		//����� ����� ����� � �������������� �� �����-������
		Map<Integer, Edge> edges = ExcelGraphReader.getEdgeSkeletons("C:\\Users\\test\\Desktop\\������\\�����_���_�_�����.xlsx");
		//����� �������������� ��������� ����� - �����, ������� � ��
		ExcelGraphReader.populateEdgeParameters(edges, "C:\\Users\\test\\Desktop\\������\\����.xlsx");
		//����� ���������� �����-������ �����
		CSVGraphReader.populateVertexCoordinates(edges, "C:\\Users\\test\\Desktop\\������\\����� � ������������.csv");
		//GraphReader.filterUnusedEdges(edges);
		
		ExcelGraphReader.populatePointsWithParameters(edges, "C:\\Users\\test\\Desktop\\������\\����.xlsx");
		
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
