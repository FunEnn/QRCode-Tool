package com.chy.qmzy_202308190231

import android.app.Activity
import android.content.Intent

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
