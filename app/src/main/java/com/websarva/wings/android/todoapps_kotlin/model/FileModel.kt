package com.websarva.wings.android.todoapps_kotlin.model

import androidx.annotation.Keep

@Keep
data class DownloadStatus(
    val list: String = "list_list",
    val iv_aes_list: String = "iv_aes_list",
    val salt_list: String = "salt_list",
    val task: String = "task_task",
    val iv_aes_task: String = "iv_aes_task",
    val salt_task: String = "salt_task"
)

@Keep
data class FileName(
    val list: String = "list",
    val iv_aes: String = "iv_aes",
    val salt: String = "salt",
    val task: String = "task"
)
