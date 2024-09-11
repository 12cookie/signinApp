package com.example.signinapp

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.signinapp.R.layout
import com.example.signinapp.model.pipelinecompute.Audio
import com.example.signinapp.model.pipelinecompute.Compute
import com.example.signinapp.model.pipelinecompute.Input
import com.example.signinapp.model.pipelinecompute.InputData
import com.example.signinapp.model.pipelineconfig.Config
import com.example.signinapp.model.pipelineconfig.Language
import com.example.signinapp.model.pipelineconfig.PipelineRequest
import com.example.signinapp.model.pipelineconfig.PipelineRequestConfig
import com.example.signinapp.model.pipelineconfig.PipelineTask
import com.google.gson.Gson
import org.json.JSONObject
import java.io.IOException

class LandingActivity : AppCompatActivity()
{
    private lateinit var sText: EditText
    private lateinit var translatedText: TextView
    private lateinit var spinner: Spinner
    private lateinit var logoutButton: ImageView
    private lateinit var audio: ImageView
    private lateinit var audioLoader: ProgressBar
    private lateinit var feedbackButton: Button
    private lateinit var requestQueue: RequestQueue
    private lateinit var mediaPlayer: MediaPlayer
    private var auth: String = ""
    private var servIDTranslation: String = ""
    private var servIDTTS: String = ""
    private var targetLanguage: String = ""
    private var runnable: Runnable? = null
    private var isReleased = true
    private val delayMillis: Long = 1000
    private val handler = Handler(Looper.getMainLooper())

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(layout.activity_landing)

        sText = findViewById(R.id.textInput)
        translatedText = findViewById(R.id.translatedText)
        spinner = findViewById(R.id.spinner)
        audio = findViewById(R.id.audio)
        audioLoader = findViewById(R.id.audioLoader)

