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
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.signinapp.R.layout
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class LandingActivity : AppCompatActivity()
{
    private lateinit var sText: EditText
    private lateinit var requestQueue: RequestQueue
    private lateinit var translatedText: TextView
    private lateinit var spinner: Spinner
    private lateinit var synthesize: Button
    private lateinit var logoutButton: ImageView
    private lateinit var feedbackButton: Button
    private lateinit var serviceID: String
    private lateinit var auth: String
    private lateinit var authTranslation: String
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audio: ImageView
    private val delayMillis: Long = 1000
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private lateinit var translateLoader: ProgressBar
    private lateinit var audioLoader: ProgressBar

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
        mediaPlayer = MediaPlayer()
        translateLoader = findViewById(R.id.loader)
        audioLoader = findViewById(R.id.audioLoader)

        val languages = resources.getStringArray(R.array.Languages)
        val languageCode = resources.getStringArray(R.array.Language_Code)

        val sourceLanguage = "en"
        var targetLanguage = ""
        var text: String

        Glide.with(this).load(R.drawable.audio).into(audio)

        val loginWatcher = object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                text = sText.text.toString()
                runnable?.let { handler.removeCallbacks(it) }
                runnable = Runnable {
                    if(targetLanguage.isNotEmpty() && text.isNotEmpty())
                    {
                        translateLoader.visibility = View.VISIBLE
                        getTranslationServiceID(sourceLanguage, targetLanguage, text)
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

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener
        {
            override fun onItemSelected (parent: AdapterView<*>, view: View, position: Int, id: Long)
            {
                if (position > 0)
                {
                    targetLanguage = languageCode[position - 1]
                    text = sText.text.toString()
                    Toast.makeText(this@LandingActivity, getString(R.string.selected_language) + " " + languages[position], Toast.LENGTH_SHORT).show()
                    if(text.isNotEmpty())
                    {
                        translateLoader.visibility = View.VISIBLE
                        getTranslationServiceID(sourceLanguage, targetLanguage, text)
                    }
                }
                else
                {
                    targetLanguage = ""
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        synthesize = findViewById(R.id.synthesize)
        synthesize.setOnClickListener {
            if (targetLanguage.isEmpty())
            {
                Toast.makeText(this@LandingActivity, getString(R.string.language_message), Toast.LENGTH_SHORT).show()
            }
            else
            {
                audioLoader.visibility = View.VISIBLE
                synthesize.textSize = 0F
                synthesize.isEnabled = false
                requestQueue = Volley.newRequestQueue(this)
                getAudioServiceID(targetLanguage)
            }
        }

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

    private fun getTranslationServiceID(sourceLanguage: String, targetLanguage: String?, text: String)
    {
        requestQueue = Volley.newRequestQueue(this)
        val jsonPayload = JSONObject().apply {
            put("pipelineTasks", JSONArray().apply {
                put(JSONObject().apply {
                    put("taskType", "translation")
                    put("config", JSONObject().apply {
                        put("language", JSONObject().apply {
                            put("sourceLanguage", sourceLanguage)
                            put("targetLanguage", targetLanguage)
                        })
                    })
                })
            })
            put("pipelineRequestConfig", JSONObject().apply {
                put("pipelineId", "64392f96daac500b55c543cd")
            })
        }

        Log.e("Payload", jsonPayload.toString())
        val request = object : JsonObjectRequest
            (Method.POST,
            "https://meity-auth.ulcacontrib.org/ulca/apis/v0/model/getModelsPipeline",
            jsonPayload,
            Response.Listener
            {
                response ->
                val pipelineResponseConfig = response.getJSONArray("pipelineResponseConfig")
                val firstItem = pipelineResponseConfig.getJSONObject(0)
                val config = firstItem.getJSONArray("config")
                val outputItem = config.getJSONObject(0)
                val servID = outputItem.getString("serviceId")

                val pipelineInferenceAPIEndPoint = response.getJSONObject("pipelineInferenceAPIEndPoint")
                val inferenceApiKey = pipelineInferenceAPIEndPoint.getJSONObject("inferenceApiKey")
                authTranslation = inferenceApiKey.getString("value")

                Log.d("API", response.toString())
                makeApiRequest(sourceLanguage, targetLanguage, text, servID)
            },
            Response.ErrorListener
            {
                    error ->
                Log.e("API Error", error.toString())
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

    private fun makeApiRequest(sourceLanguage: String, targetLanguage: String?, text: String, serviceID: String?)
    {
        requestQueue = Volley.newRequestQueue(this)
        val jsonPayload = JSONObject().apply {
            put("pipelineTasks", JSONArray().apply {
                put(JSONObject().apply {
                    put("taskType", "translation")
                    put("config", JSONObject().apply {
                        put("language", JSONObject().apply {
                            put("sourceLanguage", sourceLanguage)
                            put("targetLanguage", targetLanguage)
                        })
                        put("serviceId", serviceID)
                    })
                })
            })
            put("inputData", JSONObject().apply {
                put("input", JSONArray().apply {
                    put(JSONObject().apply {
                        put("source", text)
                    })
                })
                put("audio", JSONArray().apply {
                    put(JSONObject().apply {
                        put("audioContent", null)
                    })
                })
            })
        }
        Log.e("Payload", jsonPayload.toString())
        val request = object : JsonObjectRequest
            (Method.POST,
            "https://dhruva-api.bhashini.gov.in/services/inference/pipeline",
            jsonPayload,
            Response.Listener
            {
                response ->
                val pipelineResponse = response.getJSONArray("pipelineResponse")
                val firstItem = pipelineResponse.getJSONObject(0)
                val output = firstItem.getJSONArray("output")
                val outputItem = output.getJSONObject(0)
                val responseText = outputItem.getString("target") + "    "
                translateLoader.visibility = View.GONE
                translatedText.text = responseText
                Log.d("API", response.toString())
            },
            Response.ErrorListener
            {
                    error ->
                Log.e("API Error", error.toString())
            })
        {
            override fun getHeaders(): MutableMap<String, String>
            {
                return mutableMapOf (
                    "Accept" to "*/*",
                    "User-Agent" to "Thunder Client (https://www.thunderclient.com)",
                    "Authorization" to authTranslation,
                    "Content-Type" to "application/json"
                )
            }
        }
        requestQueue.add(request)
    }

    private fun getAudioServiceID(targetLanguage: String?)
    {
        val jsonPayload = JSONObject().apply {
            put("pipelineTasks", JSONArray().apply {
                put(JSONObject().apply {
                    put("taskType", "tts")
                    put("config", JSONObject().apply {
                        put("language", JSONObject().apply {
                            put("sourceLanguage", targetLanguage)
                        })
                    })
                })
            })
            put("pipelineRequestConfig", JSONObject().apply {
                put("pipelineId", "64392f96daac500b55c543cd")
            })
        }

        Log.e("Payload", jsonPayload.toString())
        val request = object : JsonObjectRequest
            (Method.POST,
            "https://meity-auth.ulcacontrib.org/ulca/apis/v0/model/getModelsPipeline",
            jsonPayload,
            Response.Listener
            {
                    response ->
                val pipelineResponseConfig = response.getJSONArray("pipelineResponseConfig")
                val firstItem = pipelineResponseConfig.getJSONObject(0)
                val config = firstItem.getJSONArray("config")
                val outputItem = config.getJSONObject(0)
                serviceID = outputItem.getString("serviceId")
                Log.d("API", response.toString())

                val pipelineInferenceAPIEndPoint = response.getJSONObject("pipelineInferenceAPIEndPoint")
                val inferenceApiKey = pipelineInferenceAPIEndPoint.getJSONObject("inferenceApiKey")
                auth = inferenceApiKey.getString("value")

                requestQueue = Volley.newRequestQueue(this)
                synthesizeAudio(targetLanguage, translatedText.text.toString())
            },
            Response.ErrorListener
            {
                    error ->
                Log.e("API Error", error.toString())
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

    private fun synthesizeAudio(targetLanguage: String?, text: String)
    {
        val jsonPayload = JSONObject().apply {
            put("pipelineTasks", JSONArray().apply {
                put(JSONObject().apply {
                    put("taskType", "tts")
                    put("config", JSONObject().apply {
                        put("language", JSONObject().apply {
                            put("sourceLanguage", targetLanguage)
                        })
                        put("serviceId", serviceID)
                        put("gender", "female")
                    })
                })
            })
            put("inputData", JSONObject().apply {
                put("input", JSONArray().apply {
                    put(JSONObject().apply {
                        put("source", text)
                    })
                })
                put("audio", JSONArray().apply {
                    put(JSONObject().apply {
                        put("audioContent", null)
                    })
                })
            })
        }

        Log.e("Payload", jsonPayload.toString())
        val request = object : JsonObjectRequest
            (Method.POST,
            "https://dhruva-api.bhashini.gov.in/services/inference/pipeline",
            jsonPayload,
            Response.Listener
            {
                response ->
                val pipelineResponse = response.getJSONArray("pipelineResponse")
                val firstItem = pipelineResponse.getJSONObject(0)
                val output = firstItem.getJSONArray("audio")
                val outputItem = output.getJSONObject(0)
                val responseText = outputItem.getString("audioContent")
                Log.d("API", response.toString())
                playAudio(responseText)
            },
            Response.ErrorListener
            {
                error ->
                Log.e("API Error", error.toString())
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
        try
        {
            val url = "data:audio/mp3;base64,$base64FormattedString"
            val mediaPlayer = MediaPlayer()

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
                synthesize.textSize = 14F
                audio.visibility=ImageView.VISIBLE
            }
            mediaPlayer.setOnCompletionListener { mp ->
                mp.stop()
                mp.release()
                audio.visibility=ImageView.GONE
                synthesize.isEnabled = true
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
            Toast.makeText(
                this,
                "Ratings Submitted:\nTranslation: $translationRating\nTTS: $ttsRating\nSound: $soundQualityRating\nExperience: $appExperienceRating",
                Toast.LENGTH_LONG
            ).show()
            feedbackDialog.dismiss()
        }
        feedbackDialog.show()
    }
}