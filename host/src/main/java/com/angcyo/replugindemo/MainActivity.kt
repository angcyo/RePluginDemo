package com.angcyo.replugindemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.angcyo.aidl.Host
import com.angcyo.aidl.Plugin
import com.qihoo360.replugin.RePlugin
import com.qihoo360.replugin.base.IPC

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), Runnable {
    override fun run() {
        //每隔5秒, 发送一次广播

        IPC.sendLocalBroadcast2All(this, Intent().apply {
            setAction("com.angcyo.host.action")
            putExtra("data", "{\"key\":\"value\"} 宿主:${System.currentTimeMillis()}")
        })

        val plugin1 = Plugin.Stub.asInterface(RePlugin.fetchBinder("com.angcyo.plugin1", "plugin"))
        val plugin2 = Plugin.Stub.asInterface(RePlugin.fetchBinder("com.angcyo.plugin2", "plugin"))

        text_view.text = StringBuilder("发送广播:\n").apply {
            appendln(System.currentTimeMillis())
            appendln(if (plugin1 == null) "plugin1 is null" else plugin1.fetchData("获取插件1的数据."))
            appendln(if (plugin2 == null) "plugin2 is null" else plugin2.fetchData("获取插件2的数据."))
        }

        window.decorView.postDelayed(this, 5_000)
    }

    lateinit var threadPool: ThreadPoolExecutor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        threadPool = ThreadPoolExecutor(
            0, 10,
            60L, TimeUnit.SECONDS, LinkedBlockingQueue()
        )

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        plugin_list_view.setOnClickListener {
            updatePluginList()
        }


        RePlugin.registerGlobalBinder("host", object : Host.Stub() {
            override fun fetchData(parma: String?): String {
                return "宿主收到数据:$parma ${System.currentTimeMillis()})"
            }

            override fun sendData(data: String?): String {
                return ""
            }
        })

        window.decorView.postDelayed(this, 5_000)

        LocalBroadcastManager.getInstance(this).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                text_view2.text = StringBuilder("收到广播:\n").apply {
                    appendln(intent?.getStringExtra("data") ?: "data is null")
                }
            }

        }, IntentFilter("com.angcyo.plugin.action"))

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)

        updatePluginList()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
    }

    fun updatePluginList() {
        val builder = StringBuilder("插件列表:\n")

        RePlugin.getPluginInfoList().forEach { pluginInfo ->
            builder.append("插件名:").append(pluginInfo.name).appendln()
            builder.append("插件包名:").append(pluginInfo.packageName).appendln()
            builder.append("插件版本:").append(pluginInfo.version).appendln()
            builder.append("I:").append(RePlugin.isPluginInstalled(pluginInfo.name)).append(" R:")
                .append(RePlugin.isPluginRunning(pluginInfo.name)).appendln().appendln()
        }

        plugin_list_view.text = builder
    }

    fun installPlugin(path: String) {
        threadPool.execute {
            val pluginInfo = RePlugin.install(path)
            if (pluginInfo == null) {
                runOnUiThread {
                    Snackbar.make(plugin_list_view, "插件:${path}安装失败.", Snackbar.LENGTH_LONG).show()
                }
            } else {
                runOnUiThread {
                    Snackbar.make(plugin_list_view, "正在安装插件:${pluginInfo.name}.", Snackbar.LENGTH_LONG).show()
                }

                RePlugin.preload(pluginInfo)

                runOnUiThread {
                    Snackbar.make(plugin_list_view, "插件:${pluginInfo.name}安装成功.", Snackbar.LENGTH_LONG).show()

                    updatePluginList()
                }
            }
        }
    }

    fun uninstallPlugin(plugin: String) {
        threadPool.execute {
            val result = RePlugin.uninstall(plugin)
            if (result) {
                runOnUiThread {
                    Snackbar.make(plugin_list_view, "插件:${plugin}卸载成功.", Snackbar.LENGTH_LONG).show()
                    updatePluginList()
                }
            } else {
                runOnUiThread {
                    Snackbar.make(plugin_list_view, "插件:${plugin}卸载失败.", Snackbar.LENGTH_LONG).show()

                }
            }
        }
    }

    fun installPlugin1(view: View) {
        installPlugin("/sdcard/plugin/plugin1.apk")
    }

    fun installPlugin2(view: View) {
        installPlugin("/sdcard/plugin/plugin2.apk")
    }

    fun uninstallPlugin1(view: View) {
        uninstallPlugin("com.angcyo.plugin1")
    }

    fun uninstallPlugin2(view: View) {
        uninstallPlugin("com.angcyo.plugin2")
    }

    fun startPlugin1(view: View) {
        threadPool.execute {
            if (!RePlugin.startActivity(this, Intent(), "com.angcyo.plugin1", "com.angcyo.plugin1.MainActivity")) {
                runOnUiThread {
                    Snackbar.make(plugin_list_view, "启动1失败.", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    fun startPlugin2(view: View) {
        threadPool.execute {
            if (!RePlugin.startActivity(this, Intent(), "com.angcyo.plugin2", "com.angcyo.plugin2.MainActivity")) {
                runOnUiThread {
                    Snackbar.make(plugin_list_view, "启动2失败.", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    fun startPlugin12(view: View) {
        startPlugin1(view)
        startPlugin2(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        threadPool.shutdown()
    }
}
