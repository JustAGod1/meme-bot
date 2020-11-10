package ru.justagod.meme.data

sealed class SyncTask

data class UploadTask(val meme: Meme): SyncTask()

data class DeleteTask(val userId: Long, val tag: String): SyncTask()
