package jp.gr.java_conf.ya.qrmaker; // Copyright (c) 2013-2016 YA <ya.androidapp@gmail.com> All rights reserved.

import java.util.Hashtable;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QrMakerActivity extends Activity {
	private Button button1;
	private Button button2;
	private EditText editText1;
	private ImageView imageView1;

	private static int codesize = 300;

	// エンコード設定
	private static final String ENCORD_NAME = "utf-8";

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		button1 = (Button) this.findViewById(R.id.button1);
		button2 = (Button) this.findViewById(R.id.button2);
		editText1 = (EditText) this.findViewById(R.id.editText1);
		imageView1 = (ImageView) this.findViewById(R.id.imageView1);

		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (editText1.getText().toString().equals("") == false) {
					imageView1.setImageResource(android.R.color.transparent);

					Drawable d = new BitmapDrawable(getResources(), createQRCode(editText1.getText().toString()));
					if (d != null) {
						imageView1.setImageDrawable(d);
					}
				}
			}
		});

		button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				editText1.setText("");

				imageView1.setImageResource(android.R.color.transparent);
			}
		});

		button2.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				new AlertDialog.Builder(QrMakerActivity.this)
						.setTitle(R.string.app_name)
						.setMessage(
								getString(R.string.copyright) + "\n\n" + getString(R.string.license) + "\n"
										+ getString(R.string.zxing) + "\n")
						.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
							}
						}).create().show();

				return false;
			}
		});

		WindowManager windowmanager = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display disp = windowmanager.getDefaultDisplay();
		int width = disp.getWidth();
		int height = disp.getHeight();
		codesize = Math.min(width, height) * 2 / 3;

		get_intent(getIntent());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Bitmap createQRCode(String contents) {
		if (contents.equals("") == false) {
			Bitmap ret = null;
			// QRコードを生成
			QRCodeWriter writer = new QRCodeWriter();
			Hashtable encodeHint = new Hashtable();
			encodeHint.put(EncodeHintType.CHARACTER_SET, ENCORD_NAME);
			encodeHint.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
			try {
				BitMatrix bitData = writer.encode(contents, BarcodeFormat.QR_CODE, codesize, codesize, encodeHint);
				int width = bitData.getWidth();
				int height = bitData.getHeight();
				int[] pixels = new int[width * height];
				// All are 0, or black, by default
				for (int y = 0; y < height; y++) {
					int offset = y * width;
					for (int x = 0; x < width; x++) {
						pixels[offset + x] = bitData.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
					}
				}
				// Bitmapに変換
				ret = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				ret.setPixels(pixels, 0, width, 0, 0, width, height);
				return ret;
			} catch (WriterException e) {
			}
		}
		return null;
	}

	void get_intent(final Intent intent) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Intent receivedIntent = intent;
				try {
					if (receivedIntent == null) {
						return;
					}
				} catch (Exception e) {
					return;
				}

				String intentDataStr = "";
				String action = receivedIntent.getAction();

				if ((Intent.ACTION_VIEW.equals(action)) || (Intent.ACTION_SEND.equals(action))) {
					if (receivedIntent.getDataString() != null) {
						try {
							intentDataStr += " "
									+ ((receivedIntent.getDataString().equals("")) ? "" : receivedIntent
											.getDataString());
						} catch (Exception e) {
							//
						}
					}
					if (receivedIntent.getData() != null) {
						try {
							intentDataStr += " "
									+ ((receivedIntent.getData().equals("")) ? "" : receivedIntent.getData().toString());
						} catch (Exception e) {
							//
						}
					}

					Bundle extras = receivedIntent.getExtras();
					if (extras != null) {
						String intentExtraText = "";

						Iterator<?> it = extras.keySet().iterator();
						while (it.hasNext()) {
							String key = (String) it.next();
							intentExtraText += " " + extras.get(key).toString();
						}
						if (intentExtraText.equals("") == false) {
							try {
								intentDataStr = intentDataStr + " "
										+ new String(intentExtraText.getBytes("UTF8"), "UTF8");
							} catch (Exception e) {
								intentDataStr = intentDataStr + " " + intentExtraText;
							}
						}
					}
				}

				if (intentDataStr.equals("") == false) {
					final String finalIntentDataStr = intentDataStr;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							editText1.setText(finalIntentDataStr.toString());
						}
					});
				}

				receivedIntent = null;
			}
		}).start();
	}
}
