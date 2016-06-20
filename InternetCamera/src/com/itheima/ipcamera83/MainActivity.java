package com.itheima.ipcamera83;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	private EditText et_name;
	private EditText et_uid;
	private EditText et_password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		et_name = (EditText) findViewById(R.id.et_name);
		et_uid = (EditText) findViewById(R.id.et_uid);
		et_password = (EditText) findViewById(R.id.et_password);
	}
	
	public void startConnect(View v ) {
		String name = et_name.getText().toString();
		String uid = et_uid.getText().toString();
		String password = et_password.getText().toString();
		
		if(checkData(name, uid, password) ) {
			// 开始第二个Activity连接网络摄像头
			Intent intent = new Intent(this, MonitorActivity.class);
			intent.putExtra(Keys.NAME, name);
			intent.putExtra(Keys.UID, uid);
			intent.putExtra(Keys.PASSWORD, password);
			startActivity(intent);
		}
	}

	private boolean checkData(String name, String uid, String password) {
		if (TextUtils.isEmpty(name)) {
			showToast("请输入名称");
		} else if (TextUtils.isEmpty(uid)) {
			showToast("请输入UID");
		} else if (uid.length() != 20) {
			showToast("UID长度必须是20位的");
		} else if (TextUtils.isEmpty(password)) {
			showToast("请输入密码");
		} else {
			return true;
		}
		return false;
	}

	private void showToast(String string) {
		Toast toast = Toast.makeText(this, string, 0);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

}
