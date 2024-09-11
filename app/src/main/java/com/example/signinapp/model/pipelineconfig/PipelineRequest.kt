package com.example.signinapp.model.pipelineconfig

data class PipelineRequest(
    val pipelineTasks: List<PipelineTask>,
    val pipelineRequestConfig: PipelineRequestConfig
)