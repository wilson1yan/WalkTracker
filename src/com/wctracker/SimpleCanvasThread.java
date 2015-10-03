package com.wctracker;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class SimpleCanvasThread extends Thread {
	private SurfaceHolder _surfaceHolder;
	private SimpleCanvas _canvas;
	private boolean _run = false;

	public SimpleCanvasThread(SurfaceHolder surfaceHolder, SimpleCanvas walkCanvas) {
		_surfaceHolder = surfaceHolder;
		_canvas = walkCanvas;
	}

	public void setRunning(boolean run) {
		_run = run;
	}

	@Override
	public void run() {
		Canvas c;

		while (_run) {
			c = null;
			try {
				c = _surfaceHolder.lockCanvas();
				synchronized (_surfaceHolder) {
					_canvas.draw(c);
				}
			} finally {
				if (c != null) {
					_surfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
	}
}
