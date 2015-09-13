package networkimageviewer.com.networkimageviewer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int URL_NOT_EMPTY = 0;
    private EditText etUrl;
    private ImageView ivDisplay;

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DownloadImageTask.SUCCESS:
                    ivDisplay.setImageBitmap((Bitmap) msg.obj);
                    break;
                default:
                    ivDisplay.setImageResource(R.drawable.github404);
                    Toast.makeText(getApplicationContext(), (CharSequence) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public static void generateMessageAndHandle(Handler handler, int what, Object errorInfo) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = errorInfo;
        handler.sendMessage(msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUrl = (EditText) findViewById(R.id.etUrl);
        ivDisplay = (ImageView) findViewById(R.id.ivDisplay);
    }

    public void navigateClickHandler(View view) {
        String url = etUrl.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            String notEmpty = "URL不能为空";
            Log.w(TAG, notEmpty);
            generateMessageAndHandle(handler, URL_NOT_EMPTY, notEmpty);
        } else {
            Executor executor = Executors.newCachedThreadPool();
            executor.execute(new DownloadImageTask(url, handler));
        }

    }
}

class DownloadImageTask implements Runnable {

    public static final int SUCCESS = 1;
    public static final int URL_ERROR = 2;
    public static final int IO_ERROR = 3;
    public static final int RESPONSE_CODE_ERROR = 4;
    private static final String TAG = DownloadImageTask.class.getSimpleName();
    private String url;
    private Handler handler;

    DownloadImageTask(String url, Handler handler) {
        this.url = url;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            URL url = new URL(this.url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(5000);
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                MainActivity.generateMessageAndHandle(handler, SUCCESS, bitmap);
            } else {
                String errorInfo = "ResponseCode Error";
                Log.e(TAG, errorInfo);
                MainActivity.generateMessageAndHandle(handler, RESPONSE_CODE_ERROR, errorInfo);
            }


        } catch (MalformedURLException e) {
            String errorInfo = "URL错误";
            Log.e(TAG, errorInfo, e);
            MainActivity.generateMessageAndHandle(handler, URL_ERROR, errorInfo);
        } catch (IOException e) {
            String errorInfo = "网络错误";
            Log.e(TAG, errorInfo, e);
            MainActivity.generateMessageAndHandle(handler, IO_ERROR, errorInfo);
        }
    }


}


