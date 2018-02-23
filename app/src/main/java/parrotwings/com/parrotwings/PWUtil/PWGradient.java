package parrotwings.com.parrotwings.PWUtil;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

/**
 * Created by roman on 23.02.2018.
 */

public class PWGradient {
	public static Bitmap bitmapGradient(int w, int h, int colorStart, int colorEnd) {
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint p = new Paint();

		Shader shader = new LinearGradient(0, 0, w - 1, h - 1, colorStart, colorEnd, Shader.TileMode.MIRROR);
		p.setShader(shader);
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);

		return bitmap;
	}
}
