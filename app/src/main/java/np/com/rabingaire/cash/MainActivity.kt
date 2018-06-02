package np.com.rabingaire.cash

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onCaptureClick(view: View) {
        cameraView.captureImage { cameraKitImage ->
            var imageBitmap: Bitmap = cameraKitImage.bitmap
            Log.d("Debug","Bitmap is created")
        }
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        super.onPause()
        cameraView.stop()
    }


}
