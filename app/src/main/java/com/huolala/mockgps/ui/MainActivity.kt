package com.huolala.mockgps.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.baidu.mapapi.model.LatLng
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import com.castiel.common.base.BaseActivity
import com.huolala.mockgps.R
import com.huolala.mockgps.databinding.ActivityMainBinding
import com.huolala.mockgps.model.MockMessageModel
import com.huolala.mockgps.model.NaviType
import com.huolala.mockgps.model.PoiInfoModel
import com.huolala.mockgps.utils.Utils
import com.huolala.mockgps.utils.WarnDialogUtils
import com.huolala.mockgps.viewmodel.HomeViewModel
import com.huolala.mockgps.widget.MapSelectDialog
import java.util.*
import kotlin.math.*
import kotlin.random.Random

/**
 * @author jiayu.liu
 */
class MainActivity : BaseActivity<ActivityMainBinding, HomeViewModel>() {
    private var topMarginOffset: Int = 0
    private var topMargin: Int = 0
    private var mMapSelectDialog: MapSelectDialog? = null
    private var locationAlwaysView: View? = null

    override fun initView() {
        topMarginOffset = -ConvertUtils.dp2px(50f)
        topMargin = ConvertUtils.dp2px(15f)
        dataBinding.btnStartNavi.setOnClickListener {
            val lat = dataBinding.latInput.text.toString()
            val lng = dataBinding.lngInput.text.toString()
            if(lat.isEmpty() || lng.isEmpty())
            {
                Toast.makeText(this@MainActivity, "模拟位置不能为null", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            startMotion(lat.toDouble(),lng.toDouble())
        }

        dataBinding.btnStartNavi0.setOnClickListener {
            val latlng = dataBinding.latlngInput.text.toString()
            if(latlng.isEmpty())
            {
                Toast.makeText(this@MainActivity, "模拟位置不能为null", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val latlngDatas = latlng.split(",")
            if(latlngDatas.size != 2)
            {
                Toast.makeText(this@MainActivity, "格式不正确", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            startMotion(latlngDatas[0].toDouble(),latlngDatas[1].toDouble())
        }

        dataBinding.btnStartNavi1.setOnClickListener {
            val lnglat = dataBinding.lnglatInput.text.toString()
            if(lnglat.isEmpty())
            {
                Toast.makeText(this@MainActivity, "模拟位置不能为null", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val lnglatDatas = lnglat.split(",")
            if(lnglatDatas.size != 2)
            {
                Toast.makeText(this@MainActivity, "格式不正确", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            startMotion(lnglatDatas[1].toDouble(),lnglatDatas[0].toDouble())
        }

        dataBinding.chengkunButton.setOnClickListener {
            val baseLat = 39.12793123
            val baseLng = 117.28209674

            val randomPoint = getRandomNearbyPoint(baseLat, baseLng)
            startMotion(randomPoint.first,randomPoint.second)
        }

        dataBinding.wandongButton.setOnClickListener {
            val baseLat = 39.13634783
            val baseLng = 117.26727190

            val randomPoint = getRandomNearbyPoint(baseLat, baseLng)
            startMotion(randomPoint.first,randomPoint.second)
        }

        dataBinding.qinjianButton.setOnClickListener {
            val baseLat = 39.18157062
            val baseLng = 117.15031129

            val randomPoint = getRandomNearbyPoint(baseLat, baseLng)
            startMotion(randomPoint.first,randomPoint.second)
        }

        dataBinding.baodiButton.setOnClickListener {
            val baseLat = 39.55431882
            val baseLng = 117.39548797

            val randomPoint = getRandomNearbyPoint(baseLat, baseLng)
            startMotion(randomPoint.first,randomPoint.second)
        }

        dataBinding.beichenButton.setOnClickListener {
            val baseLat = 39.22705531
            val baseLng = 117.25168738

            val randomPoint = getRandomNearbyPoint(baseLat, baseLng)
            startMotion(randomPoint.first,randomPoint.second)
        }

        dataBinding.jiaoguanButton.setOnClickListener {
            val baseLat = 39.12022706
            val baseLng = 117.2086891
            val randomPoint = getRandomNearbyPoint(baseLat, baseLng)
            startMotion(randomPoint.first,randomPoint.second)
        }

        dataBinding.openWeb.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://jingweidu.bmcx.com/")
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "未安装浏览器应用", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val EARTH_RADIUS = 6371000.0 // 地球半径(米)
    private val MAX_DISTANCE = 80.0 // 最大距离80米

    // 生成随机距离（0-80米）和随机角度（0-360度）
    private fun getRandomPolarCoords(): Pair<Double, Double> {
        val distance = Random.nextDouble(MAX_DISTANCE)
        val angle = Random.nextDouble(360.0)
        return distance to angle
    }

    // 计算随机偏移后的坐标
    fun getRandomNearbyPoint(lat: Double, lng: Double): Pair<Double, Double> {
        val (distance, angle) = getRandomPolarCoords()
        val radAngle = angle * PI / 180

        // 纬度偏移（南北方向）
        val latOffset = distance * cos(radAngle) / EARTH_RADIUS * 180 / PI
        // 经度偏移（东西方向）
        val lngOffset = distance * sin(radAngle) / (EARTH_RADIUS * cos(lat * PI / 180)) * 180 / PI

        return Pair(lat + latOffset, lng + lngOffset)
    }

    private fun startMotion(lat: Double,lng:Double)
    {
        //启动模拟导航
        Utils.checkFloatWindow(this).let {
            if (!it) {
                WarnDialogUtils.setFloatWindowDialog(this@MainActivity)
                return
            }
            val latLng = LatLng(lat,lng)
            val locationModel = PoiInfoModel(latLng,UUID.randomUUID().toString())
            val model = MockMessageModel(
                locationModel = locationModel,
                naviType = NaviType.LOCATION,
                uid = locationModel?.uid ?: ""
            )
            val intent = Intent(this, MockLocationActivity::class.java)
            intent.putExtra("model", model)
            startActivity(intent)
        }
    }

    override fun initViewModel(): Class<HomeViewModel> {
        return HomeViewModel::class.java
    }

    override fun getLayout(): Int {
        return R.layout.activity_main
    }

    override fun initData() {
    }

    override fun initObserver() {
        viewModel.updateApp.observe(this) {
            dataBinding.isUpdate = true
        }
    }

    override fun onResume() {
        super.onResume()
        mMapSelectDialog?.onResume()
        //后台定位提示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!PermissionUtils.isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                if (locationAlwaysView == null) {
                    locationAlwaysView = LayoutInflater.from(this)
                        .inflate(R.layout.layout_location_always_allow, null)
                    locationAlwaysView?.let {
                        it.findViewById<View>(R.id.btn_skip)?.setOnClickListener {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            intent.data = Uri.parse("package:$packageName")
                            try {
                                startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                ToastUtils.showShort("跳转失败，请手动开启！")
                            }
                        }

                        val frameLayout = this.window.decorView as FrameLayout
                        frameLayout.addView(
                            it,
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                gravity = Gravity.BOTTOM
                                val margin = ConvertUtils.dp2px(10f)
                                setMargins(margin, 0, margin, margin * 2)
                            }
                        )
                    }
                }
            } else {
                locationAlwaysView?.let {
                    (window.decorView as FrameLayout).removeView(it)
                    locationAlwaysView = null
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mMapSelectDialog?.onPause()
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addCategory(Intent.CATEGORY_HOME)
            startActivity(intent)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}