package com.motix.tableowl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class SelectTypeActivity extends Activity {
	int MaxTableID, MaxMinBet;
	Boolean isEditingTableID, isEditingMinBet, isMan, isWoman;
	String TableType, StaffID, Location, Language, ftpServer, ftpUsername, ftpPassword, outputFilePath, outputFilePassword, SeatLimit;
	LinearLayout layoutNumberPad;
	int MinBetNumberPadY, TableIDNumberPadY;
	TextView lblMinBet, lblTableID;
	String[] arrayLocation, arrayLocation_zh, arrayTableType, arrayTableType_zh, arrayTableType_SeatLimit, arrayBetAmount, arrayEvent, arrayEvent_zh;
	
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selecttype);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        Bundle extras = getIntent().getExtras(); 
        if(extras !=null) {
        	StaffID = extras.getString("StaffID");
        	Location = extras.getString("Location");
        	Language = extras.getString("Language");
        	ftpServer = extras.getString("ftpServer");
        	ftpUsername = extras.getString("ftpUsername");
        	ftpPassword = extras.getString("ftpPassword");
        	outputFilePath = extras.getString("outputFilePath");
        	outputFilePassword = extras.getString("outputFilePassword");
        	
        	arrayLocation = extras.getStringArray("arrayLocation");
        	arrayLocation_zh = extras.getStringArray("arrayLocation_zh");
        	arrayTableType = extras.getStringArray("arrayTableType");
        	arrayTableType_zh = extras.getStringArray("arrayTableType_zh");
        	arrayTableType_SeatLimit = extras.getStringArray("arrayTableType_SeatLimit");
        	arrayBetAmount = extras.getStringArray("arrayBetAmount");
        	arrayEvent = extras.getStringArray("arrayEvent");
        	arrayEvent_zh = extras.getStringArray("arrayEvent_zh");
        }

        MaxTableID = 5;
        MaxMinBet = 8;
        isMan = false;
        isWoman = false;
        isEditingMinBet = false;
        isEditingTableID = false;
        
	    Spinner spinnerTableType = (Spinner) findViewById(R.id.spinnerTableType);
	    ArrayAdapter<String> adapterTableType;
	    if (Language.equals("eng")) {
	    	adapterTableType= new ArrayAdapter<String>(this,R.layout.spinner_view, arrayTableType);
	    } else {
	    	adapterTableType= new ArrayAdapter<String>(this,R.layout.spinner_view, arrayTableType_zh);
	    }
	    adapterTableType.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
    	spinnerTableType.setAdapter(adapterTableType);
    	spinnerTableType.setOnItemSelectedListener(new TableTypeOnItemSelectedListener());
    	
		lblMinBet = (TextView)findViewById(R.id.lblMinBet);
		lblTableID = (TextView)findViewById(R.id.lblTableID);
		
    	layoutNumberPad = (LinearLayout)findViewById(R.id.layoutSelectTypeNumberPad);
    	layoutNumberPad.setVisibility(LinearLayout.GONE);
		layoutNumberPad.setX(365);
		MinBetNumberPadY = 905;
		TableIDNumberPadY = 315;
		
		LinearLayout layoutBackground = (LinearLayout)findViewById(R.id.layoutSelectTypeBackground);
		layoutBackground.setOnClickListener(new OnClickListener() {
        	
        	public void onClick(View v) {
    			isEditingMinBet = false;
    			isEditingTableID = false;
    			layoutNumberPad.setVisibility(LinearLayout.GONE);
        	}
		});
		
		ImageButton btnTableIDEdit = (ImageButton)findViewById(R.id.btnTableIDEdit);
        btnTableIDEdit.setOnClickListener(new OnClickListener() {
        	
        	public void onClick(View v) {
    			isEditingMinBet = false;
    			isEditingTableID = false;
        		if (layoutNumberPad.getVisibility()==LinearLayout.GONE || (layoutNumberPad.getVisibility()==LinearLayout.VISIBLE && layoutNumberPad.getY()==MinBetNumberPadY)) {
        			isEditingTableID = true;
        			layoutNumberPad.setY(TableIDNumberPadY);
        			layoutNumberPad.setVisibility(LinearLayout.VISIBLE);
        		} else {
        			layoutNumberPad.setVisibility(LinearLayout.GONE);
        		}
        	}      	
        });
    	
        ImageButton btnMinBetEdit = (ImageButton)findViewById(R.id.btnMinBetEdit);
        btnMinBetEdit.setOnClickListener(new OnClickListener() {
        	
        	public void onClick(View v) {
    			isEditingMinBet = false;
    			isEditingTableID = false;
        		if (layoutNumberPad.getVisibility()==LinearLayout.GONE || (layoutNumberPad.getVisibility()==LinearLayout.VISIBLE && layoutNumberPad.getY()==TableIDNumberPadY)) {
        			isEditingMinBet = true;
        			layoutNumberPad.setY(MinBetNumberPadY);
        			layoutNumberPad.setVisibility(LinearLayout.VISIBLE);
        		} else {
        			layoutNumberPad.setVisibility(LinearLayout.GONE);
        		}
        	}   
        });
        
        Button btnNext = (Button)findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new OnClickListener() {
        	
        	public void onClick(View v) {
            	layoutNumberPad.setVisibility(LinearLayout.GONE);

        		if (lblTableID.getText().length()==0) {
        			String message = getResources().getString(R.string.stringTableIDEmptyMessage);
        			messageShowUp("",message);
        		} else if (!isMan && !isWoman) {
        			String message = getResources().getString(R.string.stringGenderMessage);
        			messageShowUp("",message);
        		} else if (lblMinBet.getText().length()==0) {
        			String message = getResources().getString(R.string.stringMinBetEmptyMessage);
        			messageShowUp("",message);
        		} else {
        			Intent newIntent = new Intent(SelectTypeActivity.this, CountViewActivity.class);
            		if (isMan) {
                		newIntent.putExtra("Gender", "M"); 
            		} else {
            			newIntent.putExtra("Gender", "F");
            		}
            		
            		newIntent.putExtra("StaffID", StaffID);
            		newIntent.putExtra("Location", Location);
            		newIntent.putExtra("SeatLimit", SeatLimit);
            		newIntent.putExtra("TableType", TableType);
            		newIntent.putExtra("MinBet", lblMinBet.getText());
            		newIntent.putExtra("TableID", lblTableID.getText());
            		newIntent.putExtra("Language", Language);
            		newIntent.putExtra("ftpServer", ftpServer);
            		newIntent.putExtra("ftpUsername", ftpUsername);
            		newIntent.putExtra("ftpPassword", ftpPassword);
            		newIntent.putExtra("outputFilePath", outputFilePath);
            		newIntent.putExtra("outputFilePassword", outputFilePassword);
            		
            		newIntent.putExtra("arrayLocation", arrayLocation);
            		newIntent.putExtra("arrayLocation_zh", arrayLocation_zh);
            		newIntent.putExtra("arrayTableType", arrayTableType);
            		newIntent.putExtra("arrayTableType_zh", arrayTableType_zh);
            		newIntent.putExtra("arrayBetAmount", arrayBetAmount);
            		newIntent.putExtra("arrayEvent", arrayEvent);
            		newIntent.putExtra("arrayEvent_zh", arrayEvent_zh);
                	startActivity(newIntent);
        		}
        	}
        });
        
        ImageButton btnDealerMan = (ImageButton)findViewById(R.id.btnDealerMan);
        btnDealerMan.setOnClickListener(new OnClickListener() {
        	
        	public void onClick(View v) {
        		isMan = true;
        		isWoman = false;
            	layoutNumberPad.setVisibility(LinearLayout.GONE);
        		ImageButton btnDealerMan = (ImageButton)findViewById(R.id.btnDealerMan);
        		btnDealerMan.setImageResource(R.drawable.tableowl_item_05);
        		ImageButton btnDealerWoman = (ImageButton)findViewById(R.id.btnDealerWoman);
        		btnDealerWoman.setImageResource(R.drawable.tableowl_item_08);
        	}      	
        });
        
        ImageButton btnDealerWoman = (ImageButton)findViewById(R.id.btnDealerWoman);
        btnDealerWoman.setOnClickListener(new OnClickListener() {
        	
        	public void onClick(View v) {
        		isMan = false;
        		isWoman = true;
            	layoutNumberPad.setVisibility(LinearLayout.GONE);
        		ImageButton btnDealerMan = (ImageButton)findViewById(R.id.btnDealerMan);
        		btnDealerMan.setImageResource(R.drawable.tableowl_item_07);
        		ImageButton btnDealerWoman = (ImageButton)findViewById(R.id.btnDealerWoman);
        		btnDealerWoman.setImageResource(R.drawable.tableowl_item_06);
        	}      	
        });
        
        for (int i=0; i<11; i++) {
    		final String number;
    		if (i==10) {
    			number = "00";
    		} else {
    			number = String.valueOf(i);
    		}
            int resID = getResources().getIdentifier("btnSelectTypeNumber"+number, "id", "com.motix.tableowl");
        	Button btnNumber = (Button)findViewById(resID);
        	btnNumber.setOnClickListener(new OnClickListener() {
            	
            	public void onClick(View v) {
            		clickNumberPad(number);
            	}
        	});
        }
        
        Button btnSelectTypeNumberBack = (Button)findViewById(R.id.btnSelectTypeNumberBack);
        btnSelectTypeNumberBack.setOnClickListener(new OnClickListener() {
        	
        	public void onClick(View v) {
            	if (isEditingTableID) {
            		if (lblTableID.getText().length()!=0) {
            			String myString = (String) lblTableID.getText().subSequence(0, lblTableID.getText().length()-1);
            			lblTableID.setText(myString);
            		}
            	} else if (isEditingMinBet) {
            		if (lblMinBet.getText().length()!=0) {
            			String myString = (String) lblMinBet.getText().subSequence(0, lblMinBet.getText().length()-1);
            			lblMinBet.setText(myString);
            		}
            	}
        	}
        });
    }
    
    private void messageShowUp(String title, String message) {
    	AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
    	MyAlertDialog.setTitle(title);
    	MyAlertDialog.setMessage(message);
    	MyAlertDialog.show();
    }
    
    public void clickNumberPad(String i) {
    	if (isEditingTableID) {
    		if (lblTableID.getText().length()<MaxTableID) {
    			String myString = lblTableID.getText() + i;
    			lblTableID.setText(myString);
    		}
    	} else if (isEditingMinBet) {
      		if (lblMinBet.getText().length()<MaxMinBet) {
      			String myString = "";
      			if (lblMinBet.getText().length()==0) {
      				if (!i.equals("0") && !i.equals("00")) {
      					myString = i;
      				}
      			} else {
      	  			myString = lblMinBet.getText() + i;
      			}
      			lblMinBet.setText(myString);
    		}
    	}
    }
        
	public class TableTypeOnItemSelectedListener implements OnItemSelectedListener {
		
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	    	SeatLimit = arrayTableType_SeatLimit[pos];
		    if (Language.equals("eng")) {
		    	TableType = arrayTableType[pos];
		    } else {
		    	TableType = arrayTableType_zh[pos];
		    }
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
	}
}