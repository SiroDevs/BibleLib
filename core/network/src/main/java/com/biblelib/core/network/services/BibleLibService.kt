package com.biblelib.core.network.services

import androidx.annotation.Keep
import com.biblelib.core.common.utils.ApiConstants
import com.biblelib.core.database.model.BookEntity
import com.biblelib.core.database.model.SongEntity
import com.biblelib.core.network.dtos.DraftDto
import com.biblelib.core.network.dtos.EditActionResponse
import com.biblelib.core.network.dtos.EditDto
import com.biblelib.core.network.dtos.EditRejectRequest
import com.biblelib.core.network.dtos.LikeToggleRequest
import com.biblelib.core.network.dtos.LikeToggleResponse
import com.biblelib.core.network.dtos.LikedSongsResponse
import com.biblelib.core.network.dtos.ListingDto
import com.biblelib.core.network.dtos.OrganisationDto
import com.biblelib.core.network.dtos.PagedSongsResponse
import com.biblelib.core.network.dtos.SongReportRequest
import com.biblelib.core.network.dtos.SongReportResponse
import com.biblelib.core.network.dtos.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@Keep
interface BibleLibService {
    @GET(ApiConstants.BOOKS)
    suspend fun getBooks(): List<BookEntity>

    @GET("${ApiConstants.BOOKS}/{ids}")
    suspend fun getBooksByIds(@Path("ids") ids: String): List<BookEntity>

    @GET("${ApiConstants.SONGS}/books/{bookIds}")
    suspend fun getSongsPage(
        @Path("bookIds") bookIds: String,
        @Query("page")   page: Int = 1,
        @Query("limit")  limit: Int = 500,
        @Query("since")  since: String? = null
    ): PagedSongsResponse

    @GET("${ApiConstants.SONGS}/{songId}")
    suspend fun getSongById(@Path("songId") songId: Int): SongEntity

    @GET(ApiConstants.DRAFTS)
    suspend fun getDrafts(): List<DraftDto>

    @GET("${ApiConstants.DRAFTS}/{draftId}")
    suspend fun getDraft(@Path("draftId") draftId: Int): DraftDto

    @POST(ApiConstants.DRAFTS)
    suspend fun createDraft(@Body draft: DraftDto): DraftDto

    @PUT("${ApiConstants.DRAFTS}/{draftId}")
    suspend fun updateDraft(@Path("draftId") draftId: Int, @Body draft: DraftDto): DraftDto

    @DELETE("${ApiConstants.DRAFTS}/{draftId}")
    suspend fun deleteDraft(@Path("draftId") draftId: Int): Map<String, String>

    @GET(ApiConstants.USER_EDITS)
    suspend fun getEdits(): List<EditDto>

    @GET("${ApiConstants.USER_EDITS}/pending")
    suspend fun getPendingEdits(): List<EditDto>

    @GET("${ApiConstants.USER_EDITS}/user/{userId}")
    suspend fun getEditsForUser(@Path("userId") userId: Int): List<EditDto>

    @GET("${ApiConstants.USER_EDITS}/{editId}")
    suspend fun getEdit(@Path("editId") editId: Int): EditDto

    @POST(ApiConstants.USER_EDITS)
    suspend fun createEdit(@Body edit: EditDto): EditDto

    @PUT("${ApiConstants.USER_EDITS}/{editId}")
    suspend fun updateEdit(@Path("editId") editId: Int, @Body edit: EditDto): EditDto

    @PATCH("${ApiConstants.USER_EDITS}/{editId}/approve")
    suspend fun approveEdit(@Path("editId") editId: Int): EditActionResponse

    @PATCH("${ApiConstants.USER_EDITS}/{editId}/reject")
    suspend fun rejectEdit(
        @Path("editId") editId: Int,
        @Body body: EditRejectRequest = EditRejectRequest()
    ): EditActionResponse

    @DELETE("${ApiConstants.USER_EDITS}/{editId}")
    suspend fun deleteEdit(@Path("editId") editId: Int): Map<String, String>

    @POST(ApiConstants.REPORTS)
    suspend fun submitReport(@Body report: SongReportRequest): SongReportResponse

    @GET("${ApiConstants.USERS}/{userId}")
    suspend fun getUser(@Path("userId") userId: Int): UserDto

    @POST(ApiConstants.USERS)
    suspend fun createUser(@Body user: UserDto): UserDto

    @PUT("${ApiConstants.USERS}/{userId}")
    suspend fun updateUser(@Path("userId") userId: Int, @Body user: UserDto): UserDto

    @GET(ApiConstants.ORGANISATIONS)
    suspend fun getOrganisations(): List<OrganisationDto>

    @GET("${ApiConstants.ORGANISATIONS}/{orgId}")
    suspend fun getOrganisation(@Path("orgId") orgId: Int): OrganisationDto

    @GET(ApiConstants.LISTINGS)
    suspend fun getRemoteListings(): List<ListingDto>

    @POST(ApiConstants.LISTINGS)
    suspend fun createRemoteListing(@Body listing: ListingDto): ListingDto

    @PUT("${ApiConstants.LISTINGS}/{listingId}")
    suspend fun updateRemoteListing(
        @Path("listingId") listingId: Int,
        @Body listing: ListingDto
    ): ListingDto

    @POST(ApiConstants.LIKES_TOGGLE)
    suspend fun toggleLike(@Body body: LikeToggleRequest): LikeToggleResponse

    @GET("${ApiConstants.LIKES_USER}/{userId}")
    suspend fun getLikedSongs(@Path("userId") userId: Int): LikedSongsResponse
}
