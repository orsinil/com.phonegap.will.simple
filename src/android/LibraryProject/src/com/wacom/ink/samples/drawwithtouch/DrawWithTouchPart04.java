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

import com.wacom.ink.geometry.WRect;
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
import com.wacom.ink.smooth.MultiChannelSmoothener;
import com.wacom.ink.smooth.MultiChannelSmoothener.SmoothingResult;
import com.wacom.ink.utils.TouchUtils;

public class DrawWithTouchPart04 extends Activity {
	private RenderingContext renderingContext;
	private InkCanvas inkCanvas;
	private Layer viewLayer;
	private SpeedPathBuilder pathBuilder;
	private StrokePaint paint;
	private StrokePaint prelimPaint;
	private SolidColorBrush brush;
	private StrokeJoin strokeJoin;
	private StrokeJoin prelimJoin;
	private Layer strokesLayer;
	private Layer strokesWithPreliminaryLayer;
	private MultiChannelSmoothener smoothener;
	private int pathStride;
	private WRect prevPrelimArea = new WRect();
	private WRect dirtyArea = new WRect();

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

				strokesWithPreliminaryLayer = new Layer();
				strokesWithPreliminaryLayer.init(inkCanvas, width, height, 1.0f, true);

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

				prelimPaint = new StrokePaint();

				strokeJoin = new StrokeJoin();
				prelimJoin = new StrokeJoin();

				smoothener = new MultiChannelSmoothener(pathStride);
				smoothener.enableChannel(2);
				
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
		// Clear the view and fills it with white color.
		inkCanvas.clearColor(Color.WHITE);
		inkCanvas.drawLayer(strokesWithPreliminaryLayer, BlendMode.BLENDMODE_NORMAL);	
		renderingContext.swap();
	}

	private void buildPath(MotionEvent event){
		float x = event.getX();
		float y = event.getY();
		double timestamp = TouchUtils.getTimestamp(event);

		FloatBuffer part = null;
		int partSize;
		boolean bFinishSmoothing = false;
		
		// Add the current input point to the path builder
		switch (event.getAction()){
			case MotionEvent.ACTION_DOWN:
				part = pathBuilder.beginPath(x, y, timestamp);
				smoothener.reset();
				break;
			case MotionEvent.ACTION_MOVE:
				part = pathBuilder.addPoint(x, y, timestamp);
				break;
			case MotionEvent.ACTION_UP:
				bFinishSmoothing = true;
				part = pathBuilder.endPath(x, y, timestamp);
				break;
		}
		
		SmoothingResult smoothingResult;
		
		if (part!=null){
			partSize = pathBuilder.getPathPartSize();
			// Smooth the returned control points (aka path part).
			smoothingResult = smoothener.smooth(part, partSize, bFinishSmoothing);
			// Add the smoothed control points to the path builder.
			pathBuilder.addPathPart(smoothingResult.getSmoothedPoints(), smoothingResult.getSize());
		}
		
		// Create a preliminary path.
		FloatBuffer preliminaryPath = pathBuilder.createPreliminaryPath();
		// Smoothen the preliminary path's control points (return inform of a path part).
		smoothingResult = smoothener.smooth(preliminaryPath, pathBuilder.getPreliminaryPathSize(), true);
		// Add the smoothed preliminary path to the path builder.
		pathBuilder.finishPreliminaryPath(smoothingResult.getSmoothedPoints(), smoothingResult.getSize());
	}

	private void drawStroke(MotionEvent event){
		switch (event.getAction()){
			case MotionEvent.ACTION_DOWN:
				strokeJoin.reset();
				
				// Reset areas, needed for correct drawing.
				prevPrelimArea.setNaN();
				dirtyArea.setNaN();
				
				// Use the same paint for the preliminary path.
				prelimPaint.copy(paint);
				prelimPaint.setRoundCaps(false, true);
				
				// Copy the strokesLayer content into the strokesWithPreliminaryLayer.
				inkCanvas.setTarget(strokesWithPreliminaryLayer);
				inkCanvas.drawLayer(strokesLayer, null, BlendMode.BLENDMODE_NONE);
				
			case MotionEvent.ACTION_MOVE:
				
			case MotionEvent.ACTION_UP:	
				if (pathBuilder.getPathSize()>0){
					if (pathBuilder.hasFinished()){
						paint.setRoundCaps(false, true);
					} else {
						if (pathBuilder.getPathSize()==pathBuilder.getAddedPointsSize()){
							paint.setRoundCaps(true, false);
						} else {
							paint.setRoundCaps(false, false);
						}
					}
	
					// Draw part of a path.
					if (pathBuilder.getAddedPointsSize() > 0) {
						inkCanvas.setTarget(strokesLayer);
						inkCanvas.drawStroke(paint, strokeJoin, pathBuilder.getPathBuffer(), pathBuilder.getPathLastUpdatePosition(), pathBuilder.getAddedPointsSize(), pathStride, 0.0f, 1.0f);
						
						// Set the dirty area of the current update and unite it with 
						// the previous preliminary path dirty area (if any).
						dirtyArea.set(strokeJoin.getDirtyArea());
						dirtyArea.union(prevPrelimArea);
					}

					inkCanvas.setTarget(strokesWithPreliminaryLayer);
					
					// Update only the dirty area.
					inkCanvas.setClipRect(dirtyArea);
					inkCanvas.drawLayer(strokesLayer, null, BlendMode.BLENDMODE_NONE);
					inkCanvas.disableClipRect();
					
					prelimJoin.copy(strokeJoin);
					
					// Draw the preliminary path.
					inkCanvas.drawStroke(prelimPaint, prelimJoin, pathBuilder.getPreliminaryPathBuffer(), 0, pathBuilder.getFinishedPreliminaryPathSize(), pathBuilder.getStride(), 0.0f, 1.0f);
					
					// Save the preliminary path's dirty area (if any).
					prevPrelimArea.set(prelimJoin.getDirtyArea());
				}
				break;
		}
	}
}
