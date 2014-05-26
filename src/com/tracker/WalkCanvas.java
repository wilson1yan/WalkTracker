package com.tracker;


import java.text.DecimalFormat;

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
	GradientDrawable grad, gradVert;
	DecimalFormat format;
	
	double distance, calories;
	long startTime;
	
	public WalkCanvas(Context context, AttributeSet attrs){
		super(context, attrs);
		getHolder().addCallback(this);
		setFocusable(true);
		
		format = new DecimalFormat("#.##");
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){

	}

	public void surfaceCreated(SurfaceHolder holder){
		canvasThread = new CanvasThread(getHolder(), this);
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

	private void drawBackgroundLayout(Canvas canvas){
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(5);
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

		initGrad(canvas);

		grad.draw(canvas);
		gradVert.draw(canvas);

		drawBorders(canvas, paint);
	}
	
	private void drawDistanceText(Canvas canvas){
		int x = canvas.getWidth()/10;
		int y = (canvas.getHeight()/2)-(canvas.getHeight()/10);

		String text = getCorrectDecimalPlace(Calculator.convertedDistance, Calculator.measurementUnit) + Calculator.measurementUnit;
		canvas.drawText(text, x, y, paint);
	}
	
	private void drawCalorieText(Canvas canvas){
		int x = canvas.getWidth()/4;
		int y = (canvas.getHeight())-(canvas.getHeight()/10);
		
		String text = (int)Calculator.totalCalories + " calories";
		canvas.drawText(text, x, y, paint);
	}
	
	private void drawTimeText(Canvas canvas){
		int x = canvas.getWidth()*11/20;
		int y = canvas.getWidth()/8;

		int seconds = (int)((System.currentTimeMillis()-Calculator.startTime)/1000);
		String text = getTime(seconds);
		
		canvas.drawText(text, x, y, paint);
	}
	
	private void initPaintbrush(int textSize, int color){
		paint.setTextSize(textSize);
		paint.setColor(color);
	}
	
	@Override
	public void draw(Canvas canvas){

		try{
			drawBackgroundLayout(canvas);

			int textSize = (canvas.getHeight()/2)-(canvas.getHeight()/4);
			int color = Color.BLUE;
			initPaintbrush(textSize, color);
			
			
			drawDistanceText(canvas);
			drawTimeText(canvas);
			drawCalorieText(canvas);
	
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
	
	private double getCorrectDecimalPlace(double totalDistance, String measurementUnit){
		if(measurementUnit.equalsIgnoreCase(Settings.MILE) || measurementUnit.equalsIgnoreCase(Settings.KILOMETER)){
			return Double.parseDouble(format.format(totalDistance));
		}else{
			return (int)totalDistance;
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
		if(PathManager.isWalking){
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
