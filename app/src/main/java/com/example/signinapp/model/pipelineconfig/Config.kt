package com.example.signinapp.model.pipelineconfig

data class Config(
    val language: Language,
    val serviceId: String? = null,
    val gender: String? = null
)