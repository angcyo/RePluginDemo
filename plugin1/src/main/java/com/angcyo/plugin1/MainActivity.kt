package com.angcyo.plugin1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.Snackbar
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

class MainActivity : AppCompatActivity(), Runnable {

    override fun run() {
        //每隔5秒, 发送一次广播

        IPC.sendLocalBroadcast2All(this, Intent().apply {
            setAction("com.angcyo.plugin.action")
            putExtra("data", "{\"key\":\"value\"} 插件1:${System.currentTimeMillis()}")
        })

        val plugin2 = Plugin.Stub.asInterface(RePlugin.fetchBinder("com.angcyo.plugin2", "plugin"))

        text_view.text = StringBuilder("发送广播:\n").apply {
            appendln(System.currentTimeMillis())
            appendln(if (plugin2 == null) "plugin2 is null" else plugin2.fetchData("获取插件2的数据."))
        }

        window.decorView.postDelayed(this, 5_000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        text_view.text = StringBuilder("插件1:\n").apply {
            appendln(BuildConfig.APPLICATION_ID)
            appendln(this@MainActivity.packageName)
            appendln(RePlugin.getHostContext()?.packageName ?: "null")
            appendln(RePlugin.getPluginContext()?.packageName ?: "null")
        }

        RePlugin.registerPluginBinder("plugin", object : Plugin.Stub() {
            override fun fetchData(parma: String?): String {
                return "插件1收到:$parma"
            }

            override fun sendData(data: String?): String {
                return ""
            }

        })

        LocalBroadcastManager.getInstance(this).registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                text_view3.text = StringBuilder("收到广播:\n").apply {
                    appendln(intent?.getStringExtra("data") ?: "data is null")
                }
            }

        }, IntentFilter("com.angcyo.host.action"))

        window.decorView.postDelayed(this, 5_000)
    }

    fun onButton1Click(view: View) {
        val host = Host.Stub.asInterface(RePlugin.getGlobalBinder("host"))

        if (host == null) {
            text_view1.text = "host is null."
        } else {
            text_view1.text = host.fetchData("插件1, 请求获取宿主数据.")
        }
    }

    fun onButton2Click(view: View) {
        val plugin = Plugin.Stub.asInterface(RePlugin.fetchBinder("com.angcyo.plugin2", "plugin"))

        if (plugin == null) {
            text_view2.text = "plugin2 is null."
        } else {
            text_view2.text = plugin.fetchData("插件1, 请求获取插件2数据.")
        }
    }
}
