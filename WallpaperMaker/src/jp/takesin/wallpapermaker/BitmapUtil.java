package jp.takesin.wallpapermaker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.widget.Toast;

public class BitmapUtil {

	@SuppressWarnings("unused")
	private static final String TAG = BitmapUtil.class.getSimpleName();

	public static boolean saveBitmapToSd(Bitmap mBitmap) {
		// sdcardフォルダを指定
		final File pathExternalPublicDir =	Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

		// DCIMフォルダーのパス
		final String dir = pathExternalPublicDir.getPath();
		final File root = new File(dir);
//		final File root = Environment.getExternalStorageDirectory();

		// 日付でファイル名を作成　
		final Date mDate = new Date();
		final SimpleDateFormat fileName = new SimpleDateFormat(
				"yyyyMMdd_HHmmss");

		// 保存処理開始
		try {
			final FileOutputStream fos = new FileOutputStream(new File(root,
					fileName.format(mDate) + ".jpg"));
			// jpegで保存
			mBitmap.compress(CompressFormat.JPEG, 100, fos);
			// 保存処理終了
			fos.close();
			
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;

	}
}
