package com.tracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class WalkCanvas extends SurfaceView implements SurfaceHolder.Callback{
	CanvasThread canvasThread;
	Paint paint = new Paint();

	long start = System.currentTimeMillis();

	GradientDrawable grad, gradVert;

	public WalkCanvas(Context context, AttributeSet attrs){
		super(context, attrs);
		getHolder().addCallback(this);
		setFocusable(true);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){

	}

	public void surfaceCreated(SurfaceHolder holder){
		canvasThread = new CanvasThread(getHolder(),  this);
		canvasThread.setRunning(true);
		canvasThread.start();
	}


	public void surfaceDestroyed(SurfaceHolder arg0) {
		boolean retry = true;
		canvasThread.setRunning(false);
		while(retry){
			try{
				canvasThread.join();
				retry = false;
			}catch(InterruptedException e){

			}
		}
	}


	@Override
	public void draw(Canvas canvas){

		try{

			paint.setColor(Color.WHITE);
			paint.setStrokeWidth(5);
			canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

			initGrad(canvas);

			grad.draw(canvas);
			gradVert.draw(canvas);

			drawBorders(canvas, paint);

			int textSize = (canvas.getHeight()/2)-(canvas.getHeight()/4);
			int x = canvas.getWidth()/10;
			int y = (canvas.getHeight()/2)-(canvas.getHeight()/10);

			paint.setTextSize(textSize);
			paint.setColor(Color.BLUE);

			canvas.drawText((int)WalkMapV2.convertedDistance + WalkMapV2.measurement, x, y, paint);

			y = canvas.getWidth()/8;

			String text = getTime((int)((System.currentTimeMillis()-WalkMapV2.startTime)/1000));


			x = canvas.getWidth()*11/20;

			canvas.drawText(text, x, y, paint);

			text = (int)WalkMapV2.calories + " calories";
			canvas.drawText(text, canvas.getWidth()/4, (canvas.getHeight())-(canvas.getHeight()/10), paint);
		}catch(NullPointerException e){

		}
	}

	private void initGrad(Canvas canvas){
		if(grad==null || gradVert==null){
			grad = new GradientDrawable(Orientation.LEFT_RIGHT,
			        new int[]{0xff777777, 0x22ffffff,0xff777777, 0x22ffffff, 0xff777777});
			grad.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());

			gradVert = new GradientDrawable(Orientation.TOP_BOTTOM,
					new int[]{0xff777777, 0x22ffffff, 0xff777777, 0x22ffffff, 0xff777777});
			gradVert.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		}
	}

	private void drawBorders(Canvas canvas, Paint paint){
		paint.setColor(Color.BLACK);
		canvas.drawLine(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2, paint);
		canvas.drawLine(canvas.getWidth()/2, 0, canvas.getWidth()/2, canvas.getHeight()/2, paint);
		canvas.drawLine(2, 2, 2, canvas.getHeight()-2, paint);
		canvas.drawLine(2, 2, canvas.getWidth()-2, 2, paint);
		canvas.drawLine(canvas.getWidth()-2, 2, canvas.getWidth()-2, canvas.getHeight()-2, paint);
		canvas.drawLine(2, canvas.getHeight()-2, canvas.getWidth()-2, canvas.getHeight()-2, paint);

	}

	private String getTime(int time){
		String text = "";
		if(WalkMapV2.isRunning){
			int remainder;
			int hours, minutes, seconds;

			if(time>=3600){
				hours = (int)(time/3600.0);
				minutes = time%3600;

				text += hours + ":";

				remainder = minutes%60;
				minutes = (int)(minutes/60.0);

				text += minutes + ":" + remainder;
			}else if(time>=60){
				seconds = time%60;
				minutes = (int)(time/60.0);

				text += minutes + ":" + seconds;
			}else{
				text += time;
			}
		}else{
			text = "0";
		}

		return text;
	}
}
