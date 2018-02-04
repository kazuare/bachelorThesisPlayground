package org.educationalProject.surfacePathfinder.visualization;

public class DisplayMode {
	public static double getStrokeWidth(String mode){
		if("screen".equals(mode)){
			return 1;
		} else if ("focused screen".equals(mode)){
			return 3;
		} else {
			return 4;
		}
	}
	
	public static double getBigPointSize(String mode){
		if("screen".equals(mode)){
			return 2;
		} else if ("focused screen".equals(mode)){
			return 4;
		} else {
			return 12;
		}
	}
	
	public static double getSmallPointSize(String mode){
		if("screen".equals(mode)){
			return 1;
		} else if ("focused screen".equals(mode)){
			return 3;
		} else {
			return 6;
		}
	}

}
