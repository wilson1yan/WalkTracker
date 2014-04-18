package com.tracker;

import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;

public class CanvasThreadV2 extends Thread{
	private SurfaceHolder _surfaceHolder;
	private WalkCanvasV2 _canvas;
	private boolean _run = false;
	public Handler mHandler;
	
	public CanvasThreadV2(SurfaceHolder surfaceHolder, WalkCanvasV2 walkCanvas){
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
