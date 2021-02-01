package com.example.batterylevel

import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import io.flutter.plugin.common.BasicMessageChannel
import io.flutter.plugin.common.StringCodec
import io.flutter.view.FlutterMain
import io.flutter.view.FlutterRunArguments
import io.flutter.view.FlutterView
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    private val CHANNEL = "samples.flutter.dev/battery"
    private var flutterView: FlutterView? = null
    private var messageChannel: BasicMessageChannel<String>? = null

    private fun getArgsFromIntent(intent: Intent): Array<String>? {
        // Before adding more entries to this list, consider that arbitrary
        // Android applications can generate intents with extra data and that
        // there are many security-sensitive args in the binary.
        val args = ArrayList<String>()
        if (intent.getBooleanExtra("trace-startup", false)) {
            args.add("--trace-startup")
        }
        if (intent.getBooleanExtra("start-paused", false)) {
            args.add("--start-paused")
        }
        if (intent.getBooleanExtra("enable-dart-profiling", false)) {
            args.add("--enable-dart-profiling")
        }
        if (!args.isEmpty()) {
            val argsArray = arrayOfNulls<String>(args.size)
            return args.toTypedArray<String>()
        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // handling arguments.
        val args = getArgsFromIntent(intent)
        FlutterMain.ensureInitializationComplete(applicationContext, args)

        setContentView(R.layout.flutter_view_layout)
        flutterView = findViewById(R.id.flutter_view)

        // preparing bundle to use for FlutterView we have added inside activity layout.
        val runArguments = FlutterRunArguments()
        runArguments.bundlePath = FlutterMain.findAppBundlePath(applicationContext)
        runArguments.entrypoint = "main"
        flutterView?.runFromBundle(runArguments)

        // establishing message channel which can be used for the data passing from one platform to another.
        flutterView?.let { messageChannel = BasicMessageChannel(it, CHANNEL, StringCodec.INSTANCE) }


        val batteryLevel = getBatteryLevel()

        if (batteryLevel != -1) {
            sendAndroidIncrement(batteryLevel.toString())
        } else {
            sendAndroidIncrement("Battery level not available.")
        }
    }

    /**
     * this function is used to send message on Message Channel with appropriate delay.
     */
    private fun sendAndroidIncrement(message: String) {
        Handler().postDelayed({ messageChannel?.send(message) }, 500)
    }

/*
        override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            // Note: this method is invoked on the main thread.
            call, result ->
            if (call.method == "getBatteryLevel") {
                val batteryLevel = getBatteryLevel()

                if (batteryLevel != -1) {
                    result.success(batteryLevel)
                } else {
                    result.error("UNAVAILABLE", "Battery level not available.", null)
                }
            } else {
                result.notImplemented()
            }
        }
    }
*/

    override fun onPostResume() {
        super.onPostResume()
        flutterView?.onPostResume()
    }

    private fun getBatteryLevel(): Int {
        val batteryLevel: Int
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } else {
            val intent = ContextWrapper(applicationContext).registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            batteryLevel = intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        }

        return batteryLevel
    }

    override fun onPause() {
        super.onPause()
        flutterView?.onPause()
    }

    override fun onDestroy() {
        flutterView?.destroy()
        super.onDestroy()
    }
}