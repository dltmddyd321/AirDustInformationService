package com.example.plugtest

import com.example.plugtest.monitoringstation.MonitoringStationsResponse
import com.example.plugtest.monitoringstation.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AirKoreaApiService {

    @GET("B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList" +
        "?serviceKey=${BuildConfig.AIR_KOREA_SERVICE_KEY}" +
        "&returnType=json")
    suspend fun getNearbyMonitoringStation(
        @Query("tmX") tmX: Double,
        @Query("tmY") tmY: Double
    ) : retrofit2.Response<MonitoringStationsResponse>
}