/*
 * Copyright (C) 2010 Daniel Nilsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.takesin.wallpapermaker.colorpicker;

import java.util.Locale;

import jp.takesin.wallpapermaker.R;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ColorPickerDialog 
	extends 
		Dialog 
	implements
		ColorPickerView.OnColorChangedListener,
		View.OnClickListener {

	private ColorPickerView mColorPicker;

	private ColorPickerPanelView mOldColor;
	private ColorPickerPanelView mNewColor;

	private EditText mHexVal;
	private boolean mHexValueEnabled = false;
	private ColorStateList mHexDefaultTextColor;

	private OnColorChangedListener mListener;

	private TextView colorRGB;
	private TextView colorHSV;
	private TextView colorYUV;
	private float mHSV[];
	private int[] YUVcolorCode;

	private Button OKbtn;
	private Button NGbtn;

	public interface OnColorChangedListener {
		public void onColorChanged(int color);
	}
	
	public ColorPickerDialog(Context context, int initialColor) {
		super(context);

		init(initialColor);
	}

	private void init(int color) {
		// To fight color banding.
		getWindow().setFormat(PixelFormat.RGBA_8888);

		setUp(color);

	}

	private void setUp(int color) {

		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View layout = inflater.inflate(R.layout.dialog_color_picker, null);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(layout);

		mHSV = new float[3];
		colorRGB = (TextView) findViewById(R.id.colorRGB);
		colorHSV = (TextView) findViewById(R.id.colorHSV);
		colorYUV = (TextView) findViewById(R.id.colorYUV);

		OKbtn = (Button) findViewById(R.id.OKbtn);
		NGbtn = (Button) findViewById(R.id.NGbtn);

		mColorPicker = (ColorPickerView) layout.findViewById(R.id.color_picker_view);
		mOldColor = (ColorPickerPanelView) layout.findViewById(R.id.old_color_panel);
		mNewColor = (ColorPickerPanelView) layout.findViewById(R.id.new_color_panel);

		mHexVal = (EditText) layout.findViewById(R.id.hex_val);
		mHexVal.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		mHexDefaultTextColor = mHexVal.getTextColors();
		
		mHexVal.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					String s = mHexVal.getText().toString();
					if (s.length() > 5 || s.length() < 10) {
						try {
							int c = convertToColorInt(s.toString());
							mColorPicker.setColor(c, true);
							mHexVal.setTextColor(mHexDefaultTextColor);
						} catch (IllegalArgumentException e) {
							mHexVal.setTextColor(Color.RED);
						}
					} else {
						mHexVal.setTextColor(Color.RED);
					}
					return true;
				}
				return false;
			}
		});
		
		((LinearLayout) mOldColor.getParent()).setPadding(
			Math.round(mColorPicker.getDrawingOffset()), 
			0, 
			Math.round(mColorPicker.getDrawingOffset()), 
			0
		);	

		mOldColor.setOnClickListener(this);
		mNewColor.setOnClickListener(this);
		OKbtn.setOnClickListener(this);
		NGbtn.setOnClickListener(this);

		mColorPicker.setOnColorChangedListener(this);
		mOldColor.setColor(color);
		mColorPicker.setColor(color, true);

		mHSV = new float[3];
		Color.colorToHSV(color, mHSV);
		colorRGB.setText(String.format("RGB: R=%03d,G=%03d,B=%03d",Color.red(color), Color.green(color),Color.blue(color)));
		colorHSV.setText(String.format("HSV: H=%03d,S=%03d,V=%03d",(int) mHSV[0], (int) (mHSV[1] * 100.0f), (int) (mHSV[2] * 100.0f)));
		YUVcolorCode = convertRGB2YUV(color);
		colorYUV.setText(String.format("YUV: Y=%03d,U=%03d,V=%03d",YUVcolorCode[0], YUVcolorCode[1], YUVcolorCode[2]));
	}

	@Override
	public void onColorChanged(int color) {

		mNewColor.setColor(color);
		
		if (mHexValueEnabled) updateHexValue(color);

		mHSV = new float[3];
		Color.colorToHSV(color, mHSV);
		colorRGB.setText(String.format("RGB: R=%03d,G=%03d,B=%03d",Color.red(color), Color.green(color),Color.blue(color)));
		colorHSV.setText(String.format("HSV: H=%03d,S=%03d,V=%03d",(int) mHSV[0], (int) (mHSV[1] * 100.0f), (int) (mHSV[2] * 100.0f)));
		YUVcolorCode = convertRGB2YUV(color);
		colorYUV.setText(String.format("YUV: Y=%03d,U=%03d,V=%03d",YUVcolorCode[0], YUVcolorCode[1], YUVcolorCode[2]));
	}

	public void setHexValueEnabled(boolean enable) {
		mHexValueEnabled = enable;
		if (enable) {
			mHexVal.setVisibility(View.VISIBLE);
			updateHexLengthFilter();
			updateHexValue(getColor());
		}
		else
			mHexVal.setVisibility(View.GONE);
	}
	
	public boolean getHexValueEnabled() {
		return mHexValueEnabled;
	}
	
	private void updateHexLengthFilter() {
		if (getAlphaSliderVisible())
			mHexVal.setFilters(new InputFilter[] {new InputFilter.LengthFilter(9)});
		else
			mHexVal.setFilters(new InputFilter[] {new InputFilter.LengthFilter(7)});
	}

	private void updateHexValue(int color) {
		if (getAlphaSliderVisible()) {
			mHexVal.setText(convertToARGB(color).toUpperCase(Locale.getDefault()));
		} else {
			mHexVal.setText(convertToRGB(color).toUpperCase(Locale.getDefault()));
		}
		mHexVal.setTextColor(mHexDefaultTextColor);
	}

	public void setAlphaSliderVisible(boolean visible) {
		mColorPicker.setAlphaSliderVisible(visible);
		if (mHexValueEnabled) {
			updateHexLengthFilter();
			updateHexValue(getColor());
		}
	}
	
	public boolean getAlphaSliderVisible() {
		return mColorPicker.getAlphaSliderVisible();
	}

	/**
	 * Set a OnColorChangedListener to get notified when the color
	 * selected by the user has changed.
	 * @param listener
	 */
	public void setOnColorChangedListener(OnColorChangedListener listener){
		mListener = listener;
	}

	public int getColor() {
		return mColorPicker.getColor();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.new_color_panel || v.getId() == R.id.OKbtn) {
			if (mListener != null) {
				mListener.onColorChanged(mNewColor.getColor());
			}
		}
		dismiss();
	}

	private static int[] convertRGB2YUV(int color) {
		ColorMatrix cm = new ColorMatrix();
		cm.setRGB2YUV();
		final float[] yuvArray = cm.getArray();

		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);
		int[] result = new int[3];
		result[0] = floatToByte(yuvArray[0] * r + yuvArray[1] * g + yuvArray[2]
				* b);
		result[1] = floatToByte(yuvArray[5] * r + yuvArray[6] * g + yuvArray[7]
				* b) + 127;
		result[2] = floatToByte(yuvArray[10] * r + yuvArray[11] * g
				+ yuvArray[12] * b) + 127;
		return result;
	}

	private static int floatToByte(float x) {
		int n = java.lang.Math.round(x);
		return n;
	}

    public static String convertToARGB(int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }

    public static String convertToRGB(int color) {
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + red + green + blue;
    }

    public static int convertToColorInt(String argb) throws NumberFormatException {

    	if (argb.startsWith("#")) {
    		argb = argb.replace("#", "");
    	}

        int alpha = -1, red = -1, green = -1, blue = -1;

        if (argb.length() == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16);
            red = Integer.parseInt(argb.substring(2, 4), 16);
            green = Integer.parseInt(argb.substring(4, 6), 16);
            blue = Integer.parseInt(argb.substring(6, 8), 16);
        }
        else if (argb.length() == 6) {
            alpha = 255;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        }

        return Color.argb(alpha, red, green, blue);
    }

    @Override
	public Bundle onSaveInstanceState() {
		Bundle state = super.onSaveInstanceState();
		state.putInt("old_color", mOldColor.getColor());
		state.putInt("new_color", mNewColor.getColor());
		return state;
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mOldColor.setColor(savedInstanceState.getInt("old_color"));
		mColorPicker.setColor(savedInstanceState.getInt("new_color"), true);
	}
}
