package com.marco.emergencyalert;






import java.text.DecimalFormat;

import com.marco.emergencyalert.SwitchButton.OnCheckedChangeListener;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class AlertSettings extends Activity{
	  private Button alertbottom;
	  private Button configbottom;
	  private Button contactnumberconfirm;
	  private Button contactnumber1search;
	  private Button contactnumber2search;
	  private Button contactnumber3search;
	  
	  private SwitchButton serviceswitch;
	  private SwitchButton soundswitch;
	  private SwitchButton lightswitch;
	  private SwitchButton baiduswitch;
	  private SwitchButton smsswitch;
	  
	  private TextView contactnumber1;
	  private TextView contactnumber2;
	  private TextView contactnumber3;
	  private TextView temperaturenumber;
	  private TextView altitudenumber;
	  private TextView accnumber;
	  
	  private SeekBar temperaturesettingseekbar;
	  private SeekBar altitudesettingseekbar;
	  private SeekBar accsettingseekbar;
	  
	  private String contact1;
	  private String contact2;
	  private String contact3;
	  
	  private boolean servicesetting;
	  private int searchbuttonnumber;
	  private int temperaturesetting;
	  private int altitudesetting;
	  private int accsetting;
	  private long exitTime = 0;
  	  SharedPreferences preferences;
  	  SharedPreferences.Editor editor;
  	  private static final int PICK_CONTACT_SUBACTIVITY = 2;
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settinglayout);
        alertbottom=(Button)findViewById(R.id.alertbottom);
        configbottom=(Button)findViewById(R.id.configbottom);
        contactnumber1search=(Button)findViewById(R.id.contactnumber1search);
        contactnumber2search=(Button)findViewById(R.id.contactnumber2search);
        contactnumber3search=(Button)findViewById(R.id.contactnumber3search);
        contactnumberconfirm=(Button)findViewById(R.id.contactnumberconfirm);
        
        temperaturesettingseekbar=(SeekBar)findViewById(R.id.temperaturesettingseekbar);
        altitudesettingseekbar=(SeekBar)findViewById(R.id.altitudesettingseekbar);
        accsettingseekbar=(SeekBar)findViewById(R.id.accsettingseekbar);
        
        contactnumber1=(TextView)findViewById(R.id.contactnumber1);
        contactnumber2=(TextView)findViewById(R.id.contactnumber2);
        contactnumber3=(TextView)findViewById(R.id.contactnumber3);
        temperaturenumber=(TextView)findViewById(R.id.temperaturenumber);
        altitudenumber=(TextView)findViewById(R.id.altitudenumber);
        accnumber=(TextView)findViewById(R.id.accnumber);
        
        serviceswitch=(SwitchButton)findViewById(R.id.serviceswitch);
        soundswitch=(SwitchButton)findViewById(R.id.soundswitch);
        lightswitch=(SwitchButton)findViewById(R.id.lightswitch);
        baiduswitch=(SwitchButton)findViewById(R.id.baiduswitch);
        smsswitch=(SwitchButton)findViewById(R.id.smswitch);
        
        preferences = getSharedPreferences("setting", MODE_PRIVATE);
		servicesetting = preferences.getBoolean("servicesetting",true);
		boolean soundsetting = preferences.getBoolean("soundsetting",true);
		boolean lightsetting = preferences.getBoolean("lightsetting",true);
		boolean baidusetting = preferences.getBoolean("baidusetting",true);
		boolean smssetting=preferences.getBoolean("smssetting",true);
		contact1=preferences.getString("contact1", "");
		contact2=preferences.getString("contact2", "");
		contact3=preferences.getString("contact3", "");
		temperaturesetting=preferences.getInt("temperaturesetting", 50);
		altitudesetting=preferences.getInt("altitudesetting", 28);
		accsetting=preferences.getInt("accsetting",40);
		
		serviceswitch.changbuttonstatues(servicesetting);
		soundswitch.changbuttonstatues(soundsetting);
		lightswitch.changbuttonstatues(lightsetting);
		baiduswitch.changbuttonstatues(baidusetting);
		smsswitch.changbuttonstatues(smssetting);
		contactnumber1.setText(contact1);
		contactnumber2.setText(contact2);
		contactnumber3.setText(contact3);
		
		if(temperaturesetting==100&&altitudesetting==100&&accsetting==100)
			serviceswitch.changbuttonstatues(false);
		else serviceswitch.changbuttonstatues(true);
		temperaturesettingseekbar.setProgress(temperaturesetting);
		if(temperaturesetting==100) {
			temperaturenumber.setText("  关闭");
		}else{
			DecimalFormat df1 = new DecimalFormat("##.0");
		    temperaturenumber.setText(df1.format(30+temperaturesetting*0.4)+"℃");
		}
		altitudesettingseekbar.setProgress(altitudesetting);
		if(altitudesetting==100) {
			altitudenumber.setText("  关闭");
		}else{
			DecimalFormat df2 = new DecimalFormat("0.00");
			altitudenumber.setText(df2.format(altitudesetting*2.5/100+0.50)+"m");
		}
		accsettingseekbar.setProgress(accsetting);
		if(accsetting==100) {
			accnumber.setText("          关闭");
		}else{
			DecimalFormat df3 = new DecimalFormat("00.0");
			accnumber.setText(df3.format(accsetting*0.25+5)+"m/s^2");
		}
		
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
		    		servicesetting=true;
					editor.putBoolean("servicesetting", isChecked);
					editor.commit();
				} else {
		    		editor = preferences.edit();
		    		servicesetting=false;
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
        smsswitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChange(boolean isChecked) {
				if (isChecked) {
		    		editor = preferences.edit();
					editor.putBoolean("smssetting", isChecked);
					editor.commit();
				} else {
		    		editor = preferences.edit();
					editor.putBoolean("smssetting", isChecked);
					editor.commit();
				}
			}
		});
        contactnumber1search.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
		        Uri uri = Uri.parse("content://contacts/people"); 
		        Intent intent = new Intent(Intent.ACTION_PICK, uri);		        
		        startActivityForResult(intent, PICK_CONTACT_SUBACTIVITY);
		        searchbuttonnumber=1;
			}        	
        });
        contactnumber2search.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
		        Uri uri = Uri.parse("content://contacts/people"); 
		        Intent intent = new Intent(Intent.ACTION_PICK, uri);		        
		        startActivityForResult(intent, PICK_CONTACT_SUBACTIVITY);
		        searchbuttonnumber=2;
			}        	
        });
        contactnumber3search.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
		        Uri uri = Uri.parse("content://contacts/people"); 
		        Intent intent = new Intent(Intent.ACTION_PICK, uri);		        
		        startActivityForResult(intent, PICK_CONTACT_SUBACTIVITY);
		        searchbuttonnumber=3;
			}        	
        });
        contactnumberconfirm.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				contact1=contactnumber1.getText().toString();
				contact2=contactnumber2.getText().toString();
				contact3=contactnumber3.getText().toString();
				int count=0;
	    		editor = preferences.edit();
				editor.putString("contact1", contact1);
				editor.putString("contact2", contact2);
				editor.putString("contact3", contact3);
				editor.commit();
				if(contact1!=null&&!contact1.equals("")) count++;
				if(contact2!=null&&!contact2.equals("")) count++;
				if(contact3!=null&&!contact3.equals("")) count++;
				  Toast.makeText(AlertSettings.this,   
  	                       "您共保存了"+ count +"个有效号码",   
  	                        Toast.LENGTH_SHORT).show(); 			
			}      	
        });
        temperaturesettingseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(progress==100) {
					temperaturenumber.setText("  关闭");
				}else{
					DecimalFormat df = new DecimalFormat("##.0");
				    temperaturenumber.setText(df.format(30+progress*0.4)+"℃");
				}
				temperaturesetting=progress;
	    		editor = preferences.edit();
				editor.putInt("temperaturesetting", temperaturesetting);
				editor.commit();
				if(temperaturesetting==100&&altitudesetting==100&&accsetting==100)
					serviceswitch.changbuttonstatues(false);
				else serviceswitch.changbuttonstatues(true);
				}
			public void onStartTrackingTouch(SeekBar seekBar) {}
			public void onStopTrackingTouch(SeekBar seekBar) { }     	
        });
        altitudesettingseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(progress==100) {
					altitudenumber.setText("  关闭");
				}else{
					DecimalFormat df = new DecimalFormat("0.00");
					altitudenumber.setText(df.format(progress*2.5/100+0.50)+"m");
				}
				altitudesetting=progress;
	    		editor = preferences.edit();
				editor.putInt("altitudesetting", altitudesetting);
				editor.commit();
				if(temperaturesetting==100&&altitudesetting==100&&accsetting==100)
					serviceswitch.changbuttonstatues(false);
				else serviceswitch.changbuttonstatues(true);
			}
			public void onStartTrackingTouch(SeekBar seekBar) {		}
			public void onStopTrackingTouch(SeekBar seekBar) {	   }   	
        });
        accsettingseekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(progress==100) {
					accnumber.setText("          关闭");
				}else{
					DecimalFormat df = new DecimalFormat("00.0");
					accnumber.setText(df.format(progress*0.25+5)+"m/s^2");
				}
				accsetting=progress;
	    		editor = preferences.edit();
				editor.putInt("accsetting", accsetting);
				editor.commit();
				if(temperaturesetting==100&&altitudesetting==100&&accsetting==100)
					serviceswitch.changbuttonstatues(false);
				else serviceswitch.changbuttonstatues(true);
			}
			public void onStartTrackingTouch(SeekBar seekBar) {  }
			public void onStopTrackingTouch(SeekBar seekBar) {  }
        });
	}
	protected void onDestory(){
		super.onDestroy();
	}
	protected void onPause(){
		super.onPause();
	}
	protected void onResume() {
        super.onResume();
        configbottom.setClickable(false);
        Intent intent= new Intent();
        intent.setClass(AlertSettings.this, AlertService.class);
    	stopService(intent);
	}
	protected void onActivityResult 
	  (int requestCode, int resultCode, Intent data) 
	  { 
         if(data!=null){
	    switch (requestCode) 
	    {  
	      case PICK_CONTACT_SUBACTIVITY: 
	        final Uri uriRet = data.getData(); 
	        if(uriRet != null) 
	        { 
	          try 
	          {
	           @SuppressWarnings("deprecation")
			Cursor c = managedQuery(uriRet, null, null, null, null);
	           c.moveToFirst();
	           String strPhone = "";
	           int contactId = c.
	             getInt(c.getColumnIndex(ContactsContract.Contacts._ID));
	           Cursor curContacts = getContentResolver().
	             query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
	                 null,ContactsContract.CommonDataKinds.Phone.
	                   CONTACT_ID +" = "+ contactId,null, null);
	           if(curContacts.getCount()>0)
	           {
	             curContacts.moveToFirst();
	             strPhone = curContacts.getString(curContacts.
	                 getColumnIndex(ContactsContract.CommonDataKinds.
	                                  Phone.NUMBER));
	           }
	           else
	           {
	           }
	           switch(searchbuttonnumber){
	           case 1:contactnumber1.setText(strPhone);break;
	           case 2:contactnumber2.setText(strPhone);break;
	           case 3:contactnumber3.setText(strPhone);break;
	           }
	          } 
	          catch(Exception e) 
	          {             
	            e.printStackTrace(); 
	          } 
	        } 
	        break; 
	    } }
	    super.onActivityResult(requestCode, resultCode, data);    
	  } 
	
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
            if((System.currentTimeMillis()-exitTime) > 2000){  
                Toast.makeText(getApplicationContext(), "再按一次退出", Toast.LENGTH_SHORT).show();                                
                exitTime = System.currentTimeMillis();   
            } else {
        		if(!isMyServiceRunning()&&servicesetting)
        	    	startService(new Intent(this, AlertService.class));	
               finish();
            }
            return true;   
        }
        return super.onKeyDown(keyCode, event);
    }
	private boolean isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.marco.AlertService".equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

    
}
