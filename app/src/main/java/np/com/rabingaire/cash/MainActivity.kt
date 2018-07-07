package np.com.rabingaire.cash

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.wonderkiln.camerakit.*
import java.util.*
import java.util.concurrent.Executors
import android.view.MotionEvent




class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    // Implementation of TextToSpeech Abstract class
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
            } else {
                tts!!.speak("Please Touch the screen to capture image.", TextToSpeech.QUEUE_FLUSH, null,"")
            }

        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    companion object {
        private const val MODEL_PATH = "cash_mobile_graph.lite"
        private const val LABEL_PATH = "labels.txt"
        private const val INPUT_SIZE = 224
    }

    lateinit var classifier: Classifier
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var cameraView: CameraView
    private lateinit var resultOutput: TextView
    private lateinit var captureButton: Button
    private lateinit var tts: TextToSpeech
    private lateinit var builder: AlertDialog.Builder
    private lateinit var dialogView: View
    private lateinit var dialogMessage: TextView
    private lateinit var dialog: AlertDialog

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initTensorFlowAndLoadModel()
        //Text to Speech
        tts = TextToSpeech(this, this)

        cameraView = findViewById(R.id.cameraView)
        resultOutput = findViewById(R.id.resultOutput)
        captureButton = findViewById(R.id.captureButton)

        // Dialog Box
        builder = AlertDialog.Builder(this)
        dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)
        dialogMessage = dialogView.findViewById(R.id.message)
        dialogMessage.text = "Computing..."
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()


        cameraView.addCameraKitListener(object : CameraKitEventListener {
            override fun onEvent(cameraKitEvent: CameraKitEvent) {

            }

            override fun onError(cameraKitError: CameraKitError) {

            }

            override fun onImage(cameraKitImage: CameraKitImage) {

                var bitmap = cameraKitImage.bitmap

                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)

                val results = classifier.recognizeImage(bitmap)
                var result: String

                if (results.isNotEmpty()) {
                    result = results[0].toString()
                } else {
                    result = "Can't identify please try again !!!"
                }

                dialog.dismiss()

                // speak the result
                tts!!.speak(result, TextToSpeech.QUEUE_FLUSH, null,"")

                resultOutput.text = result
            }

            override fun onVideo(cameraKitVideo: CameraKitVideo) {

            }
        })

        captureButton.setOnClickListener {
            cameraView.captureImage()
            // text to speech speech
            tts!!.speak("Computing Please Wait...", TextToSpeech.QUEUE_FLUSH, null,"")
            dialog.show()
        }
        cameraView.setOnTouchListener({ _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                cameraView.captureImage()
                // text to speech speech
                tts!!.speak("Computing Please Wait...", TextToSpeech.QUEUE_FLUSH, null,"")
                dialog.show()
            }
            true
        })
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
    }

    override fun onPause() {
        super.onPause()
        cameraView.stop()
    }

    override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
        executor.execute { classifier.close() }
    }

    private fun initTensorFlowAndLoadModel() {
        executor.execute {
            try {
                classifier = Classifier.create(
                        assets,
                        MODEL_PATH,
                        LABEL_PATH,
                        INPUT_SIZE)
            } catch (e: Exception) {
                throw RuntimeException("Error initializing TensorFlow!", e)
            }
        }
    }
}
