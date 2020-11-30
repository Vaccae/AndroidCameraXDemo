package dem.vaccae.camerax

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    lateinit var viewFinder: PreviewView
    lateinit var vOverLay: ViewOverLay

    val strarray = mutableListOf("微", "卡", "智", "享")
    val ptrarray = mutableListOf(
        PointF(300f, 200f), PointF(300f, 400f), PointF(300f, 600f), PointF(300f, 800f)
    )
    //数组显示序号
    var idxarray = 0;

    //计算显示的帧数
    var count = 1;
    val TAG = "CameraX"

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "未开启权限.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private lateinit var cameraExecutor: ExecutorService
    var cameraProvider: ProcessCameraProvider? = null//相机信息
    var preview: Preview? = null//预览对象
    var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA//当前相机
    var camera: Camera? = null//相机对象
    private var imageCapture: ImageCapture? = null//拍照用例
    var videoCapture: VideoCapture? = null//录像用例
    var imageAnalyzer: ImageAnalysis? = null//图片分析

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewFinder = findViewById(R.id.viewFinder)
        vOverLay = findViewById(R.id.viewOverlay)
        vOverLay.bringToFront()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    private fun startCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()//获取相机信息

            //预览配置
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }
            //拍照用例配置
            imageCapture = ImageCapture.Builder().build()
            //图像分析接口
            imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also { it ->
                    it.setAnalyzer(cameraExecutor, { p->
                        count++;
                        //每20帧执行一次
                        if (count % 20 == 0) {
                            val idx = idxarray % 4
                            vOverLay.drawText(strarray[idx], ptrarray[idx])
                            idxarray++
                        }
                        //这里的ImageProxy如果不close，不会显示下一帧
                        p.close()
                    })
                }

            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA//使用后置摄像头
            videoCapture = VideoCapture.Builder()//录像用例配置
/*                .setTargetAspectRatio(AspectRatio.RATIO_16_9) //设置高宽比
                .setTargetRotation(viewFinder.display.rotation)//设置旋转角度
                .setAudioRecordSource(AudioSource.MIC)//设置音频源麦克风*/
                .build()

            try {
                cameraProvider?.unbindAll()//先解绑所有用例
                camera = cameraProvider?.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture,
                    imageAnalyzer
                )//绑定用例
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}