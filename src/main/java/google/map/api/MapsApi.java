package google.map.api;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.educationalProject.surfacePathfinder.visualization.DrawingUtils;
import org.jgrapht.graph.SimpleWeightedGraph;

import bachelorThesisPlayground.Vertex;
import bachelorThesisPlayground.deprecated.BackloggedCycleResolution;
import bachelorThesisPlayground.deprecated.IsolatedZoneWithSingletonInOut;
import bachelorThesisPlayground.graphBuilding.GraphBuilding;
import bachelorThesisPlayground.readers.DBReader;
import bachelorThesisPlayground.water.flow.ConsumptionCalculator;
import bachelorThesisPlayground.water.flow.WaterFlow;

public class MapsApi {

	private static String key = "";
	
	public static String prepareForGoogleStatics(List<String> addresses){
	    String result = "";
	    for (String addr : addresses) {
	    	result += "&markers=size:big%7Ccolor:green%7C" + prepareForGoogleStatics(addr) + ",Saint+Petersburg";
	    }
	    return result;
	}
	
	public static String prepareForGoogleStatics(String message){
	    char[] abcCyr =   {'0','1','2','3','4','5','6','7','8','9','_',' ','.','à','á','â','ã','ä','å','¸', 'æ','ç','è','é','ê','ë','ì','í','î','ï','ð','ñ','ò','ó','ô','õ', 'ö','÷', 'ø','ù','ú','û','ü','ý', 'þ','ÿ','À','Á','Â','Ã','Ä','Å','¨', 'Æ','Ç','È','É','Ê','Ë','Ì','Í','Î','Ï','Ð','Ñ','Ò','Ó','Ô','Õ', 'Ö', '×','Ø', 'Ù','Ú','Û','Ü','Ý','Þ','ß','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	    String[] abcLat = {"0","1","2","3","4","5","6","7","8","9","+","+","+","a","b","v","g","d","e","e","zh","z","i","y","k","l","m","n","o","p","r","s","t","u","f","h","ts","ch","sh","sch", "","i", "","e","ju","ja","A","B","V","G","D","E","E","Zh","Z","I","Y","K","L","M","N","O","P","R","S","T","U","F","H","Ts","Ch","Sh","Sch", "","I", "","E","Ju","Ja","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
	    String result = "";
	    for (int i = 0; i < message.length(); i++) {
	        for (int x = 0; x < abcCyr.length; x++ ) {
	            if (message.charAt(i) == abcCyr[x]) {
	            	if (abcLat[x].equals("+") && result.charAt(result.length()-1) == '+') {
	            		continue;
	            	}
	            	result += abcLat[x];
	            }
	        }
	    }
	    return result;
	}
	
	public static void renderAffectedConsumers(List<Vertex> consumers) throws IOException {
        JFrame test = new JFrame("Google Maps");

        List<String> addresses = consumers.stream().map(c->c.address).collect(Collectors.toList());
        
        try {
            String imageUrl = "https://maps.googleapis.com/maps/api/staticmap?" + 
            		//"center=Saint-Petersburg" + 
            		"center=" + prepareForGoogleStatics(addresses) + ",Saint+Petersburg" + 
            		"&size=800x800" + 
            		"&style=element:labels|visibility:off" + 
            		"&style=element:geometry.stroke|visibility:off" + 
            		"&style=feature:landscape|element:geometry|saturation:-100" + 
            		"&style=feature:water|saturation:-100|invert_lightness:true" + 
            		prepareForGoogleStatics(addresses) + 
            		"&key=" + key;
            System.out.println("request to " + imageUrl);
            String destinationFile = "image.jpg";
            String str = destinationFile;
            URL url = new URL(imageUrl);
            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(destinationFile);

            byte[] b = new byte[2048];
            int length;

            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        test.add(new JLabel(new ImageIcon((new ImageIcon("image.jpg")).getImage().getScaledInstance(630, 600,
                java.awt.Image.SCALE_SMOOTH))));

        test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        test.setVisible(true);
        test.pack();

    }
	
}



