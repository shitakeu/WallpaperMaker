package jp.takesin.wallpapermaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditPreviewActivity extends Activity {

	private static final String TAG = EditPreviewActivity.class.getSimpleName();
	private Button mAddBtn;
	private int mBackgroundColor = Color.BLACK;
	private ArrayList<TextView> mTextViewList = new ArrayList<TextView>();
	private int mTextColor = Color.WHITE;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_edit_preview);

		mAddBtn = (Button) findViewById(R.id.BtnAdd);
		mAddBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showAddTextDialog();
			}
		});

	}

	private float mTextSize = 20;

	private void showAddTextDialog() {

		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.dialog_add_text, null,
				false);

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapter.add("大");
		adapter.add("中");
		adapter.add("小");
		final Spinner spinner = (Spinner) layout.findViewById(R.id.SpnTextSize);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					mTextSize = 30;
				} else if (position == 1) {
					mTextSize = 20;
				} else {
					mTextSize = 10;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		final EditText editView = (EditText) layout
				.findViewById(R.id.edtAddText);

		new AlertDialog.Builder(EditPreviewActivity.this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle("追加する文字を入力")
				.setView(layout)
				.setPositiveButton("追加", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						addTextView(editView.getText().toString());
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
		final DragViewListener drListener = new DragViewListener(textView);
		textView.setOnTouchListener(drListener);

		addContentView(textView, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		mTextViewList.add(textView);
	}

	/**
	 * ドラック用リスナー
	 * 
	 * @author mtb_cc_sin5
	 * 
	 */
	public class DragViewListener implements OnTouchListener {
		// ドラッグ対象のView
		private TextView mTxtView;
		// ドラッグ中に移動量を取得するための変数
		private int oldx;
		private int oldy;

		public DragViewListener(TextView dragView) {
			this.mTxtView = dragView;
		}

		@Override
		public boolean onTouch(View view, MotionEvent event) {
			// タッチしている位置取得
			int x = (int) event.getRawX();
			int y = (int) event.getRawY();

			switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				// 今回イベントでのView移動先の位置
				int left = mTxtView.getLeft() + (x - oldx);
				int top = mTxtView.getTop() + (y - oldy);
				// Viewを移動する
				mTxtView.layout(left, top, left + mTxtView.getWidth(), top
						+ mTxtView.getHeight());
				break;
			}

			// 今回のタッチ位置を保持
			oldx = x;
			oldy = y;
			// イベント処理完了
			return true;
		}
	}

	private static final int MENU_ID_SAVE = (Menu.FIRST + 1);
	private static final int MENU_ID_SETUP = (Menu.FIRST + 2);
	private static final int MENU_ID_COLOR = (Menu.FIRST + 3);

	// オプションメニューが最初に呼び出される時に1度だけ呼び出されます
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// メニューアイテムを追加します
		menu.add(Menu.NONE, MENU_ID_SAVE, Menu.NONE, "画像を保存");
		menu.add(Menu.NONE, MENU_ID_SETUP, Menu.NONE, "壁紙に設定する");
		menu.add(Menu.NONE, MENU_ID_COLOR, Menu.NONE, "背景色を変える");
		return super.onCreateOptionsMenu(menu);
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
		case MENU_ID_SAVE:

			final boolean saveFlag = BitmapUtil.saveBitmapToSd(getBitmap());
			String txt = "保存しました。";
			if (!saveFlag) {
				txt = "保存に失敗しました。";
			}
			Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT)
					.show();

			ret = true;
			break;
		case MENU_ID_SETUP:
			final WallpaperManager wallman = WallpaperManager.getInstance(this);
			try {
				wallman.setBitmap(getBitmap());
				Toast.makeText(getApplicationContext(), "壁紙に設定しました。",
						Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "設定に失敗しました。",
						Toast.LENGTH_SHORT).show();
			}

			ret = true;
			break;
		case MENU_ID_COLOR:
			final View view = findViewById(R.id.RelativeLayoutEditPre);
			final int color;

			if (mBackgroundColor == Color.BLACK) {
				color = Color.WHITE;
				mTextColor = Color.BLACK;
				mBackgroundColor = Color.WHITE;
			} else {
				color = Color.BLACK;
				mTextColor = Color.WHITE;
				mBackgroundColor = Color.BLACK;
			}
			view.setBackgroundColor(color);
			for (TextView txtView : mTextViewList) {
				txtView.setTextColor(mTextColor);
			}

			ret = true;
			break;
		}
		mAddBtn.setVisibility(View.VISIBLE);
		return ret;
	}

	private Bitmap getBitmap() {
		final View view = findViewById(R.id.RelativeLayoutEditPre);
		final View layout = view.getRootView();
		layout.setDrawingCacheEnabled(true);
		final Bitmap bitmap = layout.getDrawingCache();
		return bitmap;
	}

}
