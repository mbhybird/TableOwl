package com.motix.tableowl;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class TableOwlActivity extends Activity {
	int MaxStaffID;
	Boolean inputMode, updateFail;
	TextView lblLoginID;
    ProgressDialog downloadProgressDialog, updateProgressDialog;
    String apkVersion, stringSuccessFileUpload, stringFailFileUpload;
	String Location, Language, ftpServer, ftpUsername, ftpPassword, outputFilePath, outputFilePassword;
	LinearLayout layoutLoginNumberPad;
	String[] arrayLocation, arrayLocation_zh, arrayTableType, arrayTableType_zh, arrayTableType_SeatLimit, arrayBetAmount, arrayEvent, arrayEvent_zh;
    /** Called when the activity is first created. */
	
    public void onCreate(Bundle savedInstanceState) {
    	StrictMode.enableDefaults();
        super.onCreate(savedInstanceState);
        
        Log.d("Jerry",""+Locale.getDefault().toString());
    	Language = "eng";
        if (Locale.getDefault().toString().indexOf("zh")!=-1) {
        	Language = "zh";
        }
    	
        setContentView(R.layout.main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        apkVersion = "V1.1.5";
        MaxStaffID = 6;
        updatingSetting();
        
        layoutLoginNumberPad = (LinearLayout)findViewById(R.id.layoutLoginNumberPad);
        layoutLoginNumberPad.setVisibility(LinearLayout.GONE);
        layoutLoginNumberPad.setX(280);
        layoutLoginNumberPad.setY(590);
        
        lblLoginID = (TextView)findViewById(R.id.lblLoginID);
        
		LinearLayout layoutLoginBackground = (LinearLayout)findViewById(R.id.layoutLoginBackground);
		layoutLoginBackground.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		layoutLoginNumberPad.setVisibility(LinearLayout.GONE);
        	}
		});
        
        ImageButton btnLoginIDEdit = (ImageButton)findViewById(R.id.btnLoginIDEdit);
        btnLoginIDEdit.setOnClickListener(new OnClickListener() {
        	
        	public void onClick(View v) {
        		if (layoutLoginNumberPad.getVisibility()==LinearLayout.GONE) {
        			layoutLoginNumberPad.setVisibility(LinearLayout.VISIBLE);
        		} else {
        			layoutLoginNumberPad.setVisibility(LinearLayout.GONE);
        		}
        	}
        });
        
        ImageButton btnUpdate = (ImageButton)findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if (!isOnline()) {
        			String message = getResources().getString(R.string.stringConnectionError);
        			messageShowUp("",message);
        		} else {
        			clickUpdate();
        		}
        	}
        });
        
        ImageButton btnUploadData = (ImageButton)findViewById(R.id.btnUploadData);
        btnUploadData.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if (!isOnline()) {
        			String message = getResources().getString(R.string.stringConnectionError);
        			messageShowUp("",message);
        		} else if (inputMode) {
        			String message = getResources().getString(R.string.stringMissSetting);
        			messageShowUp("",message);
        		} else {
        			uploadData();
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
            int resID = getResources().getIdentifier("btnLoginNumber"+number, "id", "com.motix.TableOwl");
        	Button btnNumber = (Button)findViewById(resID);
        	btnNumber.setOnClickListener(new OnClickListener() {
            	
            	public void onClick(View v) {
            		clickNumberPad(number);
            	}
        	});
        }
        
        Button btnCountViewNumberBack = (Button)findViewById(R.id.btnLoginNumberBack);
        btnCountViewNumberBack.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
            	if (lblLoginID.getText().length()!=0) {
            		String myString = (String) lblLoginID.getText().subSequence(0, lblLoginID.getText().length()-1);
            		lblLoginID.setText(myString);
            	}
        	}
        });
        
        ImageButton btnLogin = (ImageButton)findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		if (inputMode) {
        			String message = getResources().getString(R.string.stringMissSetting);
        			messageShowUp("",message);
        		} else if (lblLoginID.getText().length()!=0) {
                	layoutLoginNumberPad.setVisibility(LinearLayout.GONE);
        			Intent newIntent = new Intent();
        			newIntent.setClassName("com.motix.TableOwl", "com.motix.TableOwl.SelectTypeActivity");
            		newIntent.putExtra("Location", Location); 
            		newIntent.putExtra("StaffID", lblLoginID.getText());
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
            		newIntent.putExtra("arrayTableType_SeatLimit", arrayTableType_SeatLimit);
            		newIntent.putExtra("arrayBetAmount", arrayBetAmount);
            		newIntent.putExtra("arrayEvent", arrayEvent);
            		newIntent.putExtra("arrayEvent_zh", arrayEvent_zh);
        			startActivity(newIntent);
        		} else {
        			String message = getResources().getString(R.string.stringStaffIDEmptyMessage);
        			messageShowUp("",message);
        		}
        	}
        });
    }

    public void clickNumberPad(String i) {
		if (lblLoginID.getText().length()<MaxStaffID) {
			String myString = lblLoginID.getText() + i;
			lblLoginID.setText(myString);
		}
    }
    
	public class LocationOnItemSelectedListener implements OnItemSelectedListener {
	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		    if (Language.equals("eng")) {
		    	Location = arrayLocation[pos];
		    } else {
		    	Location = arrayLocation_zh[pos];
		    }
		}
		public void onNothingSelected(AdapterView<?> arg0) {}
	}

	public void clickUpdate() {
		if (inputMode) {
			Context mContext = getApplicationContext();
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.dialog_login, null);
			EditText lblServer = (EditText)layout.findViewById(R.id.lblServer);
			lblServer.setText(ftpServer);
			EditText lblFolder = (EditText)layout.findViewById(R.id.lblFolder);
			lblFolder.setText("Strategic Analysis");
			EditText lblUserName = (EditText)layout.findViewById(R.id.lblUserName);
			lblUserName.setText("VEN-SA-TableOwl");
			//TODO
//			lblFolder.setText("Slot");
//			lblUserName.setText("VEN-SL-Motix");
//			EditText lblPassword = (EditText)layout.findViewById(R.id.lblPassword);
//			lblPassword.setText("4hd7s9cQ");
			
		    AlertDialog.Builder alert = new AlertDialog.Builder(this);
	        alert.setTitle("FTP");
	        alert.setView(layout);
	        String connect = getResources().getString(R.string.stringConnect);
	        alert.setPositiveButton(connect, new DialogInterface.OnClickListener() {
	        	public void onClick(DialogInterface dialog, int whichButton) {
	    			EditText lblServer = (EditText)layout.findViewById(R.id.lblServer);
	        		ftpServer = lblServer.getText().toString();
	    			EditText lblFolder = (EditText)layout.findViewById(R.id.lblFolder);
	        		String ftpSettingFolder = lblFolder.getText().toString();
	    			EditText lblUserName = (EditText)layout.findViewById(R.id.lblUserName);
    				ftpUsername = lblUserName.getText().toString();
	    			EditText lblPassword = (EditText)layout.findViewById(R.id.lblPassword);
    				ftpPassword = lblPassword.getText().toString();
    				
    				saveFTPSettingFolder(ftpSettingFolder);
    				downloadFile();
    				
    				if (updateFail) {
    					String message = getResources().getString(R.string.stringUpdateSettingFailed);
	    				messageShowUp("",message);
    				} else {
    					String message = getResources().getString(R.string.stringUpdateSettingSuccessful);
	    				downloadedMessageShowUp("",message);
    				}
	        	}
	        });
	        alert.show();
		} else {
			downloadingMessage();
			new Thread() {
				public void run() {
					downloadFile();
    				if (updateFail) {
    					Log.d("Jerry","updateFail");
    					String message = getResources().getString(R.string.stringUpdateSettingFailed);
	    				messageShowUp("",message);
    				} else {
    					Log.d("Jerry","!updateFail");
    					String message = getResources().getString(R.string.stringUpdateSettingSuccessful);
	    				downloadedMessageShowUp("",message);
    				}
	            	downloadProgressDialog.cancel();
	            }
	    	}.start();
		}
	}
	
	public void saveFTPSettingFolder(String s) {
		Log.d("Jerry","saveFTPSettingFolder");
		try {
    	    File tempFile = new File(new ContextWrapper(this).getDir("", Context.MODE_PRIVATE),"ftpSettingFolder.txt"); 
			FileOutputStream fos = new FileOutputStream(tempFile);
			fos.write(s.getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    private void messageShowUp(String title, String message) {
    	final AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
    	MyAlertDialog.setTitle(title);
    	MyAlertDialog.setMessage(message);
    	this.runOnUiThread(new Runnable(){
    		public void run(){
    			MyAlertDialog.show();
    		}
    	});
    }
	
    private void downloadingMessage() {
		downloadProgressDialog = new ProgressDialog(this);
        String stringUploadingMessage = getResources().getString(R.string.stringDownloadingMessage);
        downloadProgressDialog.setMessage(stringUploadingMessage+"...");
        downloadProgressDialog.setCancelable(false);
        downloadProgressDialog.show();
	}
	
    private void downloadedMessageShowUp(String title, String message) {
    	final AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
    	MyAlertDialog.setTitle(title);
    	MyAlertDialog.setMessage(message);
    	DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
            	updatingSetting();
			}
    	};
    	String ok = getResources().getString(R.string.stringFinishOkay);
    	MyAlertDialog.setPositiveButton(ok, okClick);
    	this.runOnUiThread(new Runnable(){
    		public void run(){
    			MyAlertDialog.show();
    		}
    	});
	}
	
	public void downloadFile() {
		Log.d("Jerry","downloadFile");
    	FTPClient ftpClient = new FTPClient();
    	try {
    		File settingFile = new File(new ContextWrapper(this).getDir("", Context.MODE_PRIVATE),"ftpSettingFolder.txt");
    		InputStream settingIS = new FileInputStream(settingFile);
			BufferedReader settingBR = new BufferedReader(new InputStreamReader(settingIS));
			String settingFolder = settingBR.readLine();
    		
    	    ftpClient.connect(ftpServer);
    	    ftpClient.login(ftpUsername, ftpPassword);
    	    ftpClient.enterLocalPassiveMode();
    	    if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
    	    	Log.d("Jerry","FTP getReplyCode");
    	    	updateFail = false;
    	    	File file = new File(new ContextWrapper(this).getDir("", Context.MODE_PRIVATE),"Config.txt");
    	    	File tempFile = new File(new ContextWrapper(this).getDir("", Context.MODE_PRIVATE),"Config_tmp.txt");
    			FileOutputStream out = new FileOutputStream(tempFile);
    	    	boolean finish = ftpClient.retrieveFile(settingFolder+"/Config.txt", out);
    	    	if (finish) {
    	    		tempFile.renameTo(file);
    	    	} else {
        	    	updateFail = true;
        	    	Log.d("Jerry","update not finish");
    	    	}
	    		out.close();
	    		settingIS.close();
    	    } else {
    	    	updateFail = true;
    	    	Log.d("Exception","FTP not Reply");
    	    }
	    	ftpClient.logout();
	    	ftpClient.disconnect();
    	} catch (Exception e) {
    		updateFail = true;
	        e.printStackTrace();
	        Log.d("Exception","FTP get Exception"+e.getMessage());
    	}
    	Log.d("Jerry","downloadFile Finish");
	}

    public void updatingSetting() {
		Log.d("Jerry","Reading Setting");
    	InputStream instream;
    	File file = new File(new ContextWrapper(this).getDir("", Context.MODE_PRIVATE),"Config.txt");
		try {
			if (file.exists()) {
				instream = new FileInputStream(file);
			} else {
				AssetManager assetManager = getAssets();
				instream = assetManager.open("Config.txt");
			}
    		BufferedReader buffreader = new BufferedReader(new InputStreamReader(instream));
    		String line;
   			while ((line = buffreader.readLine()) != null) {
   				if (line.toLowerCase().indexOf("//Version".toLowerCase()) >= 0) {
					String Version = buffreader.readLine();
   			        TextView lblVersion = (TextView)findViewById(R.id.lblVersion);
   			        lblVersion.setText(apkVersion+"("+Version+")");
   				} else if (line.toLowerCase().indexOf("//Location".toLowerCase()) >= 0) {
   					String data = buffreader.readLine();
   					if (line.toLowerCase().indexOf("zh") >= 0) {
        				arrayLocation_zh = data.split(",");
    				} else {
        				arrayLocation = data.split(",");
    				}
    			} else if (line.toLowerCase().indexOf("//TableType".toLowerCase()) >= 0) {
    				String data = buffreader.readLine();
    				if (line.toLowerCase().indexOf("SeatLimit".toLowerCase()) >= 0) {
    					arrayTableType_SeatLimit = data.split(",");
    				} else if (line.toLowerCase().indexOf("zh") >= 0) {
   						arrayTableType_zh = data.split(",");
   					} else {
   						arrayTableType = data.split(",");
   					}
    			} else if (line.toLowerCase().indexOf("//BetAmount".toLowerCase()) >= 0) {
    				String data = buffreader.readLine();
    				data = data.replace(" ", "");
    				arrayBetAmount = data.split(",");
    			} else if (line.toLowerCase().indexOf("//Event".toLowerCase()) >= 0) {
    				String data = buffreader.readLine();
    				if (line.toLowerCase().indexOf("zh") >= 0) {
   						arrayEvent_zh = data.split(",");
   					} else {
   						arrayEvent = data.split(",");
   					}
    			} else if (line.toLowerCase().indexOf("//FTP".toLowerCase()) >= 0) {
    				String data = buffreader.readLine();
    				String [] ftp = data.split(",");
    		        inputMode = true;
    				ftpServer = ftp[0];
    				if (ftp.length>1) {
    			        inputMode = false;
    					ftpUsername = ftp[1];
   						ftpPassword = ftp[2];
    				}
    			} else if (line.toLowerCase().indexOf("//OutputFilePath".toLowerCase()) >= 0) {
    				outputFilePath = buffreader.readLine();
    				if (!outputFilePath.substring(outputFilePath.length()-1).equals("/")) {
    					outputFilePath +="/";
    				}
    			} else if (line.toLowerCase().indexOf("//OutputFilePassword".toLowerCase()) >= 0) {
    				outputFilePassword = buffreader.readLine();
    			}
   			}
    		instream.close();
    	} catch (Exception e) {
          	e.printStackTrace();
    	} 
		
		String errorConfigFile = "";
		if (arrayLocation.length!=arrayLocation_zh.length) {
			errorConfigFile+="Location";
		} else {
			for (int i=0; i<arrayLocation.length; i++) {
				if (arrayLocation[i].length()==0) {
					errorConfigFile +="Location";
					break;
				}
				if (arrayLocation_zh[i].length()==0) {
					errorConfigFile +="Location";
					break;
				}
			}
		}
		if (arrayTableType.length!=arrayTableType_zh.length) {
			if (errorConfigFile.length()>0) { errorConfigFile+="\n"; }
			errorConfigFile+="TableType";
		} else {
			if (arrayTableType.length!=arrayTableType_SeatLimit.length) {
				if (errorConfigFile.length()>0) { errorConfigFile+="\n"; }
				errorConfigFile+="TableType_SeatLimit";
			} else {
				for (int i=0; i<arrayTableType.length; i++) {
					if (arrayTableType[i].length()==0) {
						if (errorConfigFile.length()>0) { errorConfigFile+="\n"; }
						errorConfigFile +="TableType";
						break;
					}
					if (arrayTableType_zh[i].length()==0) {
						if (errorConfigFile.length()>0) { errorConfigFile+="\n"; }
						errorConfigFile +="TableType";
						break;
					}
					if (arrayTableType_SeatLimit[i].length()==0) {
						if (errorConfigFile.length()>0) { errorConfigFile+="\n"; }
						errorConfigFile +="TableType_SeatLimit";
						break;
					}
				}
			}
		}
		if (arrayEvent.length!=arrayEvent_zh.length) {
			if (errorConfigFile.length()>0) { errorConfigFile+="\n"; } 
			errorConfigFile+="Event";
		} else {
			for (int i=0; i<arrayEvent.length; i++) {
				if (arrayEvent[i].length()==0) {
					if (errorConfigFile.length()>0) { errorConfigFile+="\n"; }
					errorConfigFile +="Event";
					break;
				}
				if (arrayEvent_zh[i].length()==0) {
					if (errorConfigFile.length()>0) { errorConfigFile+="\n"; }
					errorConfigFile +="Event";
					break;
				}
			}
		}
		if (errorConfigFile.length()>0) { messageShowUp("Config file error.",errorConfigFile); }
		
	    Spinner spinnerLocation = (Spinner) findViewById(R.id.spinnerLocation);
	    ArrayAdapter<String> adapterLocation;
	    if (Language.equals("eng")) {
	    	adapterLocation= new ArrayAdapter<String>(this,R.layout.spinner_view, arrayLocation);
	    } else {
	    	adapterLocation= new ArrayAdapter<String>(this,R.layout.spinner_view, arrayLocation_zh);
	    }
	    adapterLocation.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
    	spinnerLocation.setAdapter(adapterLocation);
    	spinnerLocation.setOnItemSelectedListener(new LocationOnItemSelectedListener());
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
    	    b64os.close();
    	    isEncrypt.close();
        } catch (Exception e) {
	        e.printStackTrace();
	    	Log.d("Jerry","Encrypt error"+e.getMessage());
	    }
    	
    	FTPClient ftpClient = new FTPClient();
    	try {
    	    ftpClient.connect(ftpServer);
    	    ftpClient.login(ftpUsername, ftpPassword);
    	    if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
    	    	Log.d("Jerry","FTP getReplyCode"+to.getAbsolutePath());
        	    BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(to));
            	ftpClient.enterLocalPassiveMode();
            	boolean finish = ftpClient.storeFile(outputFilePath+to.getName(), buffIn);
        	    if (finish) {
        	    	file.delete();
        	    	Log.d("Jerry","upload finish");
        	    	stringSuccessFileUpload += fileName+"\n";
        	    } else {
        	    	Log.d("Jerry","upload not finish");
        	    	stringFailFileUpload += fileName+"\n";
        	    }
        	    buffIn.close();
    	    } else {
    	    	Log.d("Jerry","FTP not Reply");
    	    	stringFailFileUpload += fileName+"\n";
    	    }
        	ftpClient.logout();
        	ftpClient.disconnect();
    	} catch (Exception e) {
	        e.printStackTrace();
	        Log.d("Exception","FTP get Exception"+e.getMessage());
    		stringFailFileUpload += fileName+"\n";
    	}
    }
    
    public void uploadData() {
    	updateProgressDialog = new ProgressDialog(this);
        String stringUploadingMessage = getResources().getString(R.string.stringUploadingMessage);
        updateProgressDialog.setMessage(stringUploadingMessage+"...");
        updateProgressDialog.setCancelable(false);
        updateProgressDialog.show();
    	 new Thread() {
             public void run() {
             	stringSuccessFileUpload = "";
            	stringFailFileUpload = "";
            	File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/TableOwl");
            	if (!dir.exists()) dir.mkdirs();
            	File toDir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/TableOwlSent");
            	if (!toDir.exists()) toDir.mkdirs();
        		File[] files = dir.listFiles();
            	for (int i=0; i<files.length; i++) {
            		encryptAndUploadFTP(dir, toDir, files[i].getName());
            	}
            	
            	String message = getResources().getString(R.string.stringUploadNoFile);
            	if (stringSuccessFileUpload.length()>0) {
            		message = stringSuccessFileUpload+getResources().getString(R.string.stringUploadSuccessful);
            		if (stringFailFileUpload.length()>0) message += "\n\n"+stringFailFileUpload+getResources().getString(R.string.stringUploadFailed);
            	} else if (stringFailFileUpload.length()>0) {
            		message = stringFailFileUpload+getResources().getString(R.string.stringUploadFailed);
            	}
            	messageShowUp("",message);
            	updateProgressDialog.cancel();
             }
    	 }.start();
    }
    
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}