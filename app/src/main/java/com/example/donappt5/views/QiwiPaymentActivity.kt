package com.example.donappt5.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.donappt5.R
import com.example.donappt5.databinding.ActivityQiwiPaymentBinding


class QiwiPaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQiwiPaymentBinding

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var link = intent.getStringExtra("qiwiPaymentUrl")
//        link = "https://my.qiwi.com/Artem-PjJByE6F2s"
        binding = ActivityQiwiPaymentBinding.inflate(layoutInflater)
        setupView()
        if (link != null) {
            webView.loadUrl(link)
        } else {
            Toast.makeText(this, resources.getString(R.string.no_payment_credentials_message), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupView() {
        val view = binding.root
        setContentView(view)
        webView = binding.qiwiPaymentWebview
        val webSettings: WebSettings = webView.getSettings()
        webSettings.javaScriptEnabled = true
        webSettings.setDomStorageEnabled(true)
        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if (view != null && request != null) {
                    view.loadUrl(request.url.toString())
                }
                return false
            }
        })
    }
}
