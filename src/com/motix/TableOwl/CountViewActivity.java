package com.motix.tableowl;

import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPClient;
 
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CountViewActivity extends Activity {
    ProgressDialog progressDialog;
	TextView lblBetCount, lblBetAmount, lblSeat, lblStand, lblTotal, lblTitleMinBet;
	TextView lblActivityCount, lblActivity, lblNextActivity, lblActivity0, lblActivity1, lblActivity2;
	LinearLayout layoutCountViewNumberPad, layoutCountViewMinBetNumberPad, layoutCountViewBetNumberPad;
	int activityLoop, activityCount, Seat, SeatLimit, Stand, eventsNumber;
	Boolean havingEvent, isBacked;
	ImageButton btnActivity, btnRefresh;
	ImageButton btnAddBetCount, btnMinusBetCount, btnEditBetCount, btnEditBetAmount, btnMinBetEdit;
	ImageButton btnMinusSeat, btnAddSeat, btnMinusStand, btnAddStand;
	Calendar StartBet, StartDeal, StartPayout, NextStartBet, EventStart, EventEnd;
	String gender, tableType, minBet, tableID, staffID, location, ObsStart;
	String fileName, Language, ftpServer, ftpUsername, ftpPassword, outputFilePath, outputFilePassword;
	String newLocation, newTableType, stringBetCount, stringBetAmount, stringSuccessFile, stringFailFile;
	String[] arrayActivity, arrayLocation, arrayLocation_zh, arrayTableType, arrayTableType_zh, arrayBetAmount, arrayEvent, arrayEvent_zh;
	
	public void onBackPressed() {
    	AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
    	String message = getResources().getString(R.string.stringFinishMessage);
    	MyAlertDialog.setMessage(message);
    	DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
    	};
    	DialogInterface.OnClickListener cancelClick = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
    	};
    	String okay = getResources().getString(R.string.stringFinishOkay);
    	MyAlertDialog.setPositiveButton(okay, okClick);
    	String cancel = getResources().getString(R.string.stringFinishCancel);
    	MyAlertDialog.setNegativeButton(cancel, cancelClick);
    	MyAlertDialog.show();
	}
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.countview);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle extras = getIntent().getExtras(); 
        if(extras !=null) {
        	gender = extras.getString("Gender");
        	tableType = extras.getString("TableType"); 
        	minBet = extras.getString("MinBet");
        	tableID = extras.getString("TableID"); 
        	staffID = extras.getString("StaffID");
        	location = extras.getString("Location");
        	Language = extras.getString("Language");
        	ftpServer = extras.getString("ftpServer");
        	ftpUsername = extras.getString("ftpUsername");
        	ftpPassword = extras.getString("ftpPassword");
        	outputFilePath = extras.getString("outputFilePath");
        	outputFilePassword = extras.getString("outputFilePassword");
        	SeatLimit = Integer.parseInt(extras.getString("SeatLimit"));
        	
        	arrayLocation = extras.getStringArray("arrayLocation");
        	arrayLocation_zh = extras.getStringArray("arrayLocation_zh");
        	arrayTableType = extras.getStringArray("arrayTableType");
        	arrayTableType_zh = extras.getStringArray("arrayTableType_zh");
        	arrayBetAmount = extras.getStringArray("arrayBetAmount");
        	arrayEvent = extras.getStringArray("arrayEvent");
        	arrayEvent_zh = extras.getStringArray("arrayEvent_zh");
        }
        Log.d("Jerry","Start");
        
        newLocation = location;
        newTableType = tableType;
        if (!Language.equals("eng")) {
        	changeString();
        }
        
		Calendar createTime = Calendar.getInstance();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	ObsStart = sdf.format(createTime.getTime());
    	
    	sdf = new SimpleDateFormat("ddMMyy");
    	fileName = sdf.format(createTime.getTime());
    	sdf = new SimpleDateFormat("ddMMyyHHmmss");
    	fileName += "_"+tableID+"_"+sdf.format(createTime.getTime())+"_"+newTableType;
        
        activityLoop = 0;	//Bet, Deal, Payout
        Seat = 0;
        Stand = 0;
        activityCount = 1;
        
        isBacked = true;
    	havingEvent = false;
    	
		StartBet = Calendar.getInstance();
    	arrayActivity = getResources().getStringArray(R.array.stringArrayActivity);
    	
        layoutCountViewNumberPad = (LinearLayout)findViewById(R.id.layoutCountViewNumberPad);
        layoutCountViewNumberPad.setVisibility(LinearLayout.GONE);
        layoutCountViewNumberPad.setX(470);
        layoutCountViewNumberPad.setY(440);
        
		lblBetCount = (TextView)findViewById(R.id.lblBetCount);
		lblBetCount.setText("0");
		
		lblBetAmount = (TextView)findViewById(R.id.lblBetAmount);
		lblBetAmount.setText("0");
		
		lblSeat = (TextView)findViewById(R.id.lblSeat);
		lblSeat.setText("0");
		lblStand = (TextView)findViewById(R.id.lblStand);
		lblStand.setText("0");
		lblTotal = (TextView)findViewById(R.id.lblTotal);
		lblTotal.setText("0");
        
        TextView lblTitleTableType = (TextView)findViewById(R.id.lblTitleTableType);
        lblTitleTableType.setText(tableType);
        
        TextView lblTitleTableID = (TextView)findViewById(R.id.lblTitleTableID);
        lblTitleTableID.setText(tableID);
        
        ImageButton btnTitleGender = (ImageButton)findViewById(R.id.imageviewTitleGender);
        if (gender.equals("Man")) {
        	btnTitleGender.setImageResource(R.drawable.gender_male);
        } else {
        	btnTitleGender.setImageResource(R.drawable.gender_female);
        }

        ImageButton btnTitleUploadData = (ImageButton)findViewById(R.id.btnTitleUploadData);
        btnTitleUploadData.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		uploadMessageShowUp();
        	}
        });
        
        ImageButton btnTitleSetting = (ImageButton)findViewById(R.id.btnTitleSetting);
        btnTitleSetting.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		onBackPressed();
        	}
        });
        
        lblTitleMinBet = (TextView)findViewById(R.id.lblTitleMinBet);
        lblTitleMinBet.setText(minBet);
		
		lblActivityCount = (TextView)findViewById(R.id.lblActivityCount);
		lblActivityCount.setText("1");
		
		lblActivity = (TextView)findViewById(R.id.lblActivity);
		lblActivity.setText(arrayActivity[0]);
		
		lblNextActivity = (TextView)findViewById(R.id.lblNextActivity);
		lblNextActivity.setText("... "+arrayActivity[2]);
		
		lblActivity0 = (TextView)findViewById(R.id.lblActivity0);
		lblActivity0.setText(arrayActivity[0]);
		lblActivity0.setTextColor(Color.parseColor("#FFFFFF"));
		
		lblActivity1 = (TextView)findViewById(R.id.lblActivity1);
		lblActivity1.setText(arrayActivity[1]);
		lblActivity1.setTextColor(Color.parseColor("#000000"));

		lblActivity2 = (TextView)findViewById(R.id.lblActivity2);
		lblActivity2.setText(arrayActivity[2]);
		lblActivity2.setTextColor(Color.parseColor("#000000"));
		
		LinearLayout layoutBackground = (LinearLayout)findViewById(R.id.layoutCountViewBackground);
		layoutBackground.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		hiddenMinBetNumberPad();
        	}
		});
		
		btnMinusSeat = (ImageButton)findViewById(R.id.btnMinusSeat);
		btnMinusSeat.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Seat--;
        		if (Seat<0) {
        			Seat = 0;
        		}
        		lblSeat.setText(String.valueOf(Seat));
        		lblTotal.setText(String.valueOf(Stand+Seat));
        		hiddenMinBetNumberPad();
        	}
        });
		
		btnAddSeat = (ImageButton)findViewById(R.id.btnAddSeat);
		btnAddSeat.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Seat++;
        		if (Seat>SeatLimit) {
        			Seat = SeatLimit;
        		}
        		lblSeat.setText(String.valueOf(Seat));
        		lblTotal.setText(String.valueOf(Stand+Seat));
        		hiddenMinBetNumberPad();
        	}
        });
		
		btnMinusStand = (ImageButton)findViewById(R.id.btnMinusStand);
		btnMinusStand.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Stand--;
        		if (Stand<0) {
        			Stand = 0;
        		}
        		lblStand.setText(String.valueOf(Stand));
        		lblTotal.setText(String.valueOf(Stand+Seat));
        		hiddenMinBetNumberPad();
        	}
        });
		
		btnAddStand = (ImageButton)findViewById(R.id.btnAddStand);
		btnAddStand.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Stand++;
        		lblStand.setText(String.valueOf(Stand));
        		lblTotal.setText(String.valueOf(Stand+Seat));
        		hiddenMinBetNumberPad();
        	}
        });
		
		btnActivity = (ImageButton)findViewById(R.id.btnActivity);
		btnActivity.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		isBacked = false;
        		activityLoop++;
         		if (activityLoop>2) {
        			activityLoop = 0;
        		}
        		if (activityLoop==0) {
        			activityCount++;
        			stringBetCount = lblBetCount.getText().toString();
        			lblBetCount.setText("0");
        			stringBetAmount = lblBetAmount.getText().toString();
        			lblBetAmount.setText("0");
        		}
        		chagneBackground();
        		hiddenMinBetNumberPad();
        	}
        });
		
		btnRefresh = (ImageButton)findViewById(R.id.btnRefresh);
		btnRefresh.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if (isBacked || activityLoop==0) {
        			return;
        		}
        		isBacked = true;
        		activityLoop--;
        		if (activityLoop<0) {
        			activityLoop = 2;
        		}
        		if (activityLoop==2) {
        			activityCount--;
        		}
        		chagneBackground();
        		hiddenMinBetNumberPad();
        	}
        });
		
		btnMinBetEdit = (ImageButton)findViewById(R.id.btnMinBetEdit);
		btnMinBetEdit.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if (layoutCountViewMinBetNumberPad.getVisibility()==LinearLayout.GONE) {
        			if (layoutCountViewMinBetNumberPad.getX()==0) {
        				TextView lblTitleTableID = (TextView)findViewById(R.id.lblTitleTableID);
        	        	TextView lblTitleMinBetSpace = (TextView)findViewById(R.id.lblTitleMinBetSpace);
        	        	int space = lblTitleTableID.getWidth()+lblTitleMinBetSpace.getWidth();
        	    		layoutCountViewMinBetNumberPad.setX(space+75);
        			}
        			layoutCountViewNumberPad.setVisibility(LinearLayout.GONE);
        			layoutCountViewBetNumberPad.setVisibility(LinearLayout.GONE);
        			layoutCountViewMinBetNumberPad.setVisibility(LinearLayout.VISIBLE);
        		} else {
            		hiddenMinBetNumberPad();
        		}
        	}
        });
		
        for (int i=0; i<11; i++) {
    		final String number;
    		if (i==10) {
    			number = "00";
    		} else {
    			number = String.valueOf(i);
    		}
            int resID = getResources().getIdentifier("btnCountViewMinBetNumber"+number, "id", "com.motix.TableOwl");
        	Button btnNumber = (Button)findViewById(resID);
        	btnNumber.setOnClickListener(new OnClickListener() {
            	
            	public void onClick(View v) {
            		clickMinBetNumberPad(number);
            	}
        	});
        }

    	layoutCountViewMinBetNumberPad = (LinearLayout)findViewById(R.id.layoutCountViewMinBetNumberPad);
    	layoutCountViewMinBetNumberPad.setVisibility(LinearLayout.GONE);
    	layoutCountViewMinBetNumberPad.setY(50);
        
        Button btnCountViewMinBetBack = (Button)findViewById(R.id.btnCountViewMinBetBack);
        btnCountViewMinBetBack.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
            	if (lblTitleMinBet.getText().length()!=0) {
            		String myString = (String) lblTitleMinBet.getText().subSequence(0, lblTitleMinBet.getText().length()-1);
            		lblTitleMinBet.setText(myString);
            	}
        	}
        });
		
        btnEditBetAmount = (ImageButton)findViewById(R.id.btnEditBetAmount);
        btnEditBetAmount.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if (layoutCountViewNumberPad.getVisibility()==LinearLayout.GONE) {
        			hiddenMinBetNumberPad();
        			layoutCountViewNumberPad.setVisibility(LinearLayout.VISIBLE);
        		} else {
        			layoutCountViewNumberPad.setVisibility(LinearLayout.GONE);
        		}
        	}
        });
        
        for (int i=0; i<11; i++) {
    		final String number;
    		if (i==10) {
    			number = "00";
    		} else {
    			number = String.valueOf(i);
    		}
            int resID = getResources().getIdentifier("btnCountViewNumber"+number, "id", "com.motix.TableOwl");
        	Button btnNumber = (Button)findViewById(resID);
        	btnNumber.setOnClickListener(new OnClickListener() {
            	public void onClick(View v) {
            		clickNumberPad(number);
            	}
        	});
        }
        
        Button btnCountViewNumberBack = (Button)findViewById(R.id.btnCountViewNumberBack);
        btnCountViewNumberBack.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
            	if (lblBetAmount.getText().length()!=0) {
            		String myString = (String) lblBetAmount.getText().subSequence(0, lblBetAmount.getText().length()-1);
            		lblBetAmount.setText(myString);
            	}
        	}
        });
        
    	layoutCountViewBetNumberPad = (LinearLayout)findViewById(R.id.layoutCountViewBetNumberPad);
    	layoutCountViewBetNumberPad.setVisibility(LinearLayout.GONE);
    	layoutCountViewBetNumberPad.setX(300);
    	layoutCountViewBetNumberPad.setY(355);
    	
    	btnEditBetCount = (ImageButton)findViewById(R.id.btnEditBetCount);
    	btnEditBetCount.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if (layoutCountViewBetNumberPad.getVisibility()==LinearLayout.GONE) {
        			hiddenMinBetNumberPad();
        			layoutCountViewBetNumberPad.setVisibility(LinearLayout.VISIBLE);
        		} else {
        			layoutCountViewBetNumberPad.setVisibility(LinearLayout.GONE);
        		}
        	}
        });
    	
        Button btnCountViewBetBack = (Button)findViewById(R.id.btnCountViewBetBack);
        btnCountViewBetBack.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if (lblBetCount.getText().length()==1) {
        			lblBetCount.setText("0");
        		} else if (lblBetCount.getText().length()>1) {
            		String myString = (String) lblBetCount.getText().subSequence(0, lblBetCount.getText().length()-1);
            		lblBetCount.setText(myString);
            	}
        	}
        });
        
        for (int i=0; i<11; i++) {
    		final String number;
    		if (i==10) number = "00";
    		else number = String.valueOf(i);
            int resID = getResources().getIdentifier("btnCountViewBetNumber"+number, "id", "com.motix.TableOwl");
        	Button btnNumber = (Button)findViewById(resID);
        	btnNumber.setOnClickListener(new OnClickListener() {
            	public void onClick(View v) {
            		clickBetPad(number);
            	}
        	});
        }
        
        btnMinusBetCount = (ImageButton)findViewById(R.id.btnMinusBetCount);
        btnMinusBetCount.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		int Count = Integer.valueOf((String)lblBetCount.getText());
        		if (Count!=0) {
        			Count--;
        			lblBetCount.setText(String.valueOf(Count));
            		hiddenMinBetNumberPad();
        		}
        	}      	
        });
        
        btnAddBetCount = (ImageButton)findViewById(R.id.btnAddBetCount);
        btnAddBetCount.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		int Count = Integer.valueOf((String)lblBetCount.getText());
        		lblBetCount.setText(String.valueOf(Count+1));
        		hiddenMinBetNumberPad();
        	}      	
        });
        
        LinearLayout layoutEvents = (LinearLayout)findViewById(R.id.layoutEvents);
        
        final String[] tempArrayEvent;
	    if (Language.equals("eng")) {
	    	tempArrayEvent = arrayEvent;
	    } else {
	    	tempArrayEvent = arrayEvent_zh;
	    }
        for (int i=0; i<tempArrayEvent.length;i++) {	
        	RelativeLayout layoutNew = new RelativeLayout(this);
        	final int eventsValue = i;
        	
        	ImageButton imgNew = new ImageButton(this);
        	imgNew.setImageResource(R.drawable.btn_events);
        	imgNew.getBackground().setAlpha(0);
        	imgNew.setLayoutParams(new LayoutParams(125,125));
        	layoutNew.addView(imgNew);
        	imgNew.setOnClickListener(new OnClickListener() {
            	public void onClick(View v) {
            		clickEvents(arrayEvent[eventsValue], eventsValue, v);
            		hiddenMinBetNumberPad();
            	}
        	});
        	
        	TextView textNew = new TextView(this);
        	textNew.setText(tempArrayEvent[i]);
        	textNew.setTextColor(Color.WHITE);
        	textNew.setGravity(Gravity.CENTER);
        	textNew.setTextSize(TypedValue.COMPLEX_UNIT_DIP,24);
        	
        	RelativeLayout.LayoutParams btnLayoutParams = new RelativeLayout.LayoutParams(105,125);
        	btnLayoutParams.setMargins(10, 0, 0, 0);
        	
        	layoutNew.addView(textNew, btnLayoutParams);
        	
        	layoutEvents.addView(layoutNew);
        }
        
        LinearLayout layoutBetAmount = (LinearLayout)findViewById(R.id.layoutBetAmount);
        for (int i=0; i<arrayBetAmount.length;i++) {
        	RelativeLayout newLayout = new RelativeLayout(this);
        	
        	final int coinsValue = Integer.valueOf(arrayBetAmount[i]);
        	ImageButton btnNewAddCoins = new ImageButton(this);
        	btnNewAddCoins.setImageResource(R.drawable.xml_add_coin);
        	btnNewAddCoins.setBackgroundDrawable(null);
        	btnNewAddCoins.setOnClickListener(new OnClickListener() {
            	public void onClick(View v) {
            		clickAddCoins(coinsValue);
            		hiddenMinBetNumberPad();
            	}
        	});
        	
        	ImageButton btnNewMinusCoins = new ImageButton(this);
        	btnNewMinusCoins.setImageResource(R.drawable.btn_minus_coins);
        	btnNewMinusCoins.setBackgroundDrawable(null);
        	btnNewMinusCoins.setOnClickListener(new OnClickListener() {

            	public void onClick(View v) {
            		clickMinusCoins(coinsValue);
            		hiddenMinBetNumberPad();
            	}
        	});
        	
        	RelativeLayout.LayoutParams btnLayoutParams = new RelativeLayout.LayoutParams(
        			RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        	btnLayoutParams.setMargins(0, 120, 0, 0);
        	
        	
        	TextView lblNewCoins = new TextView(this);
        	lblNewCoins.setText(arrayBetAmount[i]);
        	lblNewCoins.setTextColor(Color.WHITE);
        	lblNewCoins.setGravity(Gravity.CENTER);
        	lblNewCoins.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        	lblNewCoins.setLayoutParams(new LayoutParams(150,120));

        	newLayout.addView(btnNewAddCoins);
        	newLayout.addView(lblNewCoins);
        	newLayout.addView(btnNewMinusCoins, btnLayoutParams);
        	
        	layoutBetAmount.addView(newLayout); 
        }
    }
    
    public void chagneBackground() {
    	int i = activityLoop;
    	if (i==0) {
    		NextStartBet = Calendar.getInstance();
    		if (activityCount>1 && !isBacked) {
    			writeData();
    		}
    		NextStartBet = null;
    		StartBet = Calendar.getInstance();
		} else if (i==1) {
			StartDeal = Calendar.getInstance();
		} else if (i==2) {
			StartPayout = Calendar.getInstance();
		}
		int resIDBackground = getResources().getIdentifier("tableowl_bg_0"+(i+3), "drawable", "com.motix.TableOwl");
		LinearLayout layoutCountViewBackground = (LinearLayout)findViewById(R.id.layoutCountViewBackground);
		layoutCountViewBackground.setBackgroundResource(resIDBackground);
		
		if (i==0) {
			lblActivity0.setTextColor(Color.parseColor("#FFFFFF"));
			lblActivity1.setTextColor(Color.parseColor("#000000"));
			lblActivity2.setTextColor(Color.parseColor("#000000"));
		} else if (i==1) {
			lblActivity0.setTextColor(Color.parseColor("#000000"));
			lblActivity1.setTextColor(Color.parseColor("#FFFFFF"));
			lblActivity2.setTextColor(Color.parseColor("#000000"));
		} else if (i==2) {
			lblActivity0.setTextColor(Color.parseColor("#000000"));
			lblActivity1.setTextColor(Color.parseColor("#000000"));
			lblActivity2.setTextColor(Color.parseColor("#FFFFFF"));
		}
		lblActivity.setText(arrayActivity[i]);
		int nextActivity = i+1;
		if (nextActivity>2) {
			nextActivity = 0;
		}
		lblNextActivity.setText("... "+arrayActivity[nextActivity]);
		lblActivityCount.setText(""+activityCount);
    }
    
    public void clickEvents(String title, int Number, View v) {
		ImageButton btnEvents = (ImageButton)v;
		LinearLayout layoutCountViewBackground = (LinearLayout)findViewById(R.id.layoutCountViewBackground);
    	if (havingEvent) {
    		if (eventsNumber==Number) {
    			havingEvent = false;
        		EventEnd = Calendar.getInstance();
        		writeEventData(title);
    			btnEvents.setImageResource(R.drawable.btn_events);
    			
    			int resIDBackground = getResources().getIdentifier("tableowl_bg_0"+(activityLoop+3), "drawable", "com.motix.TableOwl");
    			layoutCountViewBackground.setBackgroundResource(resIDBackground);
    		}
    	} else {
    		havingEvent = true;
    		eventsNumber = Number;
    		EventStart = Calendar.getInstance();
    		int resIDEvents = getResources().getIdentifier("btn_events_"+(activityLoop+1), "drawable", "com.motix.TableOwl");
    		btnEvents.setImageResource(resIDEvents);
    		
			layoutCountViewBackground.setBackgroundResource(R.drawable.tableowl_bg_07);
			layoutCountViewNumberPad.setVisibility(LinearLayout.GONE);
    	}
    	btnClickable(!havingEvent);
    }
    
    public void betAmountSetText(String amount) {
		lblBetAmount.setText(amount);
    }
    
    public long checkBetAmount() {
    	long betAmount;
    	if (lblBetAmount.getText().length()>=18) {
    		betAmount = -1;
    	} else if (lblBetAmount.getText().length()==0) {
    		betAmount = 0;
    	} else {
    		betAmount = Long.valueOf((String) lblBetAmount.getText());
    	}
    	return betAmount;
    }
    
    public void clickNumberPad(String i) {
    	long betAmount = checkBetAmount();
    	if (betAmount==-1) {
    		return;
    	}
    	String myString = "0";
    	if (betAmount>0) {
    		myString = lblBetAmount.getText() + i;
    	} else if (!i.equals("0") && !i.equals("00")) {
			myString = i;
    	}
    	betAmountSetText(myString);
    }
    
    public void clickBetPad(String s) {
 		if (lblBetCount.getText().length()<4) {
 			int i = Integer.parseInt((String)lblBetCount.getText());
 			if (i==0) {
 				if (!s.equals("0") && !s.equals("00")) {
 					lblBetCount.setText(s);
 				}
 			} else {
 				String myString = lblBetCount.getText() + s;
 				lblBetCount.setText(myString);
 			}
		}
    }
    
    public void clickMinBetNumberPad(String i) {
  		if (lblTitleMinBet.getText().length()<8) {
  			String myString = "";
  			if (lblTitleMinBet.getText().length()==0) {
  				if (!i.equals("0") && !i.equals("00")) {
  					myString = i;
  				}
  			} else {
  	  			myString = lblTitleMinBet.getText() + i;
  			}
  			lblTitleMinBet.setText(myString);
		}
    }
    
    public void hiddenMinBetNumberPad() {
		layoutCountViewNumberPad.setVisibility(LinearLayout.GONE);
		layoutCountViewBetNumberPad.setVisibility(LinearLayout.GONE);
    	if (lblTitleMinBet.getText().length()!=0) { 
    		layoutCountViewMinBetNumberPad.setVisibility(LinearLayout.GONE);
    	} else {
    		String message = getResources().getString(R.string.stringMinBetEmptyMessage);
    		messageShowUp("",message);
    	}
    }
    
    public void clickAddCoins(int i) {
    	if (havingEvent) {
    		return;
    	}
    	long betAmount = checkBetAmount();
    	if (betAmount==-1) {
    		return;
    	}
    	betAmount = betAmount + i;
    	betAmountSetText(String.valueOf(betAmount));
    }

    public void clickMinusCoins(int i) {
    	if (havingEvent) {
    		return;
    	}
    	long betAmount = checkBetAmount();
    	if (betAmount==-1) {
    		return;
    	}
    	betAmount = betAmount - i;
    	if (betAmount==0) {
    		lblBetAmount.setText("0");
    	} else if (betAmount>=0) {
    		betAmountSetText(String.valueOf(betAmount));
    	}
    }
    
    public void btnClickable(Boolean clickable) {
    	btnActivity.setClickable(clickable);
    	btnRefresh.setClickable(clickable);
    	btnAddBetCount.setClickable(clickable);
    	btnMinusBetCount.setClickable(clickable);
    	btnEditBetCount.setClickable(clickable);
    	btnEditBetAmount.setClickable(clickable);
    	btnMinusSeat.setClickable(clickable);
    	btnAddSeat.setClickable(clickable);
    	btnMinusStand.setClickable(clickable);
    	btnAddStand.setClickable(clickable);
    }
    
    public void changeString() {
       	for (int i=0; i<arrayLocation.length; i++) {
       		if (location.equals(arrayLocation_zh[i])) {
       			newLocation = arrayLocation[i];
       			i = arrayLocation.length;
       		}
       	}
       	for (int i=0; i<arrayTableType.length; i++) {
       		if (tableType.equals(arrayTableType_zh[i])) {
       			newTableType = arrayTableType[i];
       			i = arrayTableType.length;
       		}
       	}
    }
    
    public String DateUtils(Calendar cal) {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	return sdf.format(cal.getTime());
    }
    
    public void writeData() {
    	DecimalFormat df = new DecimalFormat("0.0");
        double BetTime = (StartDeal.getTime().getTime()-StartBet.getTime().getTime())/1000.0;
        double DealTime = (StartPayout.getTime().getTime()-StartDeal.getTime().getTime())/1000.0;
        double PayoutTime = (NextStartBet.getTime().getTime()-StartPayout.getTime().getTime())/1000.0;
        
        Calendar cal = Calendar.getInstance();
        int month = (cal.get(Calendar.MONTH))+1;
        String saveString = (activityCount-1)+",\""+newLocation+"\","+tableID+",\""+newTableType+"\",\""+cal.get(Calendar.DAY_OF_WEEK)+"\"";
        saveString+=","+cal.get(Calendar.YEAR)+","+month+","+cal.get(Calendar.DATE)+","+staffID+",\""+gender+"\"";
        saveString+=",\""+ObsStart+"\",\""+DateUtils(StartBet)+"\","+df.format(BetTime)+",\""+DateUtils(StartDeal)+"\","+df.format(DealTime);
        saveString+=",\""+DateUtils(StartPayout)+"\""+","+df.format(PayoutTime)+","+(BetTime+DealTime+PayoutTime)+","+minBet+","+lblStand.getText();
        saveString+=","+lblSeat.getText()+","+lblTotal.getText()+","+stringBetCount+","+stringBetAmount;
		saveFile(fileName,saveString);
    }
    
    public void writeEventData(String event) {
        Calendar cal = Calendar.getInstance();
        int month = (cal.get(Calendar.MONTH))+1;
        String saveString = activityCount+",\""+newLocation+"\","+tableID+",\""+newTableType+"\",\""+cal.get(Calendar.DAY_OF_WEEK)+"\"";
        saveString+=","+cal.get(Calendar.YEAR)+","+month+","+cal.get(Calendar.DATE)+","+staffID+",\""+gender+"\"";
        saveString+=",\""+ObsStart+"\",,,,,,,,,,,,,,\""+event+"\",\""+DateUtils(EventStart)+"\",\""+DateUtils(EventEnd)+"\"";
		saveFile(fileName,saveString);
    }
    
    public void saveFile(String FileName, String Data) {
    	File root = Environment.getExternalStorageDirectory();
    	if (root.canWrite()) {
    		File dir = new File (root.getAbsolutePath() + "/TableOwl");
    		if (!dir.exists()) dir.mkdirs();
    		File file = new File(dir, FileName+".csv");
    		FileOutputStream out = null;

    		String combinedString;
			if (!file.exists()) {
				String columnString = "Observation,Property,Table,Gametype,DOW,Year,Month,Day,StaffID,StaffGender,ObsStart,StartBet,BetTime,StartDeal,";
				columnString += "DealTime,StartPayout,PayoutTime,RoundTime,MinBet,NumStand,NumSeat,Headcount,BetCount,TotalBet,Event,EventStart,EventEnd";
				combinedString = columnString+"\n"+Data;
			} else {
				combinedString = "\n"+Data;
			}
			combinedString.replaceAll("\\s+", "");
			
    		try {
				out = new FileOutputStream(file, true);
				out.write(combinedString.getBytes("UTF-8"));
				out.flush();
				out.close();
    		} catch (Exception e) {
    	        e.printStackTrace();
    		}
    	}
    }
    
    public void encryptAndUploadFTP(File dir, File toDir, String fileName) {
    	File file = new File(dir, fileName);
    	File to = new File(toDir, fileName);
    	try {
            String key = outputFilePassword;
            Cipher ecipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            DESKeySpec desKeySpec = new DESKeySpec(key.getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
            IvParameterSpec iv = new IvParameterSpec(key.getBytes("UTF-8"));
            ecipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
    	    
    	    InputStream isEncrypt = new FileInputStream(file);
    	    Base64OutputStream b64os = new Base64OutputStream(new FileOutputStream(to), Base64.DEFAULT);
    	    OutputStream osEncrypt = new CipherOutputStream(b64os, ecipher);
    	    int numRead = 0;
  	      	byte[] buf = new byte[1024];
    	    while ((numRead = isEncrypt.read(buf)) >= 0) {
    	    	osEncrypt.write(buf, 0, numRead);
    	    }
    	    osEncrypt.close();
    	    isEncrypt.close();
        } catch (Exception e) {
	        e.printStackTrace();
	    }
    	
    	FTPClient ftpClient = new FTPClient();
    	try {
    	    ftpClient.connect(ftpServer);
    	    ftpClient.login(ftpUsername, ftpPassword);
        	ftpClient.enterLocalPassiveMode();
    	    if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
        	    BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(to));
            	boolean finish = ftpClient.storeFile(outputFilePath+to.getName(), buffIn);
        	    if (finish) {
        	    	file.delete();
        	    	stringSuccessFile += fileName+"\n";
        	    } else {
        	    	stringFailFile += fileName+"\n";
        	    }
        	    buffIn.close();
    	    }
        	ftpClient.logout();
        	ftpClient.disconnect();
    	} catch (Exception e) {
	        e.printStackTrace();
    		stringFailFile += fileName+"\n";
    	}
    }
    
    public void uploadData() {
    	progressDialog = new ProgressDialog(this);
        String stringUploadingMessage = getResources().getString(R.string.stringUploadingMessage);
        progressDialog.setMessage(stringUploadingMessage+"...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    	 new Thread() {
             public void run() {
             	stringSuccessFile = "";
            	stringFailFile = "";
            	File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/TableOwl");
            	if (!dir.exists()) dir.mkdirs();
            	File toDir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/TableOwlSent/");
            	if (!toDir.exists()) toDir.mkdirs();
        		File[] files = dir.listFiles();
            	for (int i=0; i<files.length; i++) {
            		encryptAndUploadFTP(dir, toDir, files[i].getName());
            	}
            	
            	closeMessageShowUp(stringSuccessFile, stringFailFile);
            	progressDialog.cancel();
             }
    	 }.start();
    }
    
    private void messageShowUp(String title, String message) {
    	final AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
		MyAlertDialog.setTitle(title);
		MyAlertDialog.setMessage(message);
        MyAlertDialog.show();
    }
    
    
    private void uploadMessageShowUp() {
        AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
    	String message = getResources().getString(R.string.stringUploadMessage);
    	MyAlertDialog.setMessage(message);
    	DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
        		uploadData();
			}
    	};
    	String ok = getResources().getString(R.string.stringFinishOkay);
    	MyAlertDialog.setPositiveButton(ok, okClick);
    	DialogInterface.OnClickListener cancelClick = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
    	};
    	String cancel = getResources().getString(R.string.stringFinishCancel);
    	MyAlertDialog.setNegativeButton(cancel, cancelClick);
        MyAlertDialog.show();
    }

    private void closeMessageShowUp(String sFile, String fFile) {
    	String message = getResources().getString(R.string.stringUploadNoFile);
    	if (sFile.length()>0) {
    		message = stringSuccessFile+getResources().getString(R.string.stringUploadSuccessful);
    		if (fFile.length()>0) message += "\n\n"+stringFailFile+getResources().getString(R.string.stringUploadFailed);
    	} else if (fFile.length()>0) {
    		message = stringFailFile+getResources().getString(R.string.stringUploadFailed);
    	}
    	final AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
    	MyAlertDialog.setMessage(message);
    	DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
    			Intent newIntent = new Intent();
    			newIntent.setClassName("com.motix.TableOwl", "com.motix.TableOwl.TableOwlActivity");
    			newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    			startActivity(newIntent);
			}
    	};
    	String logout = getResources().getString(R.string.stringLogout);
    	MyAlertDialog.setPositiveButton(logout, okClick);
        this.runOnUiThread(new Runnable() {
        	public void run() {
        		MyAlertDialog.show();
        	}
        });
    }
    
}
