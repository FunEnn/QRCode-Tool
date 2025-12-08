package com.chy.qmzy_202308190231

import android.app.Activity
import android.content.Intent

/**
 * 返回主页的统一方法
 * 清除返回栈，回到 MainActivity
 */
fun Activity.navigateToHome() {
    val intent = Intent(this, MainActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    startActivity(intent)
    finish()
}

/**
 * 简单返回上一页
 */
fun Activity.navigateBack() {
    finish()
}

/**
 * 导航到扫描页面
 * @param finishCurrent 是否关闭当前页面
 */
fun Activity.navigateToScan(finishCurrent: Boolean = true) {
    val intent = Intent(this, QRScanActivity::class.java)
    startActivity(intent)
    if (finishCurrent) finish()
}

/**
 * 导航到生成页面
 * @param finishCurrent 是否关闭当前页面
 */
fun Activity.navigateToGenerate(finishCurrent: Boolean = true) {
    val intent = Intent(this, QRGenerateActivity::class.java)
    startActivity(intent)
    if (finishCurrent) finish()
}
