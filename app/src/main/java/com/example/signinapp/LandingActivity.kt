package com.example.signinapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class LandingActivity : AppCompatActivity()
{
    private lateinit var sText: EditText
    private lateinit var requestQueue: RequestQueue
    private lateinit var translatedText: TextView
    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landing)

        sText = findViewById(R.id.textInput)
        translatedText = findViewById(R.id.translatedText)
        spinner = findViewById<Spinner>(R.id.spinner)

        val languages = resources.getStringArray(R.array.Languages)
        val languageCode = resources.getStringArray(R.array.Language_Code)

        val sourceLanguage = "en"
        var targetLanguage: String = ""
        var text: String = ""

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

        val submitButton = findViewById<Button>(R.id.submit)
        submitButton.setOnClickListener {
            if (targetLanguage.isEmpty())
            {
                Toast.makeText(this@LandingActivity, getString(R.string.language_message), Toast.LENGTH_SHORT).show()
            }
            else
            {
                requestQueue = Volley.newRequestQueue(this)
                makeApiRequest(sourceLanguage, targetLanguage, text)
            }
        }

        val button = findViewById<Button>(R.id.logout)
        button.setOnClickListener {
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
                    "Authorization" to "J4G_NJzGen06N8KiAxeGCB-IJHeLvFz72jCPD9Cs3Mg5CAAUeElc3XXiqfSK5nP3",
                    "Content-Type" to "application/json"
                )
            }
        }
        requestQueue.add(request)
    }
}