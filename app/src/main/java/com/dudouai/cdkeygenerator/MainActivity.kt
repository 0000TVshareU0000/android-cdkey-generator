package com.dudouai.cdkeygenerator

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var spType: Spinner
    private lateinit var etMaxAct: EditText
    private lateinit var etCount: EditText
    private lateinit var btnGenerate: Button
    private lateinit var btnCopy: Button
    private lateinit var btnSave: Button
    private lateinit var tvResult: TextView
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        setupSpinner()
        setupButtons()
    }

    private fun initViews() {
        spType = findViewById(R.id.spType)
        etMaxAct = findViewById(R.id.etMaxAct)
        etCount = findViewById(R.id.etCount)
        btnGenerate = findViewById(R.id.btnGenerate)
        btnCopy = findViewById(R.id.btnCopy)
        btnSave = findViewById(R.id.btnSave)
        tvResult = findViewById(R.id.tvResult)
        tvStatus = findViewById(R.id.tvStatus)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.license_types,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spType.adapter = adapter
    }

    private fun setupButtons() {
        btnGenerate.setOnClickListener {
            tvStatus.text = "生成中..."
            // TODO: 实现 CDKey 生成逻辑
            tvResult.text = "DDAI-TEST-TEST-TEST-TEST"
            tvStatus.text = "生成完成"
        }

        btnCopy.setOnClickListener {
            val text = tvResult.text.toString()
            if (text.isNotEmpty()) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("CDKey", text)
                clipboard.setPrimaryClip(clip)
                tvStatus.text = "已复制到剪贴板"
            }
        }

        btnSave.setOnClickListener {
            tvStatus.text = "保存功能开发中..."
        }
    }
}
