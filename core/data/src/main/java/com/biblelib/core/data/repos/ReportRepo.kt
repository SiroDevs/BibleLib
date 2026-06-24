package com.biblelib.core.data.repos

import com.biblelib.core.network.dtos.SongReportRequest
import com.biblelib.core.network.dtos.SongReportResponse
import com.biblelib.core.network.services.BibleLibService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepo @Inject constructor(
    private val service: BibleLibService
) {
    suspend fun submitReport(request: SongReportRequest): SongReportResponse =
        service.submitReport(request)
}
