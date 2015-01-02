package jp.takesin.wallpapermaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import jp.takesin.wallpapermaker.colorpicker.ColorPickerDialog;
import jp.takesin.wallpapermaker.colorpicker.ColorPickerDialog.OnColorChangedListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();
	
	
	private ArrayList<ItemData> mItemList = new ArrayList<ItemData>();
	private int mBackgroundColor = Color.BLACK;
	private int mTextColor = Color.WHITE;
	private float mAddTextSize;
	private float mEditTextSize;
	
	
	
	// assets以下のフォントファイル
	private String[] mFontPaths = {
			null,
            "fonts/ipaexg.ttf",
            "fonts/ipaexm.ttf",
            "fonts/hannari.ttf",
            "fonts/bokutachi.ttf",
            "fonts/kokoro.ttf"};
	
	// TextViewのフォントを変更する際のViewId
	private int[] mFontTextViewIds = {
			R.id.textFont1,
			R.id.textFont2,
			R.id.textFont3,
			R.id.textFont4,
			R.id.textFont5,
			R.id.textFont6};
	
	// 追加するTextViewのフォントサイズ
	private String[] mTextViewSizeArray = {"8", "16", "24", "32", "40", "60", "120"}; 
	
	private RelativeLayout mRootView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		mRootView = (RelativeLayout)findViewById(R.id.RelativeLayoutEditPre);
		(findViewById(R.id.ImgAdd)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddTextDialog();
			}
		});
	}

	/**
	 * TextViewを追加する際に表示するダイアログ
	 */
	private void showAddTextDialog() {

		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.dialog_add_text, null,
				false);

		// フォントサイズを指定するSpinnerの設定
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, mTextViewSizeArray);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		final Spinner spinner = (Spinner) layout.findViewById(R.id.spnTextSize);
		spinner.setAdapter(adapter);
		spinner.setSelection(3);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mAddTextSize = Float.valueOf(mTextViewSizeArray[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		// 文字列を入力するEditText
		final EditText editView = (EditText) layout
				.findViewById(R.id.edtAddText);

		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle("追加する文字を入力")
				.setView(layout)
				.setPositiveButton("追加", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						final String txt = editView.getText().toString();
						if(TextUtils.isEmpty(txt)){
							return;
						}
						addTextView(txt);
					}
				})
				.setNegativeButton("キャンセル",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}
				).show();
	}
	
	/**
	 * RootViewにTextViewを追加する
	 * @param 追加するする文字列　text
	 */
	private void addTextView(String text) {
		final TextView textView = new TextView(getApplicationContext());
		textView.setTextSize(mAddTextSize);
		textView.setTextColor(mTextColor);
		textView.setText(text);
		textView.setBackgroundResource(R.drawable.item_text_selector);
		// ドラックして移動するためのリスナーを追加
		textView.setOnTouchListener(new DragViewListener());
		// クリックで文字サイズ、文字列変更処理
		textView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showEditTextDialog((TextView)v);
			}
		});
		
		final Point point = new Point();
		point.x = 50;
		point.y = 50;
		textView.setPivotX(point.x);
		textView.setPivotX(point.y);
		
		final RelativeLayout.LayoutParams param =  new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		param.setMargins(point.x, point.y, point.x + textView.getWidth(), point.y
				+ textView.getHeight());
		mRootView.addView(textView, param);
		
		final ItemData item = new ItemData();
		item.textView = textView;
		item.size = mAddTextSize;
		mItemList.add(item);
		textView.setTag(item);
	}
	
	
	/**
	 * TextViewを追加する際に表示するダイアログ
	 */
	private void showEditTextDialog(final TextView textView) {

		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.dialog_add_text, null,
				false);

		// フォントサイズを指定するSpinnerの設定
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, mTextViewSizeArray);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		final Spinner spinner = (Spinner) layout.findViewById(R.id.spnTextSize);
		spinner.setAdapter(adapter);
		
		
		final ItemData item = (ItemData)textView.getTag();
		final int size =(int)item.size;
		final int index = Arrays.asList(mTextViewSizeArray).indexOf(String.valueOf(size));
		spinner.setSelection(index);
		
		
		
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mEditTextSize = Integer.valueOf(mTextViewSizeArray[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		// 文字列を入力するEditText
		final EditText editView = (EditText) layout
				.findViewById(R.id.edtAddText);
		editView.setText(textView.getText());

		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle("文字を編集")
				.setView(layout)
				.setPositiveButton("完了", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						final String txt = editView.getText().toString();
						if(TextUtils.isEmpty(txt)){
							mRootView.removeView(textView);
							mItemList.remove(textView.getTag());
							return;
						}
						textView.setText(txt);
						textView.setTextSize(mEditTextSize);
						
						final ItemData item = (ItemData)textView.getTag();
						item.textView = textView;
						item.size = mEditTextSize;
					}
				})
				.setNegativeButton("キャンセル",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}
				).setNeutralButton("削除",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								mRootView.removeView(textView);
								mItemList.remove(textView.getTag());
							}
						})
				.show();
	}
	

	// オプションメニューが最初に呼び出される時に1度だけ呼び出されます
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	// オプションメニューアイテムが選択された時に呼び出されます
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;
		
		final View addBtn = findViewById(R.id.ImgAdd);
		addBtn.setVisibility(View.GONE);
		for (ItemData itemData : mItemList) {
			itemData.textView.setBackgroundColor(Color.TRANSPARENT);
		}
		
		switch (item.getItemId()) {
		default:
			ret = super.onOptionsItemSelected(item);
			break;
		case R.id.action_save:
			final boolean saveFlag = BitmapUtil.saveBitmapToSd(BitmapUtil.getBitmap(mRootView));
			String txt = "保存しました。";
			if (!saveFlag) {
				txt = "保存に失敗しました。";
			}
			Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT)
					.show();

			ret = true;
			break;
		case R.id.action_setup:
			final WallpaperManager wallman = WallpaperManager.getInstance(this);
			try {
				wallman.setBitmap(BitmapUtil.getBitmap(mRootView));
				Toast.makeText(getApplicationContext(), "壁紙に設定しました。",
						Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				//e.printStackTrace();
				Toast.makeText(getApplicationContext(), "設定に失敗しました。",
						Toast.LENGTH_SHORT).show();
			}

			ret = true;
			break;
			
		case R.id.action_text_color:
			
			// ダイアログ生成(Context, デフォルトの色)
			final ColorPickerDialog txtDialog = new ColorPickerDialog(this, mTextColor);
			// HEXの表示（非表示の場合はfalse）
			txtDialog.setHexValueEnabled(false);
			// アルファ値の表示（非表示の場合はfalse）
			txtDialog.setAlphaSliderVisible(false);

			// OKボタンが押下された時のハンドラ
			txtDialog.setOnColorChangedListener(new OnColorChangedListener() {
				@Override
				public void onColorChanged(int newcolor) {
					mTextColor = newcolor;
					for (ItemData itemData : mItemList) {
						itemData.textView.setTextColor(newcolor);
					}
				}
			});

			// ダイアログ表示
			txtDialog.show();
			
			ret = true;
			break;
		case R.id.action_background_color:
						
			// ダイアログ生成(Context, デフォルトの色)
			final ColorPickerDialog bgDialog = new ColorPickerDialog(this, mBackgroundColor);

			// HEXの表示（非表示の場合はfalse）
			bgDialog.setHexValueEnabled(false);

			// アルファ値の表示（非表示の場合はfalse）
			bgDialog.setAlphaSliderVisible(false);

			// OKボタンが押下された時のハンドラ
			bgDialog.setOnColorChangedListener(new OnColorChangedListener() {
			    @Override
			    public void onColorChanged(int newcolor) {
			    	mBackgroundColor = newcolor;
			    	mRootView.setBackgroundColor(newcolor);
			    }
			});

			// ダイアログ表示
			bgDialog.show();

			ret = true;
			break;
			
		case R.id.action_fonts:
			final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.dialog_fonts, null,
					false);
			
			final AlertDialog fontSelectDialog = new AlertDialog.Builder(this)
		    .setTitle("フォントを選択")
		    .setView(layout)
			.show();
			
			for(int i = 0; i < mFontTextViewIds.length; i++){
				final Typeface typeface;
				if(TextUtils.isEmpty(mFontPaths[i])){
					typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
				} else {
					typeface = Typeface.createFromAsset(getAssets(), mFontPaths[i]);
				}
				
				final TextView textView = (TextView)layout.findViewById(mFontTextViewIds[i]);
				textView.setTypeface(typeface);
				textView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						setTextFornt(typeface);
						fontSelectDialog.dismiss();
					}
				});
			}
			
			ret = true;
			break;
		}
		
		addBtn.setVisibility(View.VISIBLE);
		for (ItemData itemData : mItemList) {
			itemData.textView.setBackgroundResource(R.drawable.item_text_selector);
		}
		return ret;
	}
	
	/**
	 * 画面に追加しているTextViewのフォントを全て変更する
	 * @param typeface
	 */
	private void setTextFornt(Typeface typeface){
		for (ItemData itemData : mItemList) {
			itemData.textView.setTypeface(typeface);
		}
	}
}
