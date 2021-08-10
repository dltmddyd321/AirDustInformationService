package com.example.plugtest

import com.example.plugtest.airquality.AirQualityResponse
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
    //관측소 정보 API 인터페이스 정의

    @GET("B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty" +
        "?serviceKey=${BuildConfig.AIR_KOREA_SERVICE_KEY}" +
        "&returnType=json" +
        "&dataTerm=DAILY" +
        "&ver=1.3")
    suspend fun getRealtimeAirQualties(
        @Query("stationName") stationName: String
    ): retrofit2.Response<AirQualityResponse>
    //대기 오염 정보 API 인터페이스 정의
}