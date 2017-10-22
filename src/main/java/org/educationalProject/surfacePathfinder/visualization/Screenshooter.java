package org.educationalProject.surfacePathfinder.visualization;

import com.jogamp.opengl.GLProfile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
/**
* Swing window class. Can be used in a static way.
*/
public class Screenshooter {

    public static void start(Visualizer visualizer, int width, int height){
        GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities( glprofile );
        glcapabilities.setOnscreen(false);

        GLDrawableFactory factory = GLDrawableFactory.getFactory(glprofile);
        GLOffscreenAutoDrawable drawable = factory.createOffscreenAutoDrawable(null,glcapabilities,null,width,height);
        drawable.addGLEventListener(new GLEventListener() {
            public void init(GLAutoDrawable drawable) {
                drawable.getContext().makeCurrent();
                visualizer.setup( drawable.getGL().getGL2(), width, height );
            }

            public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
                //Called at least once after init(...) and before display(...)
            }

            public void dispose(GLAutoDrawable drawable) {
                //Dispose code here
            }

			@Override
			public void display(GLAutoDrawable drawable) {
			    visualizer.checkAndDisplay( drawable.getGL().getGL2(), drawable.getSurfaceWidth(), drawable.getSurfaceHeight() );
	            
			}
        });
        drawable.display();

        //Move drawing code to OffscreenJOGL

        BufferedImage im = new AWTGLReadBufferUtil(drawable.getGLProfile(), false).readPixelsToBufferedImage(drawable.getGL(), 0, 0, width, height, true /* awtOrientation */);
        try {
			ImageIO.write(im,"png",new File("C:\\users\\test\\desktop\\yo.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
    }
}