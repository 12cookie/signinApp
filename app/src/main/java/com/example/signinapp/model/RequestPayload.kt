package com.example.signinapp.model

import com.google.gson.annotations.SerializedName

data class Response(

	@field:SerializedName("inputData")
	val inputData: InputData? = null,

	@field:SerializedName("pipelineTasks")
	val pipelineTasks: List<PipelineTasksItem?>? = null
)

data class PipelineTasksItem(

	@field:SerializedName("taskType")
	val taskType: String? = null,

	@field:SerializedName("config")
	val config: Config? = null
)

data class InputData(

	@field:SerializedName("input")
	val input: List<InputItem?>? = null,

	@field:SerializedName("audio")
	val audio: List<AudioItem?>? = null
)

data class Config(

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("language")
	val language: Language? = null,

	@field:SerializedName("serviceId")
	val serviceId: String? = null
)

data class Language(

	@field:SerializedName("sourceLanguage")
	val sourceLanguage: String? = null,

	@field:SerializedName("targetLanguage")
	val targetLanguage: String? = null
)

data class InputItem(

	@field:SerializedName("source")
	val source: String? = null
)

data class AudioItem(

	@field:SerializedName("audioContent")
	val audioContent: Any? = null
)