        val languages = resources.getStringArray(R.array.Languages)
        val languageCode = resources.getStringArray(R.array.Language_Code)
        Glide.with(this).load(R.drawable.audio).into(audio)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onItemSelected (parent: AdapterView<*>, view: View, position: Int, id: Long)
            {
                if (position > 0)
                {
                    targetLanguage = languageCode[position - 1]
                    val text = sText.text.toString()
                    Toast.makeText(this@LandingActivity, getString(R.string.selected_language) + " " + languages[position], Toast.LENGTH_SHORT).show()
                    getServiceID(text)
                }
                else
                {
                    targetLanguage = ""
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val loginWatcher = object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                val text = sText.text.toString()
                runnable?.let { handler.removeCallbacks(it) }
                runnable = Runnable {
                    if(targetLanguage.isNotEmpty() && text.isNotEmpty())
                    {
                        if(!isReleased)
                        {
                            mediaPlayer.stop()
                        }
                        audio.visibility = ImageView.GONE
                        audioLoader.visibility = View.VISIBLE
                        makeApiRequest(text)
                    }
                    else
                    {
                        translatedText.text = ""
                    }
                }
                handler.postDelayed(runnable!!, delayMillis)
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        sText.addTextChangedListener(loginWatcher)

        feedbackButton = findViewById(R.id.feedback)
        feedbackButton.setOnClickListener {
            showFeedbackDialog()
        }

        logoutButton = findViewById(R.id.logout)
        logoutButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getServiceID(text: String)
    {
        requestQueue = Volley.newRequestQueue(this)
        val langTranslation = Language("en", targetLanguage)
        val langTTS = Language(targetLanguage)
        val configTranslation = Config(langTranslation)
        val configTTS = Config(langTTS)
        val pipelineTaskTranslation = PipelineTask("translation", configTranslation)
        val pipelineTaskTTS = PipelineTask("tts", configTTS)
        val tasks = listOf(pipelineTaskTranslation, pipelineTaskTTS)
        val pipelineRequest = PipelineRequest(tasks, PipelineRequestConfig())
        Log.e("pipelineRequest", Gson().toJson(pipelineRequest))

        val request = object : JsonObjectRequest
            (Method.POST,
            "https://meity-auth.ulcacontrib.org/ulca/apis/v0/model/getModelsPipeline",
            JSONObject(Gson().toJson(pipelineRequest)),
            Response.Listener
            {
                response -> Log.d("API", response.toString())
                val pipelineResponseConfig = response.getJSONArray("pipelineResponseConfig")
                val firstItem = pipelineResponseConfig.getJSONObject(0)
                val secondItem = pipelineResponseConfig.getJSONObject(1)
                val config = firstItem.getJSONArray("config")
                val configtts = secondItem.getJSONArray("config")
                val outputItem1 = config.getJSONObject(0)
                val outputItem2 = configtts.getJSONObject(0)
                servIDTranslation = outputItem1.getString("serviceId")
                servIDTTS = outputItem2.getString("serviceId")

                val pipelineInferenceAPIEndPoint = response.getJSONObject("pipelineInferenceAPIEndPoint")
                val inferenceApiKey = pipelineInferenceAPIEndPoint.getJSONObject("inferenceApiKey")
                auth = inferenceApiKey.getString("value")

                if(text.isNotEmpty())
                {
                    if(!isReleased)
                    {
                        mediaPlayer.stop()
                    }
                    audio.visibility = ImageView.GONE
                    audioLoader.visibility = View.VISIBLE
                    makeApiRequest(text)
                }
            },
            Response.ErrorListener
            {
                error -> Log.e("API Error", error.toString())
            })
        {
            override fun getHeaders(): MutableMap<String, String>
            {
                return mutableMapOf (
                    "Accept" to "*/*",
                    "User-Agent" to "Thunder Client (https://www.thunderclient.com)",
                    "ulcaApiKey" to "2450b4fa21-7f80-4a8f-b0b3-24beb72a3138",
                    "userID" to "1697231e6cb546d4a346972eebaad3e7",
                    "Content-Type" to "application/json"
                )
            }
        }
        requestQueue.add(request)
    }

    private fun makeApiRequest(text: String)
    {
        requestQueue = Volley.newRequestQueue(this)
        val langTranslation = Language("en", targetLanguage)
        val configTranslation = Config(langTranslation, servIDTranslation)
        val pipelineTaskTranslation = PipelineTask("translation", configTranslation)
        val inputTranslation = Input(text)
        val audio = Audio(null)
        val inputData = InputData(listOf(inputTranslation), listOf(audio))
        val pipelineRequest = Compute(listOf(pipelineTaskTranslation), inputData)
        Log.e("pipelineRequest", Gson().toJson(pipelineRequest))

        val request = object : JsonObjectRequest
            (Method.POST,
            "https://dhruva-api.bhashini.gov.in/services/inference/pipeline",
            JSONObject(Gson().toJson(pipelineRequest)),
            Response.Listener
            {
                response -> Log.d("API", response.toString())
                val pipelineResponse = response.getJSONArray("pipelineResponse")
                val firstItem = pipelineResponse.getJSONObject(0)
                val output = firstItem.getJSONArray("output")
                val outputItem = output.getJSONObject(0)
                val responseText = outputItem.getString("target")
                translatedText.text = responseText
                synthesizeAudio(responseText)
            },
            Response.ErrorListener
            {
                error -> Log.e("API Error", error.toString())
            })
        {
            override fun getHeaders(): MutableMap<String, String>
            {
                return mutableMapOf (
                    "Accept" to "*/*",
                    "User-Agent" to "Thunder Client (https://www.thunderclient.com)",
                    "Authorization" to auth,
                    "Content-Type" to "application/json"
                )
            }
        }
        requestQueue.add(request)
    }

    private fun synthesizeAudio(text: String)
    {
        val langTTS = Language(targetLanguage)
        val configTTS = Config(langTTS, servIDTTS, "female")
        val pipelineTaskTTS = PipelineTask("tts", configTTS)
        val inputTTS = Input(text)
        val audio = Audio(null)
        val inputData = InputData(listOf(inputTTS), listOf(audio))
        val pipelineRequest = Compute(listOf(pipelineTaskTTS), inputData)
        Log.e("pipelineRequest", Gson().toJson(pipelineRequest))

        val request = object : JsonObjectRequest
            (Method.POST,
            "https://dhruva-api.bhashini.gov.in/services/inference/pipeline",
            JSONObject(Gson().toJson(pipelineRequest)),
            Response.Listener
            {
                response -> Log.d("API", response.toString())
                val pipelineResponse = response.getJSONArray("pipelineResponse")
                val firstItem = pipelineResponse.getJSONObject(0)
                val output = firstItem.getJSONArray("audio")
                val outputItem = output.getJSONObject(0)
                val responseText = outputItem.getString("audioContent")
                playAudio(responseText)
            },
            Response.ErrorListener
            {
                error -> Log.e("API Error", error.toString())
            })
        {
            override fun getHeaders(): MutableMap<String, String>
            {
                return mutableMapOf (
                    "Accept" to "*/*",
                    "User-Agent" to "Thunder Client (https://www.thunderclient.com)",
                    "Authorization" to auth,
                    "Content-Type" to "application/json"
                )
            }
        }
        requestQueue.add(request)
    }

    fun playAudio(base64FormattedString: String)
    {
        mediaPlayer = MediaPlayer()
        isReleased = false
        try
        {
            val url = "data:audio/mp3;base64,$base64FormattedString"
            try
            {
                mediaPlayer.setDataSource(url)
                mediaPlayer.prepareAsync()
                mediaPlayer.setVolume(100f, 100f)
                mediaPlayer.isLooping = false
            }
            catch (e: IllegalArgumentException)
            {
                Toast.makeText(
                    applicationContext,
                    "You might not set the DataSource correctly!",
                    Toast.LENGTH_LONG
                ).show()
            }
            catch (e: SecurityException)
            {
                Toast.makeText(
                    applicationContext,
                    "You might not set the DataSource correctly!",
                    Toast.LENGTH_LONG
                ).show()
            }
            catch (e: IllegalStateException)
            {
                Toast.makeText(
                    applicationContext,
                    "You might not set the DataSource correctly!",
                    Toast.LENGTH_LONG
                ).show()
            }
            catch (e: IOException)
            {
                e.printStackTrace()
            }
            mediaPlayer.setOnPreparedListener {
                player -> player.start()
                audioLoader.visibility = View.GONE
                audio.visibility = ImageView.VISIBLE
            }
            mediaPlayer.setOnCompletionListener {
                mp -> mp.stop()
                mp.release()
                isReleased = true
                audio.visibility = ImageView.GONE
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun showFeedbackDialog()
    {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(layout.activity_popup, null)

        val feedbackDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val translationAccuracyRatingBar: RatingBar = dialogView.findViewById(R.id.translation_accuracy)
        val ttsAccuracyRatingBar: RatingBar = dialogView.findViewById(R.id.tts_accuracy)
        val soundQualityRatingBar: RatingBar = dialogView.findViewById(R.id.sound_quality)
        val appExperienceRatingBar: RatingBar = dialogView.findViewById(R.id.app_experience)
        val submitButton: Button = dialogView.findViewById(R.id.submitRatingButton)

        submitButton.setOnClickListener {
            val translationRating = translationAccuracyRatingBar.rating
            val ttsRating = ttsAccuracyRatingBar.rating
            val soundQualityRating = soundQualityRatingBar.rating
            val appExperienceRating = appExperienceRatingBar.rating
            Toast.makeText(this,
                "Ratings Submitted:\nTranslation: $translationRating\nTTS: $ttsRating\nSound: $soundQualityRating\nExperience: $appExperienceRating",
                Toast.LENGTH_LONG
            ).show()
            feedbackDialog.dismiss()
        }
        feedbackDialog.show()
    }
}