package parrotwings.com.parrotwings.PWUtil;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by roman on 23.02.2018.
 */

public class PWGradientTextView extends TextView {
	public PWGradientTextView(Context context) {
		super(context, null, -1);
	}

	public PWGradientTextView(Context context, AttributeSet attrs) {
		super(context, attrs, -1);
	}

	public PWGradientTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
							int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			getPaint().setShader(new LinearGradient(0, 0, 0, getHeight(),
					Color.RED, Color.BLUE, Shader.TileMode.CLAMP));
		}
	}
}
