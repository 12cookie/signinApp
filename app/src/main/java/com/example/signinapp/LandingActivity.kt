package com.example.signinapp

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class LandingActivity : AppCompatActivity()
{
    private lateinit var sText: EditText
    private lateinit var requestQueue: RequestQueue
    private lateinit var translatedText: TextView
    private lateinit var spinner: Spinner
    private lateinit var submitButton: Button
    private lateinit var synthesize: Button
    private lateinit var logoutButton: ImageView
    private lateinit var serviceID: String
    private lateinit var auth: String
    private lateinit var audio: ImageView
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var loader:ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landing)

        sText = findViewById(R.id.textInput)
        translatedText = findViewById(R.id.translatedText)
        spinner = findViewById(R.id.spinner)

        audio = findViewById(R.id.audio)
        loader=findViewById(R.id.loader)
        mediaPlayer = MediaPlayer()

        val languages = resources.getStringArray(R.array.Languages)
        val languageCode = resources.getStringArray(R.array.Language_Code)

        val sourceLanguage = "en"
        var targetLanguage: String = ""
        var text: String = ""

        Glide.with(this).load(R.drawable.audio).into(audio)
        Glide.with(this).load(R.drawable.loader).into(loader)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener
            {
                override fun onItemSelected (parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long)
                {
                    if (position > 0)
                    {
                        targetLanguage = languageCode[position - 1]
                        Toast.makeText(this@LandingActivity, getString(R.string.selected_language) + " " + languages[position], Toast.LENGTH_SHORT).show()
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
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                text = sText.text.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        sText.addTextChangedListener(loginWatcher)

        submitButton = findViewById<Button>(R.id.submit)
        submitButton.setOnClickListener {
            if (targetLanguage.isEmpty())
            {
                Toast.makeText(this@LandingActivity, getString(R.string.language_message), Toast.LENGTH_SHORT).show()
            }
            else
            {
                loader.visibility=ImageView.VISIBLE
                requestQueue = Volley.newRequestQueue(this)
                makeApiRequest(sourceLanguage, targetLanguage, text)
            }
        }

        synthesize = findViewById(R.id.synthesize)
        synthesize.setOnClickListener {
            if (targetLanguage.isEmpty())
            {
                Toast.makeText(this@LandingActivity, getString(R.string.language_message), Toast.LENGTH_SHORT).show()
            }
            else
            {
                requestQueue = Volley.newRequestQueue(this)
                loader.visibility=ImageView.VISIBLE
                getServiceID(targetLanguage)
            }
        }

        logoutButton = findViewById(R.id.logout)
        logoutButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun makeApiRequest(sourceLanguage: String, targetLanguage: String?, text: String)
    {
        val jsonPayload = JSONObject().apply {
            put("pipelineTasks", JSONArray().apply {
                put(JSONObject().apply {
                    put("taskType", "translation")
                    put("config", JSONObject().apply {
                        put("language", JSONObject().apply {
                            put("sourceLanguage", sourceLanguage)
                            put("targetLanguage", targetLanguage)
                        })
                        put("serviceId", "ai4bharat/indictrans-v2-all-gpu--t4")
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
                val responseText = outputItem.getString("target")
                translatedText.text = responseText
                Log.d("API Response", responseText)
                loader.visibility=ImageView.GONE
            },
            Response.ErrorListener
            {
                error ->
                Log.e("API Error", error.toString())
                loader.visibility=ImageView.GONE
            })
        {
            override fun getHeaders(): MutableMap<String, String>
            {
                return mutableMapOf (
                    "Accept" to "*/*",
                    "User-Agent" to "Thunder Client (https://www.thunderclient.com)",
                    "Authorization" to "J4G_NJzGen06N8KiAxeGCB-IJHeLvFz72jCPD9Cs3Mg5CAAUeElc3XXiqfSK5nP3",
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
                playAudio(responseText)
                loader.visibility=ImageView.GONE
            },
            Response.ErrorListener
            {
                error ->
                Log.e("API Error", error.toString())
                loader.visibility=ImageView.GONE
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

    private fun getServiceID(targetLanguage: String?)
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
                Log.d("API Error", serviceID)

                val pipelineInferenceAPIEndPoint = response.getJSONObject("pipelineInferenceAPIEndPoint")
                val inferenceApiKey = pipelineInferenceAPIEndPoint.getJSONObject("inferenceApiKey")
                auth = inferenceApiKey.getString("value")
                Log.d("API Error", auth)

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
                audio.visibility=ImageView.VISIBLE
                loader.visibility=ImageView.GONE

            }
            mediaPlayer.setOnCompletionListener { mp ->
                mp.stop()
                mp.release()
                audio.visibility=ImageView.GONE
                loader.visibility=ImageView.GONE
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
}