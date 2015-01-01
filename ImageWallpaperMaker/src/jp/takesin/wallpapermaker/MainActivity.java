package jp.takesin.wallpapermaker;

import java.io.IOException;
import java.util.ArrayList;

import jp.takesin.wallpapermaker.colorpicker.ColorPickerDialog;
import jp.takesin.wallpapermaker.colorpicker.ColorPickerDialog.OnColorChangedListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();
	private Button mAddBtn;
	
	private ArrayList<ItemData> mItemList = new ArrayList<ItemData>();
	
	private int mBackgroundColor = Color.BLACK;
	
	private int mTextColor = Color.WHITE;
	private float mTextSize = 20;
	
	private RelativeLayout mRootView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		mRootView = (RelativeLayout)findViewById(R.id.RelativeLayoutEditPre);
		mAddBtn = (Button) findViewById(R.id.BtnAdd);
		mAddBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddTextDialog();
			}
		});
	}

	private void showAddTextDialog() {

		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.dialog_add_text, null,
				false);

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
						}).show();
	}
	
	private void addTextView(String text) {
		final TextView textView = new TextView(getApplicationContext());
		textView.setTextSize(mTextSize);
		textView.setTextColor(mTextColor);
		textView.setText(text);
		textView.setOnTouchListener(new DragViewListener());
		textView.setBackgroundResource(R.drawable.item_text_selector);
		
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
		item.point = point;
		mItemList.add(item);
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
		mAddBtn.setVisibility(View.GONE);
		
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
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "設定に失敗しました。",
						Toast.LENGTH_SHORT).show();
			}

			ret = true;
			break;
		case R.id.action_background_color:
						
			// ダイアログ生成(Context, デフォルトの色)
			final ColorPickerDialog dialog = new ColorPickerDialog(this, mBackgroundColor);

			// HEXの表示（非表示の場合はfalse）
			dialog.setHexValueEnabled(false);

			// アルファ値の表示（非表示の場合はfalse）
			dialog.setAlphaSliderVisible(false);

			// OKボタンが押下された時のハンドラ
			dialog.setOnColorChangedListener(new OnColorChangedListener() {
			    @Override
			    public void onColorChanged(int newcolor) {
			    	mBackgroundColor = newcolor;
			    	mRootView.setBackgroundColor(newcolor);
			    }
			});

			// ダイアログ表示
			dialog.show();
				
//			final int color;
//
//			if (mBackgroundColor == Color.BLACK) {
//				color = Color.WHITE;
//				mTextColor = Color.BLACK;
//				mBackgroundColor = Color.WHITE;
//			} else {
//				color = Color.BLACK;
//				mTextColor = Color.WHITE;
//				mBackgroundColor = Color.BLACK;
//			}
//			mRootView.setBackgroundColor(color);
//			for (ItemData itemData : mItemList) {
//				itemData.textView.setTextColor(mTextColor);
//			}

			ret = true;
			break;
		}
		mAddBtn.setVisibility(View.VISIBLE);
		return ret;
	}
}
