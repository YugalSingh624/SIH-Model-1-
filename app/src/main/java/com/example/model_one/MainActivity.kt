package com.example.model_one

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import java.util.Locale


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {


    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var resultTextView: TextView
    private lateinit var matchTextView: TextView
    private lateinit var startButton: Button
    private lateinit var processTextButton: Button
    private lateinit var textInputEditText: EditText
    private lateinit var gifImageView: ImageView
    private lateinit var textToSpeech: TextToSpeech
    private val RECORD_AUDIO_PERMISSION_CODE = 1
    private val handler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        resultTextView = findViewById(R.id.resultTextView)
        matchTextView = findViewById(R.id.matchTextView)
        startButton = findViewById(R.id.startButton)
        processTextButton = findViewById(R.id.processTextButton)
        textInputEditText = findViewById(R.id.textInputEditText)
        gifImageView = findViewById(R.id.gifImageView)


        textToSpeech = TextToSpeech(this, this)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
        }


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)


        startButton.setOnClickListener {
            startListening()
        }


        processTextButton.setOnClickListener {
            val inputText = textInputEditText.text.toString().lowercase(Locale.getDefault())
            resultTextView.text = inputText
            matchGifPhrase(inputText)
            speakText(inputText)
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak something...")
        }


        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Toast.makeText(applicationContext, "Listening...", Toast.LENGTH_SHORT).show()
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onBeginningOfSpeech() {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                Toast.makeText(applicationContext, "Error recognizing speech", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val result = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                result?.let {
                    val recognizedText = it[0].lowercase(Locale.getDefault())  // Convert to lowercase
                    resultTextView.text = recognizedText  // Display the text
                    Log.d("SpeechRecognition", "Recognized text: $recognizedText")


                    matchGifPhrase(recognizedText)
                    speakText(recognizedText)
                }
            }
        })


        speechRecognizer.startListening(intent)
    }

    @SuppressLint("SetTextI18n")
    private fun matchGifPhrase(recognizedText: String) {
        val gifList = getGifList()
        val matchedGif = gifList.find { it.phrase == recognizedText }

        if (matchedGif != null) {
            matchTextView.text = "Matched Phrase: ${matchedGif.phrase}"
            // Display the matching GIF
            displayGif(matchedGif.phrase)
        } else {
            matchTextView.text = "No Match Found"
            displayLetterImages(recognizedText)
        }
    }


    private fun displayGif(phrase: String) {
        val gifPath = "file:///android_asset/ISL_Gifs/$phrase.gif"


        Glide.with(this)
            .asGif()
            .load(gifPath)
            .into(gifImageView)
    }


    private fun displayLetterImages(recognizedText: String) {
        val letters = recognizedText.replace("\\s".toRegex(), "") // Remove spaces

        if (letters.isNotEmpty()) {
            showLetterImageSequence(letters, 0)
        }
    }


    private fun showLetterImageSequence(letters: String, index: Int) {
        if (index < letters.length) {
            val currentLetter = letters[index]

            if (currentLetter.isLetter()) {
                val imagePath = "file:///android_asset/letters/${currentLetter}.jpg"


                Glide.with(this)
                    .load(imagePath)
                    .placeholder(R.drawable.ic_launcher_background)  // Set a placeholder image
                    .into(gifImageView)


                applyFadeTransition(gifImageView)


                handler.postDelayed({
                    showLetterImageSequence(letters, index + 1)
                }, 1000)
            } else {

                showLetterImageSequence(letters, index + 1)
            }
        }
    }


    private fun applyFadeTransition(imageView: ImageView) {
        val fadeIn = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = 500
        imageView.startAnimation(fadeIn)
    }


    private fun speakText(text: String) {
        if (text.isNotEmpty()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val langResult = textToSpeech.setLanguage(Locale.getDefault())
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Language not supported or missing data", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Text-to-Speech Initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
        handler.removeCallbacksAndMessages(null)
    }
}