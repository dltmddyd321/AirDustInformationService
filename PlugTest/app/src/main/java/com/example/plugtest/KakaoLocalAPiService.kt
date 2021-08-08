package com.example.plugtest

import com.example.plugtest.tmcoordinates.TmCoodinatesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface KakaoLocalAPiService {

    //API 및 헤더 값 등록 후 요구되는 파라미터 설정
    @Headers("Authorization: KakaoAK ${BuildConfig.KAKAO_API_KEY}")
    @GET("v2/local/geo/transcoord.json?output_coord=TM")
    suspend fun getTmCoordinates(
        @Query("x") longitude: Double,
        @Query("y") latitude: Double
    ) : Response<TmCoodinatesResponse> //JSON Response 반환

}