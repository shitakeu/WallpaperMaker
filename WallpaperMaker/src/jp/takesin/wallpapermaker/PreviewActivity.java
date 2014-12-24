package jp.takesin.wallpapermaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class PreviewActivity extends Activity {

	private static final String TAG = PreviewActivity.class.getSimpleName();

	final int[] mTxtViewIds = { R.id.TextPre1, 
			/*R.id.TextPre2, R.id.TextPre3,
			R.id.TextPre4, R.id.TextPre5 */
			};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_preview);

		final Bundle extr = getIntent().getExtras();
		String txt = "";
		String[] arr = { "" };
		int color = 0;
		if (extr != null) {
			txt = extr.getString(Const.KEY_INTENT_TEXT);
			arr = txt.split(Const.DELIMITER);
			color = extr.getInt(Const.KEY_INTENT_COLOR);
		}
		
		final int TextColer;
		if (color == 1) {
			findViewById(R.id.RelativeLayoutPre)
					.setBackgroundColor(Color.WHITE);
			TextColer = Color.BLACK;
		} else {
			TextColer = Color.WHITE;
		}

		final Random rnd = new Random();
		final int length = arr.length;
		for(int i = 0; i < length; i++){
			final int txtRan = rnd.nextInt(length);
			final TextView txtView = (TextView) findViewById(mTxtViewIds[txtRan]);
			txtView.setText(arr[i]);
			txtView.setTextColor(TextColer);
		}

		Toast.makeText(getApplicationContext(), "メニューボタンから設定できます。",
				Toast.LENGTH_LONG).show();

	}

	private static final int MENU_ID_SAVE = (Menu.FIRST + 1);
	private static final int MENU_ID_SETUP = (Menu.FIRST + 2);

	// オプションメニューが最初に呼び出される時に1度だけ呼び出されます
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// メニューアイテムを追加します
		menu.add(Menu.NONE, MENU_ID_SAVE, Menu.NONE, "画像を保存");
		menu.add(Menu.NONE, MENU_ID_SETUP, Menu.NONE, "壁紙に設定する");
		return super.onCreateOptionsMenu(menu);
	}

	// オプションメニューアイテムが選択された時に呼び出されます
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;
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
		}
		return ret;
	}

	private Bitmap getBitmap() {
		final View view = findViewById(R.id.RelativeLayoutPre);
		final View layout = view.getRootView();
		layout.setDrawingCacheEnabled(true);
		final Bitmap bitmap = layout.getDrawingCache();
		return bitmap;
	}

}
