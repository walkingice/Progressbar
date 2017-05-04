package bar.foo.julian.dummy;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FOOBAR";
    private static final int REQ_INITIUM = 0x1122;
    private static final int REQ_MOZILLA = 0x011A;

    private int mMenuItemNums = 3;
    private int mTopBarColor = Color.MAGENTA;
    private int mBottomBarColor = Color.MAGENTA;
    private Bitmap mIcon;
    private Button mBtn0;
    private EditText mInput;
    private View mTopColorPreview;
    private View mBottomColorPreview;

    private boolean mCustomAnimation = true;
    private boolean mCustomCloseBtn = false;
    private Mode mMode = Mode.NONE;

    private enum Mode {
        NONE,
        NORMAL,
        UGLY,
        DEPRECATED
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIcon = getBitmap(R.drawable.small_logo);
        mBtn0 = (Button) findViewById(R.id.btn_0);
        mInput = (EditText) findViewById(R.id.edit_text);
        setPreview();
        bindButton();
        setSpinners();
        setToggleButton();
    }

    private Bitmap getBitmap(int res) {
        return BitmapFactory.decodeResource(getResources(), res);
    }

    private void setPreview() {
        mTopColorPreview = findViewById(R.id.top_color_preview);
        mBottomColorPreview = findViewById(R.id.bottom_color_preview);
        mTopColorPreview.setBackgroundColor(mTopBarColor);
        mBottomColorPreview.setBackgroundColor(mBottomBarColor);
    }

    private void bindButton() {
        mBtn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleInput(mInput.getEditableText(), mMode);
            }
        });
    }

    private void setSpinners() {
        ((Spinner) findViewById(R.id.menu_item_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] nums = getResources().getStringArray(R.array.menu_item_nums);
                mMenuItemNums = Integer.parseInt(nums[i]);
                mMenuItemNums = Math.min(mMenuItemNums, 5);
                mMenuItemNums = Math.max(mMenuItemNums, 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ((Spinner) findViewById(R.id.top_color_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mTopBarColor = getColorByIdx(i);
                mTopColorPreview.setBackgroundColor(mTopBarColor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ((Spinner) findViewById(R.id.bottom_color_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mBottomBarColor = getColorByIdx(i);
                mBottomColorPreview.setBackgroundColor(mBottomBarColor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ((Spinner) findViewById(R.id.launch_mode_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Yes, I hard-coded, bite me!
                HashMap<Integer, Mode> map = new HashMap<>();
                map.put(0, Mode.NONE);
                map.put(1, Mode.NORMAL);
                map.put(2, Mode.UGLY);
                map.put(3, Mode.DEPRECATED);
                mMode = map.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setToggleButton() {
        ((ToggleButton) findViewById(R.id.widget_custom_animation)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        mCustomAnimation = b;
                    }
                });

        ((ToggleButton) findViewById(R.id.widget_custom_close_btn)).setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        mCustomCloseBtn = b;
                    }
                });
    }

    // Yes, Dirty! Bite me!
    private int getColorByIdx(int idx) {
        String[] arrays = getResources().getStringArray(R.array.selectable_colors);
        String colorText = arrays[idx];
        switch (colorText) {
            case "MAGENTA":
                return Color.MAGENTA;
            case "CYAN":
                return Color.CYAN;
            case "GREEN":
                return Color.GREEN;
            case "RED":
                return Color.RED;
            case "BLACK":
                return Color.BLACK;
            case "WHITE":
                return Color.WHITE;
            case "#FF8877FF":
                return 0xFF8877FF;
            case "#00FFFFFF":
                return 0x00FFFFFF;
            case "#FFFFFFFF":
                return 0xFFFFFFFF;
            case "#11FF0000":
                return 0x11FF0000;
            case "#55FF0000":
                return 0x55FF0000;
            case "#AAFF0000":
                return 0xAAFF0000;
            case "#EEFF0000":
                return 0xEEFF0000;
            case "#1100FF00":
                return 0x1100FF00;
            case "#5500FF00":
                return 0x5500FF00;
            case "#AA00FF00":
                return 0xAA00FF00;
            case "#EE00FF00":
                return 0xEE00FF00;
            default:
                return getResources().getColor(R.color.mozillaRed);
        }
    }

    private void handleInput(CharSequence input, Mode mode) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();

        // set action button
        builder.setActionButton(mIcon, "The initium", createIntent(REQ_INITIUM, "https://theinitium.com/"));
        builder.setToolbarColor(mTopBarColor);

        // set menu items
        setMenuItems(builder);

        // set animation
        if (mCustomAnimation) {
            builder.setStartAnimations(this, R.anim.push_down_in, R.anim.push_down_out);
            builder.setExitAnimations(this, R.anim.push_up_in, R.anim.push_up_out);
        }

        if (mCustomCloseBtn) {
            builder.setCloseButtonIcon(getBitmap(R.drawable.ic_e));
        }

        if (mode != Mode.NONE) {
            setBottomToolbar(builder, mode);
        }

        try {
            CustomTabsIntent intent = builder.build();
            intent.launchUrl(this, Uri.parse(input.toString()));
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setMenuItems(CustomTabsIntent.Builder builder) {
        for (int i = 0; i < mMenuItemNums; i++) {
            builder.addMenuItem("Search " + (i + 1),
                    createIntent(0x42 + i, "https://duckduckgo.com/?q=" + i));
        }
    }

    private PendingIntent createIntent(int reqCode, String uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uri));
        PendingIntent pending = PendingIntent.getActivities(this,
                reqCode,
                new Intent[]{intent},
                PendingIntent.FLAG_CANCEL_CURRENT);

        return pending;
    }

    private void setBottomToolbar(CustomTabsIntent.Builder builder, Mode mode) {
        // add toolbar items
        builder.setSecondaryToolbarColor(mBottomBarColor);
        if (mode == Mode.DEPRECATED) {
            deprecatedAddingToolbarItem(builder);
        } else {
            setSecondaryToolbar(builder, mode);
        }
    }

    private void setSecondaryToolbar(CustomTabsIntent.Builder builder, Mode mode) {
        int layoutRes = (mode == Mode.NORMAL) ?
                R.layout.layout_remote_views :
                R.layout.layout_ugly_remote_views;

        RemoteViews rvs = new RemoteViews(getPackageName(), layoutRes);
        builder.setSecondaryToolbarViews(rvs,
                new int[]{R.id.remote_btn_1, R.id.remote_btn_2},
                createIntent(REQ_MOZILLA, "https://www.mozilla.org"));
    }

    private void deprecatedAddingToolbarItem(CustomTabsIntent.Builder builder) {
        // up to 5 items, otherwise got Exception
        //   java.lang.IllegalStateException: Exceeded maximum toolbar item count of 5
        builder.addToolbarItem(1, getBitmap(R.drawable.ic_a), "item a", createIntent(1, "https://duckduckgo.com/?q=a"));
        builder.addToolbarItem(2, getBitmap(R.drawable.ic_b), "item b", createIntent(2, "https://duckduckgo.com/?q=b"));
        builder.addToolbarItem(3, getBitmap(R.drawable.ic_c), "item c", createIntent(3, "https://duckduckgo.com/?q=c"));
        builder.addToolbarItem(4, getBitmap(R.drawable.ic_d), "item d", createIntent(4, "https://duckduckgo.com/?q=d"));
        builder.addToolbarItem(5, getBitmap(R.drawable.ic_e), "item e", createIntent(5, "https://duckduckgo.com/?q=e"));
    }
}
