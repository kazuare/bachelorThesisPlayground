package org.educationalProject.surfacePathfinder.visualization;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
/**
* Base class for every visualizer. it is given to any window class in order to display something.
* Visualizers can resize their windows if needed
*/
public abstract class Visualizer {
	protected boolean dataSet = false;
	protected int width;
	protected int height;
	public String name;
	/**
	* Is invoked when window init or reshape occurs
	* Does some geometry stuff
	*/
    protected void setup( GL2 gl2, int width, int height ) {
        gl2.glMatrixMode( GL2.GL_PROJECTION );
        gl2.glLoadIdentity();

        GLU glu = new GLU();
        glu.gluOrtho2D( 0.0f, width, 0.0f, height );

        gl2.glMatrixMode( GL2.GL_MODELVIEW );
        gl2.glLoadIdentity();

        gl2.glViewport( 0, 0, width, height );
    }
    /**
	* Sets width and height variables so all the methods can use it
	*/
    private void setResolution(int width, int height){
    	this.width = width;
    	this.height = height;
    }
    /**
	* Checks if there is data to display and then displays it
	*/
    protected void checkAndDisplay( GL2 gl2, int width, int height ){
    	if(dataSet){
    		setResolution(width, height);
    		display(gl2);
    	}
    	else System.out.println("Warning: no data is set");
    }

    protected abstract void display( GL2 gl2 );
    
    protected void screenshot(GL2 gl){
    	ByteBuffer pixelsRGB = Buffers.newDirectByteBuffer(width * height * 3);

        gl.glReadBuffer(GL.GL_BACK);
        gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);

        gl.glReadPixels(0, 0, width, height, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, pixelsRGB);

        int[] pixels = new int[width * height];

        int firstByte = width * height * 3;
        int sourceIndex;
        int targetIndex = 0;
        int rowBytesNumber = width * 3;

        for (int row = 0; row < height; row++) {
            firstByte -= rowBytesNumber;
            sourceIndex = firstByte;
            for (int col = 0; col < width; col++) {
                if (pixelsRGB.get(sourceIndex) != 0) {
                    System.out.println(sourceIndex);
                }

                int iR = pixelsRGB.get(sourceIndex++);
                int iG = pixelsRGB.get(sourceIndex++);
                int iB = pixelsRGB.get(sourceIndex++);

                pixels[targetIndex++] = 0xFF000000
                        | ((iR & 0x000000FF) << 16)
                        | ((iG & 0x000000FF) << 8)
                        | (iB & 0x000000FF);
            }

        }

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);

        try {
            ImageIO.write(bufferedImage, "PNG", new File("C:\\users\\test\\desktop\\"+name+".png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
		
    }
}