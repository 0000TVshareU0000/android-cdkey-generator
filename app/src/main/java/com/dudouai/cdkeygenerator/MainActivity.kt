package com.dudouai.cdkeygenerator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class MainActivity : AppCompatActivity() {

    private val LICENSE_SECRET = "DudouAI_License_Secret_Key_2026_V1"

    private val TYPE_MAP = mapOf(
        "month" to Pair(1, 30),
        "year" to Pair(2, 365),
        "lifetime" to Pair(3, 65535)
    )

    private lateinit var spType: Spinner
    private lateinit var etMaxAct: EditText
    private lateinit var etCount: EditText
    private lateinit var btnGenerate: Button
    private lateinit var btnCopy: Button
    private lateinit var btnSave: Button
    private lateinit var tvResult: TextView
    private lateinit var tvStatus: TextView

    private var generatedKeys = mutableListOf<String>()

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

        etMaxAct.setText("2")
        etCount.setText("1")
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("月卡（30天）", "年卡（365天）", "终生授权"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spType.adapter = adapter
    }

    private fun setupButtons() {
        btnGenerate.setOnClickListener {
            generateCDKeys()
        }

        btnCopy.setOnClickListener {
            copyToClipboard()
        }

        btnSave.setOnClickListener {
            saveToFile()
        }
    }

    // ── CDKey 生成核心逻辑 ──────────────────────────────────

    private fun deriveKey(secret: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(secret.toByteArray(Charsets.UTF_8))
    }

    private fun generateCDKey(type: String, maxActivations: Int): String {
        val typeInfo = TYPE_MAP[type] ?: TYPE_MAP["month"]!!
        val typeId = typeInfo.first
        val days = typeInfo.second

        // payload: [type:1][days:2][timestamp:4][nonce:1][maxActivations:1] = 9 bytes
        val payload = ByteArray(9)
        payload[0] = typeId.toByte()

        // days: 2 bytes big-endian
        val daysBytes = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(days).array()
        payload[1] = daysBytes[2]
        payload[2] = daysBytes[3]

        // timestamp: 4 bytes big-endian (seconds)
        val now = System.currentTimeMillis() / 1000
        val timeBytes = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN).putLong(now).array()
        payload[3] = timeBytes[4]
        payload[4] = timeBytes[5]
        payload[5] = timeBytes[6]
        payload[6] = timeBytes[7]

        // nonce: 1 byte random
        val random = SecureRandom()
        payload[7] = random.nextInt(256).toByte()

        // maxActivations: 1 byte (0=unlimited, 255=unlimited flag)
        payload[8] = if (maxActivations <= 0) 0.toByte() else maxActivations.coerceAtMost(255).toByte()

        // HMAC-SHA256
        val key = deriveKey(LICENSE_SECRET)
        val hmac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key, "HmacSHA256")
        hmac.init(secretKey)
        val signature = hmac.doFinal(payload).copyOfRange(0, 8)

        // Combine: 9 bytes payload + 8 bytes signature = 17 bytes
        val cdkeyData = ByteArray(17)
        System.arraycopy(payload, 0, cdkeyData, 0, 9)
        System.arraycopy(signature, 0, cdkeyData, 9, 8)

        // Convert to hex and format
        val hex = cdkeyData.joinToString("") { "%02X".format(it) }
        val formatted = "DDAI-" + hex.chunked(4).joinToString("-")

        return formatted
    }

    private fun generateCDKeys() {
        val typePos = spType.selectedItemPosition
        val type = when (typePos) {
            0 -> "month"
            1 -> "year"
            2 -> "lifetime"
            else -> "month"
        }

        val typeName = when (typePos) {
            0 -> "月卡"
            1 -> "年卡"
            2 -> "终生"
            else -> "月卡"
        }

        val maxActStr = etMaxAct.text.toString()
        val maxAct = if (maxActStr.isEmpty()) 2 else maxActStr.toIntOrNull() ?: 2

        val countStr = etCount.text.toString()
        val count = if (countStr.isEmpty()) 1 else countStr.toIntOrNull()?.coerceIn(1, 100) ?: 1

        generatedKeys.clear()
        for (i in 0 until count) {
            val key = generateCDKey(type, maxAct)
            generatedKeys.add(key)
        }

        // Display results
        val resultText = generatedKeys.joinToString("\n")
        tvResult.text = resultText

        val maxDisplay = if (maxAct <= 0) "无限制" else maxAct.toString()
        tvStatus.text = "✅ 成功生成 ${generatedKeys.size} 个 | $typeName | 最大激活数: $maxDisplay"

        // Auto-copy first key
        if (generatedKeys.isNotEmpty()) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("CDKey", generatedKeys[0])
            clipboard.setPrimaryClip(clip)
            tvStatus.text = tvStatus.text.toString() + " | 首条已复制"
        }
    }

    private fun copyToClipboard() {
        if (generatedKeys.isEmpty()) {
            Toast.makeText(this, "暂无生成结果", Toast.LENGTH_SHORT).show()
            return
        }
        val text = generatedKeys.joinToString("\n")
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("CDKey", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "✅ 全部 CDKey 已复制", Toast.LENGTH_SHORT).show()
        tvStatus.text = "✅ 全部 CDKey 已复制到剪贴板"
    }

    private fun saveToFile() {
        if (generatedKeys.isEmpty()) {
            Toast.makeText(this, "暂无生成结果", Toast.LENGTH_SHORT).show()
            return
        }

        val now = Calendar.getInstance().time
        val formatter = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
        val defaultName = "cdkey_" + formatter.format(now) + ".txt"

        val content = buildString {
            appendLine("====== DUDOU AI · CDKey 生成记录 ======")
            appendLine("生成时间: " + java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(now))
            appendLine("=".repeat(40))
            appendLine()
            generatedKeys.forEach { appendLine(it) }
        }

        try {
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, defaultName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Documents/CDKey")
            }

            val uri = contentResolver.insert(android.provider.MediaStore.Files.getContentUri("external"), values)
            if (uri != null) {
                contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(content.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(this, "✅ 已保存到 Documents/CDKey/$defaultName", Toast.LENGTH_LONG).show()
                tvStatus.text = "✅ 已保存: $defaultName"
            } else {
                // Fallback: save to app-specific directory
                val file = java.io.File(filesDir, defaultName)
                file.writeText(content, Charsets.UTF_8)
                Toast.makeText(this, "✅ 已保存到应用目录: $defaultName", Toast.LENGTH_LONG).show()
                tvStatus.text = "✅ 已保存: $defaultName"
            }
        } catch (e: Exception) {
            Toast.makeText(this, "保存失败: " + e.message, Toast.LENGTH_SHORT).show()
        }
    }
}
