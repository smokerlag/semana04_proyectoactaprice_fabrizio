package com.example.actapriceproyect.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class SignatureView extends View {

    private Paint paint;
    private Path path;
    private boolean hasDrawn = false;

    public SignatureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(8f);

        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true); // 🚨 EVITAR QUE EL SCROLLVIEW SE LLEVE EL EVENTO TÁCTIL
                path.moveTo(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                hasDrawn = true;
                break;
            case MotionEvent.ACTION_UP:
                // El dedo se levantó, terminamos el trazo actual de forma limpia
                break;
            default:
                return false;
        }

        invalidate(); // Redibuja la vista inmediatamente
        return true;
    }

    public void clear() {
        path.reset();
        hasDrawn = false;
        invalidate();
    }

    public boolean isCanvasEmpty() {
        return !hasDrawn;
    }

    public android.graphics.Bitmap getSignatureBitmap() {
        if (!hasDrawn) return null;
        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(getWidth(), getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE); // Fondo blanco para el Word
        draw(canvas);
        return bitmap;
    }
}
