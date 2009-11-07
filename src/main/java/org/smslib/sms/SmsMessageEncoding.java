package org.smslib.sms;

/** Holds values representing message encodings. */
public enum SmsMessageEncoding {
	/** 7-Bit (default GSM alphabet) encoding. */
	GSM_7BIT,
	/** 8-Bit encoding. */
	BINARY_8BIT,
	/** UCS2 (Unicode) encoding. Use this for Far-East languages. */
	UCS2,
	/** Custom encoding. When you set this value, you should also set the DCS (Data Coding Scheme) value yourself! */
	EncCustom;
}