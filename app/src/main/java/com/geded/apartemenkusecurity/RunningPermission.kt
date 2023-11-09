package com.geded.apartemenkusecurity

data class RunningPermission(
    val id: Int,
    val start_date: String,
    val end_date: String,
    val description: String,
    val unit_no: String,
    val tenant: String,
    val workerNum: Int,
    val workPermitsCount:Int
)
