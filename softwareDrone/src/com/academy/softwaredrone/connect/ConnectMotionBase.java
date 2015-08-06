package com.academy.softwaredrone.connect;

import java.io.IOException;

import android.content.Context;
import android.hardware.usb.UsbManager;

import com.academy.softwaredrone.R;
import com.android.util.Logging;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

public class ConnectMotionBase {
	private static UsbManager usbManager;
	private static UsbSerialDriver device;
	public final static String TAG = ConnectMotionBase.class.getSimpleName();
	public static boolean stateUSB = false;
	private static int motorLeft = 0;
	private static int motorRight = 0;

	private static int pwmBtnMotorLeft; // left PWM constant value from settings
	// (���������� �������� ��� ��� ������
	// ��������� �� ��������)
	private static int pwmBtnMotorRight; // right PWM constant value from settings
	// (���������� �������� ��� ��� �������
	// ��������� �� ��������)
	private static String commandLeft; // command symbol for left motor from settings
	// (������ ������� ������ ��������� �� ��������)
	private static String commandRight; // command symbol for right motor from settings
	// (������ ������� ������� ��������� ��
	// ��������)
	private static String commandHorn; // command symbol for optional command (for

	// example - horn) from settings (������ �������
	// ��� ���. ������ (�������� ������) ��
	// ��������)
	

	public static void onCloseConnectToMotionBase() {
		// check if the device is already closed
		if (device != null) {
			try {
				device.close();
			} catch (IOException e) {
				// we couldn't close the device, but there's nothing we can do
				// about it!
			}
			// remove the reference to the device
			stateUSB = false;
			device = null;
		}
	}

	public static void onResumeDevice(Context mContext) {
		// get a USB to Serial device object
		usbManager = (UsbManager) mContext
				.getSystemService(Context.USB_SERVICE);
		// Get Server from Android.
		pwmBtnMotorLeft = Integer.parseInt((String) mContext.getResources()
				.getText(R.string.default_pwmBtnMotorLeft));
		pwmBtnMotorRight = Integer.parseInt((String) mContext.getResources()
				.getText(R.string.default_pwmBtnMotorRight));
		commandLeft = (String) mContext.getResources().getText(
				R.string.default_commandLeft);
		commandRight = (String) mContext.getResources().getText(
				R.string.default_commandRight);
		commandHorn = (String) mContext.getResources().getText(
				R.string.default_commandHorn);
		device = UsbSerialProber.acquire(usbManager);
		if (device == null) {
			// there is no device connected!
			Logging.doLog(TAG, "No USB serial device connected.",
					"No USB serial device connected.");
		} else {
			try {
				// open the device
				Logging.doLog(TAG, "	device.open()", "	device.open()");

				device.open();
				// set the communication speed
				device.setBaudRate(115200); // make sure this matches your
				stateUSB = true;
				// device's setting!
			} catch (IOException err) {
				Logging.doLog(TAG,
						"Error setting up USB device: " + err.getMessage()
								+ err,
						"Error setting up USB device: " + err.getMessage());
				try {
					// something failed, so try closing the device
					device.close();
				} catch (IOException err2) {
					// couldn't close, but there's nothing more to do!
				}
				stateUSB = false;
				device = null;
				return;
			}
		}
	}

	public static void sendDataToMotionBase(String message) {
		byte[] dataToSend = message.getBytes();
		Logging.doLog(TAG, "Send data: " + message, "Send data: " + message);

		// remove spurious line endings from color bytes so the serial device
		// doesn't get confused
		for (int i = 0; i < dataToSend.length - 1; i++) {
			if (dataToSend[i] == 0x0A) {
				dataToSend[i] = 0x0B;
				Logging.doLog(TAG, "Send data: " + dataToSend[i], "Send data: "
						+ dataToSend[i]);

			}
		}
		// send the color to the serial device
		if (device != null) {
			try {
				Logging.doLog(TAG, "device.write", "device.write");
				device.write(dataToSend, 500);

			} catch (IOException e) {
				Logging.doLog(TAG, "couldn't write bytes to serial device",
						"couldn't write bytes to serial device");
			}
		} else {
			Logging.doLog(TAG, "device = null", "device = null");
		}
	}

	public static void motionLeft() {
		motorLeft = pwmBtnMotorLeft;
		motorRight = pwmBtnMotorRight;
		sendDataToMotionBase(String.valueOf(commandLeft + motorLeft + "\r"
				+ commandRight + motorRight + "\r"));

	}

	public static void motionForward() {
		motorLeft = -pwmBtnMotorLeft;
		motorRight = pwmBtnMotorRight;
		sendDataToMotionBase(String.valueOf(commandLeft + motorLeft + "\r"
				+ commandRight + motorRight + "\r"));

	}

	public static void motionRight() {
		motorLeft = -pwmBtnMotorLeft;
		motorRight = -pwmBtnMotorRight;
		sendDataToMotionBase(String.valueOf(commandLeft + motorLeft + "\r"
				+ commandRight + motorRight + "\r"));
	}

	public static void motionBackward() {
		motorLeft = pwmBtnMotorLeft;
		motorRight = -pwmBtnMotorRight;

		sendDataToMotionBase(String.valueOf(commandLeft + motorLeft + "\r"
				+ commandRight + motorRight + "\r"));
	}

	public static void stopMotion() {
		motorLeft = 0;
		motorRight = 0;
		sendDataToMotionBase(String.valueOf(commandLeft + motorLeft + "\r"
				+ commandRight + motorRight + "\r"));
	}

	public static void loadPref() {

		pwmBtnMotorLeft = 255;
		pwmBtnMotorRight = 255;
		commandLeft = "L";
		commandRight = "R";
		commandHorn = "H";
	}

	public static void testConnect(int i) {
		if (i == 1)
			sendDataToMotionBase(String.valueOf(commandHorn + "1\r"));
		else
			sendDataToMotionBase(String.valueOf(commandHorn + "0\r"));
	}
}
