package jp.takesin.wallpapermaker;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DragViewListener implements OnTouchListener {
	
	private float downX;
	private float downY;
	private int downLeftMargin;
	private int downTopMargin;
	
	@Override
    public boolean onTouch(View v, MotionEvent event) {

		// ViewGroup.MarginLayoutParamsでキャストすることで
        // FrameLayoutの子要素であっても同様に扱える。
        final ViewGroup.MarginLayoutParams param = 
            (ViewGroup.MarginLayoutParams)v.getLayoutParams();

        if( event.getAction() == MotionEvent.ACTION_DOWN ){

            downX = event.getRawX();
            downY = event.getRawY();

            downLeftMargin = param.leftMargin;
            downTopMargin = param.topMargin;

            return true;
        }
        else if( event.getAction() == MotionEvent.ACTION_MOVE){

            param.leftMargin = downLeftMargin + (int)(event.getRawX() - downX);
            param.topMargin = downTopMargin + (int)(event.getRawY() - downY);

            v.layout(
           param.leftMargin, 
           param.topMargin, 
           param.leftMargin + v.getWidth(), 
           param.topMargin + v.getHeight());

            return true;
        }

        return false;
    }
	
	
	
}