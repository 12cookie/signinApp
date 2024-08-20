package com.example.signinapp.model

import com.google.gson.annotations.SerializedName

data class RequestPayload(

	@field:SerializedName("inputData")
	val inputData: InputData? = null,

	@field:SerializedName("pipelineTasks")
	val pipelineTasks: List<PipelineTasksItem?>? = null
)

data class InputData(

	@field:SerializedName("input")
	val input: List<InputItem?>? = null
)

data class PipelineTasksItem(

	@field:SerializedName("taskType")
	val taskType: String? = null,

	@field:SerializedName("config")
	val config: Config? = null
)

data class Language(

	@field:SerializedName("targetLanguage")
	val targetLanguage: String? = null,

	@field:SerializedName("sourceLanguage")
	val sourceLanguage: String? = null
)

data class Config(

	@field:SerializedName("isSentence")
	val isSentence: Boolean? = null,

	@field:SerializedName("numSuggestions")
	val numSuggestions: Int? = null,

	@field:SerializedName("language")
	val language: Language? = null,

	@field:SerializedName("serviceId")
	val serviceId: String? = null
)

data class InputItem(

	@field:SerializedName("source")
	val source: String? = null
)
