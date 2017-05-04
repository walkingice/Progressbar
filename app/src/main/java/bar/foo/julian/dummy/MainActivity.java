package bar.foo.julian.dummy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FOOBAR";

    private int mFinalProgress = 50;
    private Button mBtn0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtn0 = (Button) findViewById(R.id.btn_0);
        bindButton();
        setSpinners();
        setToggleButton();
    }

    private void bindButton() {
        mBtn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onReloadClicked();
            }
        });
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
    }

    private void setToggleButton() {
        //((ToggleButton) findViewById(R.id.widget_custom_animation)).setOnCheckedChangeListener(
        //        new CompoundButton.OnCheckedChangeListener() {
        //            @Override
        //            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        //                mCustomAnimation = b;
        //            }
        //        });
    }

    private void onReloadClicked() {
    }
}
