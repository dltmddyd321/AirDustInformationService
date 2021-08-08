package com.example.plugtest.data

import com.example.plugtest.AirKoreaApiService
import com.example.plugtest.BuildConfig
import com.example.plugtest.KakaoLocalAPiService
import com.example.plugtest.monitoringstation.MonitoringStation
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object Repository {

    suspend fun getNearbyMonitoringState(latitude: Double, longitude: Double): MonitoringStation? {
        val tmCoordinates = kakaoLocalAPiService
            .getTmCoordinates(longitude,latitude)
            .body()
            ?.documents
            ?.firstOrNull()
        /*위도와 경도에 대한 API data set을 호출,
        값이 있다면 첫 번째 값을 반환, 없다면 Null 반환*/

        val tmX = tmCoordinates?.x
        val tmY = tmCoordinates?.y
        //존재하는 tmX, tmY 값이 반환

        return airKoreaApiService
            .getNearbyMonitoringStation(tmX!!,tmY!!)
            .body() //API data 응답이 들어온다면
            ?.response
            ?.body //들어온 API의 body 요소
            ?.monitoringStations
            ?.minByOrNull { it.tm ?: Double.MAX_VALUE //Null 값은 자동으로 후순위로 배치
             } //선택한 값들을 비교하여 최소 값을 반환
    }

    private val kakaoLocalAPiService: KakaoLocalAPiService by lazy {
        Retrofit.Builder()
            .baseUrl(Url.KAKAO_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildHttpClient()) //로깅 목적으로 공통의 클라이언트를 생성
            .build()
            .create()
    } //Kakao API 사용을 위한 Host 주소를 Retrofit을 통해 등록

    private val airKoreaApiService: AirKoreaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Url.AIR_KOREA_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(buildHttpClient()) //로깅 목적으로 공통의 클라이언트를 생성
            .build()
            .create()
    } //에어코리아 관측소 정보 API 사용을 위한 Host 주소를 Retrofit을 통해 등록

    private fun buildHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor (
                HttpLoggingInterceptor().apply {
                    level = if(BuildConfig.DEBUG) { //level을 통해 보이는 데이터의 한도를 지정 가능
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()
    //level 설정을 완료 -> 클라이언트 업데이트
}