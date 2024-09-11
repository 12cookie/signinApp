package com.example.signinapp.model.pipelinecompute

import com.example.signinapp.model.pipelineconfig.PipelineTask

data class Compute(
    val pipelineTasks: List<PipelineTask>,
    val inputData: InputData
)