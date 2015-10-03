package com.wctracker;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class CanvasThread extends Thread{
	private SurfaceHolder _surfaceHolder;
	private WalkCanvas _canvas;
	private boolean _run = false;
	
	public CanvasThread(SurfaceHolder surfaceHolder, WalkCanvas walkCanvas){
		_surfaceHolder = surfaceHolder;
		_canvas = walkCanvas;
	}
	
	public void setRunning(boolean run){
		_run = run;
	}
	
	@Override
	public void run(){
		Canvas c;
		
		while(_run){
			c = null;
			try{
				c = _surfaceHolder.lockCanvas();
				synchronized (_surfaceHolder) {
					_canvas.draw(c);
				}
			}finally{
				if(c!=null){
					_surfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
	}
}
