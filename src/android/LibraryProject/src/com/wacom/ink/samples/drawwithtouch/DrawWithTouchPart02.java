package com.wacom.ink.samples.drawwithtouch;

import java.nio.FloatBuffer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.wacom.ink.path.PathBuilder.PropertyFunction;
import com.wacom.ink.path.PathBuilder.PropertyName;
import com.wacom.ink.path.SpeedPathBuilder;
import com.wacom.ink.rasterization.BlendMode;
import com.wacom.ink.rasterization.InkCanvas;
import com.wacom.ink.rasterization.Layer;
import com.wacom.ink.rasterization.SolidColorBrush;
import com.wacom.ink.rasterization.StrokeJoin;
import com.wacom.ink.rasterization.StrokePaint;
import com.wacom.ink.rendering.RenderingContext;
import com.wacom.ink.utils.TouchUtils;

public class DrawWithTouchPart02 extends Activity {
	private RenderingContext renderingContext;
	private InkCanvas inkCanvas;
	private Layer viewLayer;
	private SpeedPathBuilder pathBuilder;
	private StrokePaint paint;
	private SolidColorBrush brush;
	private StrokeJoin strokeJoin;
	private Layer strokesLayer;
	private int pathStride;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_draw_with_touch);

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		surfaceView.getHolder().addCallback(new SurfaceHolder.Callback(){

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				renderingContext = new RenderingContext(new RenderingContext.EGLConfigSpecification());
				renderingContext.initContext();

				renderingContext.createEGLSurface(holder);
				renderingContext.bindEGLContext();

				inkCanvas = new InkCanvas();
				inkCanvas.setDimensions(width, height);
				inkCanvas.glInit();

				viewLayer = new Layer();
				// Initialize the view layer with the dimensions of the surface, with scale factor 1.0 and 
				// bind it to the default framebuffer, the OpenGL context was created with. 
				viewLayer.initWithFramebuffer(inkCanvas, width, height, 1.0f, 0);
				viewLayer.setFlipY(true);

				strokesLayer = new Layer();
				strokesLayer.init(inkCanvas, width, height, 1.0f, true);

				pathBuilder = new SpeedPathBuilder(getResources().getDisplayMetrics().density);
				pathBuilder.setNormalizationConfig(100.0f, 4000.0f);
				pathBuilder.setMovementThreshold(2.0f);
				pathBuilder.setPropertyConfig(PropertyName.Width, 5f, 10f, 5f, 10f, PropertyFunction.Power, 1.0f, false);
				pathStride = pathBuilder.getStride();
				
				brush = new SolidColorBrush();
				brush.setGradientAntialiazingEnabled(true);
				brush.setBlendMode(BlendMode.BLENDMODE_NORMAL);

				paint = new StrokePaint();
				paint.setStrokeBrush(brush);	// Solid color brush.
				paint.setColor(Color.BLUE);		// Blue color.
				paint.setWidth(Float.NaN);		// Expected variable width.

				strokeJoin = new StrokeJoin();

				renderView();
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {

			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {

			}
		});

		surfaceView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				buildPath(event);
				drawStroke(event);
				renderView();
				return true;
			}
		});

	}

	private void renderView() {
		inkCanvas.setTarget(viewLayer);
		// Clear the view and fill it with white color.
		inkCanvas.clearColor(Color.WHITE);
		inkCanvas.drawLayer(strokesLayer, BlendMode.BLENDMODE_NORMAL);	
		renderingContext.swap();
	}

	private void buildPath(MotionEvent event){
		float x = event.getX();
		float y = event.getY();
		double timestamp = TouchUtils.getTimestamp(event);

		FloatBuffer part = null;
		int partSize;

		switch (event.getAction()){
		case MotionEvent.ACTION_DOWN:
			part = pathBuilder.beginPath(x, y, timestamp);
			break;
		case MotionEvent.ACTION_MOVE:
			part = pathBuilder.addPoint(x, y, timestamp);
			break;
		case MotionEvent.ACTION_UP:
			part = pathBuilder.endPath(x, y, timestamp);
			break;
		}

		if (part!=null){
			partSize = pathBuilder.getPathPartSize();
			pathBuilder.addPathPart(part, partSize);
		}
	}

	private void drawStroke(MotionEvent event){
		switch (event.getAction()){
		case MotionEvent.ACTION_DOWN:
			strokeJoin.reset();
			break;
		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_UP:	
			if (pathBuilder.getPathSize()>0){
				// Set round caps.
				if (pathBuilder.hasFinished()){
					// Configure round caps for stroke beginning.
					paint.setRoundCaps(false, true);
				} else {
					if (pathBuilder.getPathSize()==pathBuilder.getAddedPointsSize()){
						paint.setRoundCaps(true, false);
					} else {
						// Configure round caps for stroke ending.
						paint.setRoundCaps(false, false);
					}
				}
				// Draw part of a path.
				if (pathBuilder.getAddedPointsSize() > 0) {
					inkCanvas.setTarget(strokesLayer);
					inkCanvas.drawStroke(paint, strokeJoin, pathBuilder.getPathBuffer(), pathBuilder.getPathLastUpdatePosition(), pathBuilder.getAddedPointsSize(), pathStride, 0.0f, 1.0f);
				}
			}
			break;
		}
	}
}
