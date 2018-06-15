/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package bar.foo.julian.dummy

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.ToggleButton

private const val WHAT_PROGRESS = 123
private const val PROGRESS_MAX = 100
private const val PROGRESS_STEP = 40
private const val PROGRESS_TIME: Long = 300

class MainActivity : AppCompatActivity() {

    private var mFinalProgress = 50

    private lateinit var mBtn0: Button
    private lateinit var mProgress0: ProgressBar
    private lateinit var mProgress1: ProgressBar

    private val mHandler0 = FirstHandler(Looper.getMainLooper())
    private val mHandler1 = SecondHandler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mBtn0 = findViewById<View>(R.id.btn_0) as Button
        mProgress0 = findViewById<View>(R.id.progress0) as ProgressBar
        mProgress1 = findViewById<View>(R.id.progress1) as ProgressBar
        setSpinners()

        mBtn0.setOnClickListener { onReloadClicked() }

        buildCheckHandler(mProgress0).let { handler ->
            val toggle = (findViewById<View>(R.id.progress0_toggle) as ToggleButton)
            toggle.setOnCheckedChangeListener(handler)
        }
    }

    public override fun onStart() {
        super.onStart()
        updateProgress(50)
    }

    private fun updateProgress(progress: Int) {
        animateProgress(mProgress0, progress)
    }

    private fun animateProgress(bar: ProgressBar, progress: Int) {
        bar.progress = progress
    }

    private fun sendMsg(progress: Int, delay: Long = PROGRESS_TIME) {
        mHandler0.removeMessages(WHAT_PROGRESS)
        mHandler0.obtainMessage(WHAT_PROGRESS)
                .also { it.arg1 = progress }
                .let { msg -> mHandler0.sendMessageDelayed(msg, delay) }
    }

    private fun setSpinners() {
        (findViewById<View>(R.id.menu_item_spinner) as Spinner).onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                mFinalProgress = resources.getStringArray(R.array.menu_item_nums)
                        .let { numbers -> Integer.parseInt(numbers[i]) }
                mFinalProgress = Math.min(mFinalProgress, 100)
                mFinalProgress = Math.max(mFinalProgress, 0)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }

        (findViewById<View>(R.id.progress0_height) as Spinner).onItemSelectedListener = buildSpinnerListener(mProgress0)
    }

    private fun buildSpinnerListener(progressView: View): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val heightInDp = resources.getStringArray(R.array.progressbar_height)
                        .let { heightArray -> Integer.parseInt(heightArray[i]) }
                val params = progressView.layoutParams
                params.height = dp2px(heightInDp.toFloat())
                progressView.layoutParams = RelativeLayout.LayoutParams(params.width, dp2px(heightInDp.toFloat()))
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
    }

    private fun buildCheckHandler(progressView: View): CompoundButton.OnCheckedChangeListener {
        return CompoundButton.OnCheckedChangeListener { _, b ->
            progressView.visibility = if (b) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun onReloadClicked() {
        mBtn0.isEnabled = false
        sendMsg(0)
        mHandler1.start()
    }

    private fun dp2px(dp: Float): Int {
        val metrics = resources.displayMetrics
        val px = dp * metrics.density
        return px.toInt()
    }

    private fun delayThenUiRun(timeInMs: Long, delayedTask: () -> Unit) {
        // delay 1 second then reset progress to final
        Thread(Runnable {
            try {
                Thread.sleep(timeInMs)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            runOnUiThread(delayedTask)
        }).start()
    }

    // To simulate page loading progress from 0, 20, 40....100.
    internal inner class FirstHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            if (msg.what == WHAT_PROGRESS) {
                val now = if (msg.arg1 > PROGRESS_MAX) PROGRESS_MAX else msg.arg1
                updateProgress(now)
                if (now == PROGRESS_MAX) {
                    mProgress0.visibility = View.GONE

                    // delay 1 second then reset progress to final
                    delayThenUiRun(1000) {
                        mProgress0.visibility = View.VISIBLE
                        updateProgress(mFinalProgress)
                        mBtn0.isEnabled = true
                    }
                } else {
                    sendMsg(now + PROGRESS_STEP)
                }
            }
        }
    }

    internal inner class SecondHandler(looper: Looper) : Handler(looper) {

        fun start() {
            val bar = mProgress1
            bar.visibility = View.VISIBLE
            bar.progress = 0

            // finish
            postDelayed({ bar.progress = 100 }, 500)

            //hide
            postDelayed({ bar.visibility = View.GONE }, 600)

            // reset progress before ending animation finish
            postDelayed({ bar.progress = 30 }, 700)

            // finish again
            postDelayed({ bar.progress = 100 }, 750)

            // hide again
            postDelayed({ bar.visibility = View.GONE }, 800)
        }
    }
}
