package org.educationalProject.surfacePathfinder.visualization;

public class DisplayMode {
	private static String mode = "screen";
	public static void setMode(String mode_){
		mode = mode_;
	};
	public static double getStrokeWidth(){
		if("screen".equals(mode)){
			return 1;
		} else {
			return 4;
		}
	}
	
	public static double getBigPointSize(){
		if("screen".equals(mode)){
			return 2;
		} else {
			return 10;
		}
	}
	
	public static double getSmallPointSize(){
		if("screen".equals(mode)){
			return 1.5;
		} else {
			return 6;
		}
	}

	public static double getPolygonDiameter(){
		if("screen".equals(mode)){
			return 4;
		} else {
			return 10;
		}
	}
}
