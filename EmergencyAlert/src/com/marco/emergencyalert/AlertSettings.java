package com.marco.emergencyalert;



import com.marco.emergencyalert.SwitchButton.OnCheckedChangeListener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class AlertSettings extends Activity{
	  private Button alertbottom;
	  private Button configbottom;
	  
	  private SwitchButton serviceswitch;
	  private SwitchButton soundswitch;
	  private SwitchButton lightswitch;
	  private SwitchButton baiduswitch;
	  
	  private long exitTime = 0;
  	  SharedPreferences preferences;
  	  SharedPreferences.Editor editor;
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settinglayout);
        alertbottom=(Button)findViewById(R.id.alertbottom);
        configbottom=(Button)findViewById(R.id.configbottom);
        
        serviceswitch=(SwitchButton)findViewById(R.id.serviceswitch);
        soundswitch=(SwitchButton)findViewById(R.id.soundswitch);
        lightswitch=(SwitchButton)findViewById(R.id.lightswitch);
        baiduswitch=(SwitchButton)findViewById(R.id.baiduswitch);
        
        preferences = getSharedPreferences("setting", MODE_PRIVATE);
		boolean servicesetting = preferences.getBoolean("servicesetting",false);
		boolean soundsetting = preferences.getBoolean("soundsetting",false);
		boolean lightsetting = preferences.getBoolean("lightsetting",false);
		boolean baidusetting = preferences.getBoolean("baidusetting",false);
		serviceswitch.changbuttonstatues(servicesetting);
		soundswitch.changbuttonstatues(soundsetting);
		lightswitch.changbuttonstatues(lightsetting);
		baiduswitch.changbuttonstatues(baidusetting);
               
        alertbottom.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				startActivity(new Intent (AlertSettings.this, AlertActivity.class));
        		finish();
        		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out); 
			}      	
        });
        serviceswitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChange(boolean isChecked) {
				if (isChecked) {
		    		editor = preferences.edit();
					editor.putBoolean("servicesetting", isChecked);
					editor.commit();
				} else {
		    		editor = preferences.edit();
					editor.putBoolean("servicesetting", isChecked);
					editor.commit();
				}
			}
		});
        soundswitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChange(boolean isChecked) {
				if (isChecked) {
		    		editor = preferences.edit();
					editor.putBoolean("soundsetting", isChecked);
					editor.commit();
				} else {
		    		editor = preferences.edit();
					editor.putBoolean("soundsetting", isChecked);
					editor.commit();
				}
			}
		});
        lightswitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChange(boolean isChecked) {
				if (isChecked) {
		    		editor = preferences.edit();
					editor.putBoolean("lightsetting", isChecked);
					editor.commit();
				} else {
		    		editor = preferences.edit();
					editor.putBoolean("lightsetting", isChecked);
					editor.commit();
				}
			}
		});
        baiduswitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChange(boolean isChecked) {
				if (isChecked) {
		    		editor = preferences.edit();
					editor.putBoolean("baidusetting", isChecked);
					editor.commit();
				} else {
		    		editor = preferences.edit();
					editor.putBoolean("baidusetting", isChecked);
					editor.commit();
				}
			}
		});
	
	}
	protected void onResume() {
        super.onResume();
        configbottom.setPressed(true);
        configbottom.setClickable(false);
	}
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
            if((System.currentTimeMillis()-exitTime) > 2000){  
                Toast.makeText(getApplicationContext(), "再按一次退出", Toast.LENGTH_SHORT).show();                                
                exitTime = System.currentTimeMillis();   
            } else {
               finish();
            }
            return true;   
        }
        return super.onKeyDown(keyCode, event);
    }
}
