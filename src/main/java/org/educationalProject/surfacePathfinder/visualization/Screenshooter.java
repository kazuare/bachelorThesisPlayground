package org.educationalProject.surfacePathfinder.visualization;

import com.jogamp.opengl.GLProfile;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
                visualizer.checkAndDisplay( drawable.getGL().getGL2(), drawable.getSurfaceWidth(), drawable.getSurfaceHeight() );
            }

            public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
                //Called at least once after init(...) and before display(...)
            }

            public void dispose(GLAutoDrawable drawable) {
                //Dispose code here
            }

			@Override
			public void display(GLAutoDrawable drawable) {
			    
	            
			}
        });
        
        System.out.println("drawable.display()");
        drawable.display();

        //Move drawing code to OffscreenJOGL
        System.out.println("Move drawing code to OffscreenJOGL");
        BufferedImage im = new AWTGLReadBufferUtil(drawable.getGLProfile(), false).readPixelsToBufferedImage(drawable.getGL(), 0, 0, width, height, true /* awtOrientation */);
        try {
            System.out.println("Writing");
            OutputStream stream = new BufferedOutputStream(new FileOutputStream("C:\\users\\test\\desktop\\"+visualizer.name+".png"), 100000);
			ImageIO.write(im,"png", stream);
			stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
    }
}