package com.example.plugtest

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.example.plugtest.data.Repository
import com.example.plugtest.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

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
                val monitoringStation =
                    Repository.getNearbyMonitoringState(location.latitude,location.longitude)

                binding.textView.text = monitoringStation?.stationName
            }
        }
    }

    companion object {
        private const val REQUEST_ACCESS_LOCATION_PERMISSIONS = 100
    }
}