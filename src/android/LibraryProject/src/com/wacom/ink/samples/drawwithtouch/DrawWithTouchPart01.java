package com.wacom.ink.samples.drawwithtouch;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.pinaround.R;
import com.wacom.ink.rasterization.InkCanvas;
import com.wacom.ink.rasterization.Layer;
import com.wacom.ink.rendering.RenderingContext;

public class DrawWithTouchPart01 extends Activity {
	private RenderingContext renderingContext;
	private InkCanvas inkCanvas;
	private Layer viewLayer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_draw_with_touch);

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		surfaceView.getHolder().addCallback(new SurfaceHolder.Callback(){

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				// - Rendering context Setup -

				renderingContext = new RenderingContext(new RenderingContext.EGLConfigSpecification());

				// Initialize the rendering context: choose an EGL configuration and create an EGL context.
				renderingContext.initContext();             

				// Create an egl surface
				renderingContext.createEGLSurface(holder);

				// Bind the EGL context to the current thread
				renderingContext.bindEGLContext();

				// - InkCanvas Setup -
				inkCanvas = new InkCanvas();
				inkCanvas.setDimensions(width, height);
				inkCanvas.glInit();

				// - Layers Setup -

				// Initialize the view layer with the dimensions of the surface, with scale factor 1.0 and 
				// bind it to the default framebuffer, the OpenGL context was created with.
				viewLayer = new Layer();
				viewLayer.initWithFramebuffer(inkCanvas, width, height, 1.0f, 0);
				viewLayer.setFlipY(true);

				renderView();
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {

			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				
			}
		});
	}

	private void renderView() {
		inkCanvas.setTarget(viewLayer);
		// Clear the view and fill it with red color.
		inkCanvas.clearColor(Color.RED);
		renderingContext.swap();
	}
}
