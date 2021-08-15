package com.example.plugtest

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import com.example.plugtest.airquality.Grade
import com.example.plugtest.airquality.MeasuredValue
import com.example.plugtest.data.Repository
import com.example.plugtest.databinding.ActivityMainBinding
import com.example.plugtest.monitoringstation.MonitoringStation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null
    //Task 이전에는 사용되지 않으므로 Nullable 선언

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bindViews()
        initVariables()
        requestLocationPermissions()
    }

    override fun onDestroy() {
        //서비스를 이탈하거나 종료할 시 토큰 호출을 취소하기 위한 함수
        super.onDestroy()
        cancellationTokenSource?.cancel()
        scope.cancel()
    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult( //권한 사용 허가에 대한 결과 로직
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val locationPermissionGranted =
            requestCode == REQUEST_ACCESS_LOCATION_PERMISSIONS && grantResults[0] == PackageManager.PERMISSION_GRANTED
        // 위치 권한 사용에 대한 허가값을 코드 값 100과 패키지 매니저의 PERMISSION_GRANTED으로 가져오기

        if(!locationPermissionGranted) {
            finish()
            //위치 정보 권한이 허가되지 않았다면 종료
        } else {
            fetchAirDustData()
        }
    }

    private fun bindViews() {
        binding.refresh.setOnRefreshListener {
            fetchAirDustData()
        }
    }

    private fun initVariables() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        //통합 위치 정보 제공자 클라이언트의 인스턴스 생성
    }

    private fun requestLocationPermissions() { //권한 요청 함수
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
                //위치 정보를 받아오기 위한 Location Manifest 설정 추가
            ),
            REQUEST_ACCESS_LOCATION_PERMISSIONS
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchAirDustData() {
        cancellationTokenSource = CancellationTokenSource()

        fusedLocationProviderClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource!!.token
            //현재 위치 값 데이터를 가지고 있는 토큰을 호출
        ).addOnSuccessListener { location -> //API 호출에 성공한 이후
            scope.launch { //scope 실행 시 관측소에 대한 위도, 경도 값 받아오기
                binding.errorDescriptionText.visibility = View.GONE
                try {
                    val monitoringStation =
                        Repository.getNearbyMonitoringState(location.latitude,location.longitude)

                    val measuredValue =
                        Repository.getLatestAirQualityData(monitoringStation!!.stationName!!)

                    displayAirQualityData(monitoringStation,measuredValue!!)
                }catch (exception: Exception) {
                    binding.errorDescriptionText.visibility = View.VISIBLE
                    //오류 발생 시 에러 메시지 팝업

                    binding.informationLayout.alpha = 0F
                    //오류 발생 시 Main 화면 안 보이도록 설정
                }finally {
                    binding.progressBar.visibility = View.GONE
                    binding.refresh.isRefreshing = false
                    //오류 발생과 상관없이 progressBar는 안 보이게, refresh는 취소
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun displayAirQualityData(monitoringStation: MonitoringStation, measuredValue: MeasuredValue) {
        binding.informationLayout.animate()
            .alpha(1F)
            .start()
        //Main 화면 Fade-In

        binding.measuringStationName.text = monitoringStation.stationName
        binding.measuringStationAddressTextView.text = monitoringStation.addr

        (measuredValue.khaiGrade ?: Grade.UNKNOWN).let { grade ->
            binding.root.setBackgroundResource(grade.colorResId)
            binding.totalGradeLabelTextView.text = grade.label
            binding.totalGradeEmojiView.text = grade.emoji
        }

        with(measuredValue) {
            binding.fineDustInformation.text =
                "미세먼지 : ${pm10Value} ㎍/㎥ ${pm10Grade ?: Grade.UNKNOWN.emoji}"
            //API로 가져온 미세먼지 데이터를 실제 Main 화면에 바인딩

            binding.ultraFineDustInformation.text =
                "초미세먼지 : ${pm25Value} ㎍/㎥ ${pm25Grade ?: Grade.UNKNOWN.emoji}"
            //API로 가져온 초미세먼지 데이터를 실제 Main 화면에 바인딩

            with(binding.so2Item) {
                labelTextView.text = "아황산가스"
                gradeTextView.text = (so2Grade ?: Grade.UNKNOWN).toString()
                valueTextView.text = "$so2Value ppm"
            }

            with(binding.co2Item) {
                labelTextView.text = "일산화탄소"
                gradeTextView.text = (coGrade ?: Grade.UNKNOWN).toString()
                valueTextView.text = coValue
            }

            with(binding.o3Item) {
                labelTextView.text = "오존"
                gradeTextView.text = (o3Grade ?: Grade.UNKNOWN).toString()
                valueTextView.text = o3Value
            }

            with(binding.no2Item) {
                labelTextView.text = "이산화질소"
                gradeTextView.text = (no2Grade ?: Grade.UNKNOWN).toString()
                valueTextView.text = no2Value
            }
        }
    }

    companion object {
        private const val REQUEST_ACCESS_LOCATION_PERMISSIONS = 100
    }
}