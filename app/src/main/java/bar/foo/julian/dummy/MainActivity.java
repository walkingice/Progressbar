package bar.foo.julian.dummy;
/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.ToggleButton;

import org.mozilla.gecko.widget.themed.ThemedProgressBar;

public class MainActivity extends AppCompatActivity {

    private final static int WHAT_PROGRESS = 123;
    private final static int PROGRESS_MAX = 100;
    private final static int PROGRESS_STEP = 40;
    private final static long PROGRESS_TIME = 300;

    private int mFinalProgress = 50;
    private Button mBtn0;

    private ProgressBar mProgress0;
    private ProgressBar mProgress1;
    private boolean mIsNormalMode = true;

    private MyHandler mHandler = new MyHandler(Looper.getMainLooper());
    private SecondHandler mHandler2 = new SecondHandler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtn0 = (Button) findViewById(R.id.btn_0);
        mProgress0 = (ProgressBar) findViewById(R.id.progress0);
        mProgress1 = (ProgressBar) findViewById(R.id.progress1);
        bindButton();
        setSpinners();
        setToggleButton();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateProgress(50);
    }

    private void bindButton() {
        mBtn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onReloadClicked();
            }
        });
    }

    private void updateProgress(int progress) {
        animateProgress(mProgress0, progress);
    }

    private void animateProgress(ProgressBar bar, int progress) {
        bar.setProgress(progress);
    }

    private void sendMsg(int progress) {
        sendMsg(progress, PROGRESS_TIME);
    }

    private void sendMsg(int progress, long delay) {
        mHandler.removeMessages(WHAT_PROGRESS);
        Message msg = mHandler.obtainMessage(WHAT_PROGRESS);
        msg.arg1 = progress;
        mHandler.sendMessageDelayed(msg, delay);
    }

    private void setSpinners() {
        ((Spinner) findViewById(R.id.menu_item_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] nums = getResources().getStringArray(R.array.menu_item_nums);
                mFinalProgress = Integer.parseInt(nums[i]);
                mFinalProgress = Math.min(mFinalProgress, 100);
                mFinalProgress = Math.max(mFinalProgress, 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ((Spinner) findViewById(R.id.progress0_height)).setOnItemSelectedListener(buildSpinnerListener(mProgress0));
    }

    private AdapterView.OnItemSelectedListener buildSpinnerListener(final View progressView) {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] nums = getResources().getStringArray(R.array.progressbar_height);
                int heightInDp = Integer.parseInt(nums[i]);
                ViewGroup.LayoutParams params = progressView.getLayoutParams();
                params.height = dp2px(heightInDp);
                progressView.setLayoutParams(new RelativeLayout.LayoutParams(params.width, dp2px(heightInDp)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };
    }

    private void setToggleButton() {
        ((ToggleButton) findViewById(R.id.progress0_toggle)).setOnCheckedChangeListener(buildCheckHandler(mProgress0));
    }

    private CompoundButton.OnCheckedChangeListener buildCheckHandler(final View progressView) {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mIsNormalMode = b;
                if (progressView instanceof ThemedProgressBar) {
                    ((ThemedProgressBar) progressView).setPrivateMode(!mIsNormalMode);
                }
            }
        };
    }

    private void onReloadClicked() {
        mBtn0.setEnabled(false);
        sendMsg(0);
        mHandler2.start();
    }

    public int dp2px(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = dp * metrics.density;
        return (int) px;
    }

    // To simulate page loading progress from 0, 20, 40....100.
    class MyHandler extends Handler {

        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_PROGRESS) {
                int now = msg.arg1;
                now = (now > PROGRESS_MAX) ? PROGRESS_MAX : now;
                if (now == PROGRESS_MAX) {
                    updateProgress(now);
                    mProgress0.setVisibility(View.GONE);

                    // delay 1 second then reset progress to final
                    final Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgress0.setVisibility(View.VISIBLE);
                                    updateProgress(mFinalProgress);
                                    mBtn0.setEnabled(true);
                                }
                            });
                        }
                    });
                    t.start();
                } else {
                    updateProgress(now);
                    sendMsg(now + PROGRESS_STEP);
                }
            }
        }
    }

    class SecondHandler extends Handler {

        public SecondHandler(Looper looper) {
            super(looper);
        }

        public void start() {
            final ProgressBar bar = mProgress1;
            bar.setVisibility(View.VISIBLE);
            bar.setProgress(0);

            // finish
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    bar.setProgress(100);
                }
            }, 500);

            //hide
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    bar.setVisibility(View.GONE);
                }
            }, 600);

            // reset progress before ending animation finish
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    bar.setProgress(30);
                }
            }, 700);

            // finish again
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    bar.setProgress(100);
                }
            }, 750);

            // hide again
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    bar.setVisibility(View.GONE);
                }
            }, 800);
        }
    }
}
