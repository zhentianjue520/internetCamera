package com.itheima.ipcamera83;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlPtzCmd;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.IRegisterIOTCListener;
import com.tutk.IOTC.Monitor;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlGetSupportStreamReq;

public class MonitorActivity extends Activity {

	/** 监视器，用于显示网络摄像头的监视画面 */
	private Monitor monitor;
	private TextView tv_connect_state;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String connectState = "--";
			switch (msg.what) {
			case Camera.CONNECTION_STATE_CONNECT_FAILED:
				// 连接失败
				connectState = "连接失败";
				break;
			case Camera.CONNECTION_STATE_CONNECTED:
				// 连接成功
				connectState = "连接成功";
				// 参数1：指定要监视哪个摄像头
				// 参数2：指定AV通道
				monitor.attachCamera(camera, Camera.DEFAULT_AV_CHANNEL);
				//  参数2：指定AV通道
				// 参数2： 是否可以拍照
				camera.startShow(Camera.DEFAULT_AV_CHANNEL, true);
				break;
			case Camera.CONNECTION_STATE_CONNECTING:
				// 正在连接
				connectState = "正在连接";
				break;
			case Camera.CONNECTION_STATE_DISCONNECTED:
				// 连接中断
				connectState = "连接中断";
				break;
			case Camera.CONNECTION_STATE_TIMEOUT:
				// 连接超时
				connectState = "连接超时";
				break;
			case Camera.CONNECTION_STATE_UNSUPPORTED:
				// 不支持连接
				connectState = "不支持连接";
				break;
			case Camera.CONNECTION_STATE_WRONG_PASSWORD:
				// 连接密码错误
				connectState = "连接密码错误";
				break;
			}
			tv_connect_state.setText("连接状态：" + connectState);
		};
	};
	private Camera camera;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitor);
		monitor = (Monitor) findViewById(R.id.monitor);
		tv_connect_state = (TextView) findViewById(R.id.tv_connect_state);
		connectCamera();
	}
	
	private void connectCamera() {
		String name = getIntent().getStringExtra(Keys.NAME);
		String uid = getIntent().getStringExtra(Keys.UID);
		String password = getIntent().getStringExtra(Keys.PASSWORD);
		
		Camera.init();	// 初始化网络摄像头，初始化AV(Audio、Video)通道
		
		camera = new Camera();
		camera.connect(uid);			// 通过UID连接网络摄像头
		// 开启网络摄像头
		// 参数1：AV通道，其它用到AV通道的地方都必须用同一个
		// 参数2：名称
		// 参数3：网络摄像头的密码
		camera.start(Camera.DEFAULT_AV_CHANNEL, name, password);
		
		camera.registerIOTCListener(mIOTCListener);	// 注射网络摄像头控制的监听器
		
		// 参数1：AV通道
		// 参数2：命令的类型
		// 参数3：具体的命令
		int commandType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ;
		byte[] commands = SMsgAVIoctrlGetSupportStreamReq.parseContent();
		camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, commandType, commands);
	}
	
	public void sendOrietationCommand(View v) {
		switch (v.getId()) {
		case R.id.btn_top:		// 向上移动摄像头
			sendOrietationCommand(AVIOCTRLDEFs.AVIOCTRL_PTZ_UP);
			break;
		case R.id.btn_bottom:	// 向下移动摄像头
			sendOrietationCommand(AVIOCTRLDEFs.AVIOCTRL_PTZ_DOWN);
			break;
		case R.id.btn_left:		// 向左移动摄像头
			sendOrietationCommand(AVIOCTRLDEFs.AVIOCTRL_PTZ_LEFT);
			break;
		case R.id.btn_right:	// 向右移动摄像头
			sendOrietationCommand(AVIOCTRLDEFs.AVIOCTRL_PTZ_RIGHT);
			break;
		}
	}

	/** 发送控制指令 */
	private void sendOrietationCommand(int orietation) {
		int commandType = AVIOCTRLDEFs.IOTYPE_USER_IPCAM_PTZ_COMMAND;
		// 参数1：移动方向
		// 参数2：移动速度
		// 参数3：多少触摸点
		// 参数4：移动距离
		// 参数5：辅助设备
		// 参数6：AV通道
		byte[] commands = SMsgAVIoctrlPtzCmd.parseContent((byte) orietation, (byte) 0, (byte) 0, 
				(byte) 0, (byte) 0, (byte)Camera.DEFAULT_AV_CHANNEL);
		
		// 参数1：AV通道
		// 参数2：命令的类型
		// 参数3：具体的命令
		camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, commandType, commands);
	}
	
	IRegisterIOTCListener mIOTCListener = new IRegisterIOTCListener() {
		
		// 接收会话的反馈信息 
		@Override
		public void receiveSessionInfo(Camera arg0, int arg1) {
			
		}
		
		// 接收控制指令的反馈信息
		@Override
		public void receiveIOCtrlData(Camera arg0, int arg1, int arg2, byte[] arg3) {
			
		}
		
		// 接收画面的反馈信息
		@Override
		public void receiveFrameInfo(Camera arg0, int arg1, long arg2, int arg3, int arg4, int arg5, int arg6) {
		}
		
		// 接收监控画面截图信息
		@Override
		public void receiveFrameData(Camera arg0, int arg1, Bitmap arg2) {
		}
		
		// 接收渠道信息（获取连接状态)
		@Override
		public void receiveChannelInfo(Camera camera, int avChannel, int connectState) {
			handler.sendEmptyMessage(connectState);
		}
	};
	
	protected void onDestroy() {
		super.onDestroy();
		releaseCamera();
	}

	private void releaseCamera() {
		camera.stopShow(Camera.DEFAULT_AV_CHANNEL);	// 停止显示控制画面
		monitor.deattachCamera();	// 解绑摄像头
		camera.unregisterIOTCListener(mIOTCListener);	// 解除监听器
		camera.stop(Camera.DEFAULT_AV_CHANNEL);	// 停止摄像头
		camera.disconnect();	// 断开连接
		
	};
}
