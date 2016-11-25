package com.quanta.hcbiapi;

import java.nio.ByteBuffer;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.quanta.hcbiapi.Gpio.Gpi;
import com.quanta.hcbiapi.Gpio.Level;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Class to control IR MCU
 */
public class Ir extends Tty{
	private static final String TAG = "Ir";
	private static final byte COMMAND_STOP_IR 			= 0x00;
	private static final byte COMMAND_GET_REVISION		= 0x09;
	private static final byte COMMAND_STANDBY			= 0x0B;
	private static final byte COMMAND_WAKE_UP			= 0x0C;
	private static final byte COMMAND_GET_ID			= 0x23;
	private static final byte COMMAND_LEARN_IR_SHORT	= 0x24;
	private static final byte COMMAND_TX_LEARN_IR		= 0x25;
	private static final byte COMMAND_TX_IR				= 0x26;
	private static final byte COMMAND_LEARN_IR_LONG		= 0x27;
	private static final byte COMMAND_GET_IR_SIGNATURE	= 0x30;
	private static final byte COMMAND_UPGRADE_FW		= 0x40;
	private static final byte COMMAND_RX				= (byte) 0xCD;
	
	private static final byte STATUS_OK		= 0x30;
	private static final byte STATUS_FAIL	= 0x40;
	
	private static final byte MODE_NORMAL	= 0;
	private static final byte MODE_RX		= 1;
	
	private HashMap<Integer, byte[]> keyDataMap;
	private Handler rxHandler;
	
	int mode;
	byte []ttyReadBuf;
	int ttyReadRet;
	Thread readThread;
	
	private void dumpBuf(byte[] b) {
		Log.v(TAG, "dumBuf: b.length = " + b.length);
		String str = "";
		for(int i = 0; i<b.length; i++) {
			str += String.format("%02x ", b[i]);
			if(i%16 == 15)
				str += '\n';
		}
		Log.v(TAG, str);
	}
	
	/**
	 * IR Tx port
	 */
	public enum Port {
		PORT0,
		PORT1
	}
	
	private void dumpKeyMap() {

		Log.v(TAG, "dump keyDataMap");
		for(Map.Entry<Integer, byte[]> pair : keyDataMap.entrySet()) {
			String str = "";
			str += pair.getKey() + " ";
			for(byte b : pair.getValue()) {
				str += String.format("%02x ", b);
			}
			Log.v(TAG, str);
		}
	}
	
	private void keyFileToMap() throws IOException {
		
		File file = new File(Environment.getExternalStorageDirectory() + "/learned_key.txt");
		if(!file.exists()) {
			file.createNewFile();
			return;
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while((line = br.readLine()) != null) {
			String[] tokens = line.split("\\s");
			int key = Integer.parseInt(tokens[0]);
			byte[] value = new byte[tokens.length - 1];
			for(int i = 1; i<tokens.length; i++) {
				value[i-1] = (byte) Integer.parseInt(tokens[i], 16);
			}
			keyDataMap.put(key, value);
		}
		br.close();
		dumpKeyMap();
	}
	
	private void keyMapToFile() throws IOException {

		BufferedWriter bw = new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory() + "/learned_key.txt"));
		
		for(Entry<Integer, byte[]> pair : keyDataMap.entrySet()) {
			int key = (Integer) pair.getKey();
			byte [] value = (byte[]) pair.getValue();
			
			String str = "";
			str += key + "\t";
			for(byte b : value) {
				str += String.format("%02x ", b);
			}
			str += '\n';
			bw.write(str);
		}
		bw.close();
	}

	private class RxPacket extends AckPacket{		
		RxPacket(byte[] byteBuf) {
			super(byteBuf);
		}

		private boolean isValid() {
			byte tempChecksum;
			tempChecksum = 0;
			tempChecksum += command;
			tempChecksum += len;
			tempChecksum += status;
			for(byte b : data) {
				tempChecksum += b;
			}
			return tempChecksum == checksum && command == COMMAND_RX;
		}
	}
	/**
	 * Create connection to IR MCU and open database
	 * @param context the application context
	 * @param rxHandler while receiving a key, this class will send a message to rxHandler. The Message.obj contains RxKey object
	 * @throws IOException if fail to establish the connection to IR MCU or fail to open database
	 */
	public Ir(Context context, final Handler rxHandler) throws IOException {
		super("/dev/ttyMFD1", BaudRate.BPS115200, 0);
		
		keyDataMap = new HashMap<Integer, byte[]>();
		keyFileToMap();	

		dbHelper = new DataBaseHelper(context);
		dbHelper.createDataBase();
		db = dbHelper.getReadableDatabase();
		
		try {
			setMode(MODE_RX);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		this.rxHandler = rxHandler;
		readThread = new Thread(new Runnable() {
	        public void run() {
	        	byte []rxBuf = new byte[8];
	        	ttyReadBuf = new byte[1024];
	        	RxKey rxKey;
	        	long sendTime = 0;
	        	while(true) {
	        		try {
	        			ttyReadRet = Ir.super.read(ttyReadBuf);
						//dumpBuf(buf);
						if(mode == MODE_RX) {
							System.arraycopy(ttyReadBuf, 0, rxBuf, 0, rxBuf.length);
							RxPacket ack = new RxPacket(rxBuf);
							if(ack.isValid()) {
								int key = (ack.data[1] << 16) | (ack.data[2] << 8) | ack.data[3];
								rxKey = RxKey.toRxKey(key);
								if(rxKey != null) {
									if(ack.status == 0 || (System.currentTimeMillis() - sendTime >= 1000)) {
										Log.e(TAG, "receive key: " + rxKey.name());
						                Message msg = Message.obtain();
						                msg.obj = rxKey;
						                rxHandler.sendMessage(msg);
										sendTime = System.currentTimeMillis();
										
									}
								}
							}
						} 
						else {
							synchronized (readThread) {
								readThread.notify();//if we do not specify readThread, it will notify other object and throw exception @$#%@^@
							}
						}
						
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	    	}
	    });
		
		readThread.start();
	}
	
	protected int read(byte[] buf) throws IOException {
		try {
			synchronized (readThread) {
				readThread.wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}

		int ret = Math.min(ttyReadRet, buf.length);
		System.arraycopy(ttyReadBuf, 0, buf, 0, ret);
		return ret;
	}
	
	
	/**
	 * Received keys from TouchLife TLT-SRC3200 remote controller
	 *
	 */
	public enum RxKey {
		UP					(0x3F3A05),
		DOWN				(0x3F3B04),
		LEFT				(0x3F3C03),
		RIGHT				(0x3F3D02),
		OK					(0x3F3E01),
		CURTAIN_P1_OPEN1 	(0x3F3609),
		CURTAIN_P1_CLOSE 	(0x3F3708),
		CURTAIN_P2_OPEN		(0x3F3906),
		CURTAIN_P2_CLOSE	(0x3F3807),
		LAMP_P1_1			(0x3F1E21),
		LAMP_P1_2			(0x3F1F20),
		LAMP_P1_3			(0x3F201F),
		LAMP_P1_4			(0x3F211E),
		LAMP_P1_5			(0x3F221D),
		LAMP_P1_6			(0x3F231C),
		LAMP_P1_7			(0x3F241B),
		LAMP_P1_8			(0x3F251A),
		LAMP_P1_9			(0x3F2619),
		LAMP_P1_10			(0x3F2718),
		LAMP_P1_11			(0x3F2817),
		LAMP_P1_12			(0x3F2916),
		LAMP_P2_1			(0x3F2A15),
		LAMP_P2_2			(0x3F2B14),
		LAMP_P2_3			(0x3F2C13),
		LAMP_P2_4			(0x3F2D12),
		LAMP_P2_5			(0x3F2E11),
		LAMP_P2_6			(0x3F2F10),
		LAMP_P2_7			(0x3F300F),
		LAMP_P2_8			(0x3F310E),
		LAMP_P2_9			(0x3F320D),
		LAMP_P2_10			(0x3F330C),
		LAMP_P2_11			(0x3F340B),
		LAMP_P2_12			(0x3F350A),
		SITUATION_P1_DAY	(0x3F0837),
		SITUATION_P1_NIGHT	(0x3F0936),
		SITUATION_P1_SLEEP	(0x3F0B34),
		SITUATION_P1_THEATER(0x3F0D32),
		SITUATION_P1_ROMANTIC(0x3F0E31),
		SITUATION_P1_DINE	(0x3F0A35),
		SITUATION_P1_WELCOME(0x3F0639),
		SITUATION_P1_AWAY	(0x3F0738),
		SITUATION_P1_OUT	(0x3F0C33),
		SITUATION_P1_CUSTOMA(0x3F0F30),
		SITUATION_P1_CUSTOMB(0x3F102F),
		SITUATION_P1_CUSTOMC(0x3F112E),
		SITUATION_P2_DAY	(0x3F142B),
		SITUATION_P2_NIGHT	(0x3F152A),
		SITUATION_P2_SLEEP	(0x3F1728),
		SITUATION_P2_THEATER(0x3F1926),
		SITUATION_P2_ROMANTIC(0x3F1A25),
		SITUATION_P2_DINE	(0x3F1629),
		SITUATION_P2_WELCOME(0x3F122D),
		SITUATION_P2_AWAY	(0x3F132C),
		SITUATION_P2_OUT	(0x3F1827),
		SITUATION_P2_CUSTOMA(0x3F1B24),
		SITUATION_P2_CUSTOMB(0x3F1C23),
		SITUATION_P2_CUSTOMC(0x3F1D22);	
		
		int key;
		
		RxKey(int key) {
			this.key = key;
		}
		
		static RxKey toRxKey(int key) {
			for(RxKey rxKey : RxKey.values()) {
				if(rxKey.key == key)
					return rxKey;
			}
			return null;
		}
	}
		
	private class CommandPacket {
		byte[] syncKey = {0x45, 0x34};
		byte command;
		byte len;
		byte[] data;
		byte checksum;
		
		void calChecksum() {
			checksum = 0;
			for(byte b : syncKey) {
				checksum += b;
			}
			checksum += command;
			checksum += len;
			for(byte b : data) {
				checksum += b;
			}
		}
		
		CommandPacket(byte command, byte[] data) {
			this.command = command;
			this.len = (byte) (4+data.length);
			this.data = data;
			
			calChecksum();
		}
		
		CommandPacket(byte command) {
			this(command, new byte[0]);
			
		}
		
		private byte[] getBytes() {
			ByteBuffer buf = ByteBuffer.allocate(len+1);
			buf.put(syncKey);
			buf.put(command);
			buf.put(len);
			buf.put(data);
			buf.put(checksum);
			dumpBuf(buf.array());
			return buf.array();
		}
	}
	
	private class AckPacket {
		byte command;
		byte len;
		byte status;
		byte[] data;
		byte checksum;
		
		AckPacket(byte[] byteBuf) {
			if(byteBuf.length >= 4 && byteBuf[1] == byteBuf.length - 1)
			{
				ByteBuffer buf = ByteBuffer.wrap(byteBuf);
				command = buf.get();
				len = buf.get();
				status = buf.get();
				data = new byte[len-3];
				buf.get(data);
				checksum = buf.get();
			}
			else {
				data = new byte[0];
			}
			//Log.e(TAG, "AckPacket");
			//dumpBuf(byteBuf);
		}
		
		private boolean isValid() {
			byte tempChecksum;
			tempChecksum = 0;
			tempChecksum += command;
			tempChecksum += len;
			tempChecksum += status;
			for(byte b : data) {
				tempChecksum += b;
			}
			return tempChecksum == checksum && status == STATUS_OK;
		}
	}
	
	/**
	 * Get the ID of IR MCU.
	 * Block until the ID is returned.
	 * @return byte array of the IR MCU ID. The max bytes are 16 bytes.
	 * @throws IOException if the connection to IR MCU is broken.
	 * @throws InterruptedException if the Thread.sleep is interrupted.
	 */
	public byte[] getID() throws IOException, InterruptedException {
		setMode(MODE_NORMAL);
		write(new CommandPacket(COMMAND_GET_ID).getBytes());
		byte[] buf = new byte[13];
		read(buf);
		AckPacket ackPacket = new AckPacket(buf);
		setMode(MODE_RX);
		return ackPacket.data;
	}
	
	/**
	 * Learning a key from A/V remote controller and store it in /sdcard/learned_key.txt.
	 * Block until the key is learned or time out (15 seconds).
	 * @param keyID the key number to store the learning result.
	 * @return true if success, false otherwise.
	 * @throws IOException if the connection to IR MCU is broken.
	 * @throws InterruptedException if the Thread.sleep is interrupted.
	 */
	public boolean learnShort(int keyID) throws IOException, InterruptedException {
		setMode(MODE_NORMAL);
		write(new CommandPacket(COMMAND_LEARN_IR_SHORT).getBytes());

		byte[] buf = new byte[4];
		read(buf);
		if(!new AckPacket(buf).isValid())
		{
			Log.e(TAG, "learnShort ack1 fail");
			return false;			
		}

		buf = new byte[84];
		read(buf);
		AckPacket ackPacket = new AckPacket(buf);
		if(ackPacket.isValid() && ackPacket.data.length == 80)
		{
			Log.i(TAG, "learnShort success");
			keyDataMap.put(keyID, ackPacket.data);
			keyMapToFile();
			return true;
		}
		Log.e(TAG, "learnShort ack2 fail");
		setMode(MODE_RX);
		return false;
	}

	/**
	 * Learning a key from AC remote controller and store it in /sdcard/learned_key.txt.
	 * Block until the key is learned or time out (15 seconds).
	 * @param keyID the key number to store the learning result.
	 * @return true if success, false otherwise.
	 * @throws IOException if the connection to IR MCU is broken.
	 * @throws InterruptedException if the Thread.sleep is interrupted.
	 */
	public boolean learnLong(int keyID) throws IOException, InterruptedException {
		setMode(MODE_NORMAL);
		write(new CommandPacket(COMMAND_LEARN_IR_LONG).getBytes());

		byte[] buf = new byte[4];
		read(buf);
		if(!new AckPacket(buf).isValid())
		{
			Log.e(TAG, "learnShort ack1 fail");
			return false;			
		}

		buf = new byte[256];
		int ret = read(buf);
		buf = Arrays.copyOf(buf, ret);
		AckPacket ackPacket = new AckPacket(buf);
		if(ackPacket.isValid())
		{
			Log.i(TAG, "learnLong success");
			keyDataMap.put(keyID, ackPacket.data);
			keyMapToFile();
			return true;
		}
		Log.e(TAG, "learnLong ack2 fail");
		setMode(MODE_RX);
		return false;
	}
	
	private void setTxPort(Port port) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter("/d/gpio_debug/gpio1/current_value"));
		bw.write(port == Port.PORT0 ? "low" : "high");
		bw.close();
	}
	
	private void setMode(int mode) throws IOException, InterruptedException {
		BufferedWriter bw = new BufferedWriter(new FileWriter("/d/gpio_debug/gpio2/current_value"));
		bw.write(mode == MODE_RX ? "low" : "high");
		bw.close();		
		
		if(mode == MODE_NORMAL) {
			Thread.sleep(100);
		}
		
		this.mode = mode;
	}
	
	/**
	 * Send a learned key.
	 * Block until the ack is received.
	 * @param port the Tx port.
	 * @param keyID the key number of learned key to send (match the key parameter of learnShort/learnLong function).
	 * @throws IOException if the connection to IR MCU is broken.
	 * @return true if success, false otherwise.
	 * @throws InterruptedException if the Thread.sleep is interrupted.
	 */
	public boolean txLearn(Port port, int keyID) throws IOException, InterruptedException {
		setMode(MODE_NORMAL);
		byte [] keyData = keyDataMap.get(keyID);
		if(keyData == null)
			return false;
		
		setTxPort(port);
		
		byte [] txData = new byte[keyData.length+1];
		txData[0] = (byte) 0x81;
		System.arraycopy(keyData, 0, txData, 1, keyData.length);
		write(new CommandPacket(COMMAND_TX_LEARN_IR, txData).getBytes());

		byte[] buf = new byte[4];
		read(buf);
		
		if (!new AckPacket(buf).isValid())
		{
			Log.e(TAG, " txLearn fail");
			return false;
		}
		Log.i(TAG, " txLearn success");
		setMode(MODE_RX);
		return true;
	}

	private boolean txDB(Port port, String irData) throws IOException, InterruptedException {
		setMode(MODE_NORMAL);
		setTxPort(port);
		
		byte[] txData = new byte[irData.length()/2 +5];
		txData[0] = (byte) 0x81;
		txData[1] = 0x38;
		txData[2] = 0x34;
		txData[3] = 0x33;
		txData[4] = 0x30;
		for(int i = 5; i<txData.length; i++) {
			txData[i] = (byte) Integer.parseInt("" + irData.charAt(2*(i-5)) + irData.charAt(2*(i-5) + 1), 16);
		}		
		
		write(new CommandPacket(COMMAND_TX_IR, txData).getBytes());

		byte[] buf = new byte[4];
		read(buf);

		if (!new AckPacket(buf).isValid())
		{
			Log.e(TAG, " txDB fail");
			return false;
		}
		Log.i(TAG, " txDB success");
		setMode(MODE_RX);
		return true;
	}

	/**
	 * 
	 * Keys for A/V device
	 *
	 */
	public enum Key {
		POWER		(1),
		CH_DOWN		(2),
		CH_UP		(3),
		VOL_DOWN	(4),
		VOL_UP		(5),
		KEY0		(13),
		KEY1		(14),
		KEY2		(15),
		KEY3		(16),
		KEY4		(17),
		KEY5		(18),
		KEY6		(19),
		KEY7		(20),
		KEY8		(21),
		KEY9		(22);
		
		private int key;
		
		Key(int key) {
			this.key = key;
		}
		
		public String toString() {
			return "" + key;
		}
	}
	
	/**
	 * 
	 * Power state for air conditioner
	 *
	 */
	public enum Power {
		ON	("\"ON\""),
		OFF	("\"OFF\"");
		
		private String power;
		
		Power(String power) {
			this.power = power;
		}
		
		public String toString() {
			return power;
		}
	}

	/**
	 * 
	 * Operation mode for air conditioner
	 *
	 */
	public enum Mode {
		AUTO	("\"AUTO\""),
		COOL	("\"COOL\""),
		DRY		("\"DRY\""),
		FAN		("\"FAN\""),
		HEAT	("\"HEAT\"");
		
		private String mode;
		
		Mode(String mode) {
			this.mode = mode;
		}
		
		public String toString() {
			return mode;
		}
	}
	
	/**
	 * Temperature for air conditioner
	 *
	 */
	public enum Temp {
		TEMP_16	(16),
		TEMP_17 (17),
		TEMP_18 (18),
		TEMP_19 (19),
		TEMP_20 (20),
		TEMP_21 (21),
		TEMP_22 (22),
		TEMP_23 (23),
		TEMP_24 (24),
		TEMP_25 (25),
		TEMP_26 (26),
		TEMP_27 (27),
		TEMP_28 (28),
		TEMP_29 (29),
		TEMP_30 (30),
		TEMP_31 (31);
		
		private int temp;
		
		Temp(int temp) {
			this.temp = temp;
		}
		
		public String toString() {
			return ""+temp;
		}
	}
	
	/**
	 * Fan speed for air conditioner
	 *
	 */
	public enum Fan {
		AUTO 	("\"FAN AUTO\""),
		LOW		("\"FAN LOW\""),
		MID		("\"FAN MID\""),
		HI		("\"FAN HI\"");
		
		private String fan;
		
		Fan(String fan) {
			this.fan = fan;
		}
				
		public String toString() {
			return fan;
		}
	}
	
	//=========================== start of DB functions ===================================

	DataBaseHelper dbHelper;
	SQLiteDatabase db;
	private static class DataBaseHelper extends SQLiteOpenHelper
	{
		private static String TAG = "DataBaseHelper"; // Tag just for the LogCat window
		//destination path (location) of our database on device
		private static String DB_PATH = ""; 
		private static String DB_NAME ="demo.db";// Database name
		private SQLiteDatabase mDataBase; 
		private final Context mContext;

		public DataBaseHelper(Context context) 
		{
		    super(context, DB_NAME, null, 1);// 1? its Database Version
		    DB_PATH = context.getApplicationInfo().dataDir + "/databases/"; 
		    mContext = context;
		}   

		public void createDataBase() throws IOException
		{
		    //If database not exists copy it from the assets
	
		    boolean mDataBaseExist = checkDataBase();
		    if(!mDataBaseExist)
		    {
		        getReadableDatabase();
		        close();
		        try 
		        {
		            //Copy the database from assests
		            copyDataBase();
		            Log.e(TAG, "createDatabase database created");
		        } 
		        catch (IOException mIOException) 
		        {
		            throw new Error("ErrorCopyingDataBase");
		        }
		    }
		}
	    //Check that the database exists here: /data/data/your package/databases/Da Name
	    private boolean checkDataBase()
	    {
	        File dbFile = new File(DB_PATH + DB_NAME);
	        //Log.v("dbFile", dbFile + "   "+ dbFile.exists());
	        return dbFile.exists();
	    }

	    //Copy the database from assets
	    private void copyDataBase() throws IOException
	    {
	        InputStream mInput = mContext.getAssets().open(DB_NAME);
	        String outFileName = DB_PATH + DB_NAME;
	        OutputStream mOutput = new FileOutputStream(outFileName);
	        byte[] mBuffer = new byte[1024];
	        int mLength;
	        while ((mLength = mInput.read(mBuffer))>0)
	        {
	            mOutput.write(mBuffer, 0, mLength);
	        }
	        mOutput.flush();
	        mOutput.close();
	        mInput.close();
	    }

	    //Open the database, so we can query it
	    public boolean openDataBase() throws SQLException
	    {
	        String mPath = DB_PATH + DB_NAME;
	        //Log.v("mPath", mPath);
	        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
	        //mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
	        return mDataBase != null;
	    }

	    @Override
	    public synchronized void close() 
	    {
	        if(mDataBase != null)
	            mDataBase.close();
	        super.close();
	    }

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			
		}

	}
	
	private static final byte MODE_AV = 0;
	private static final byte MODE_AC = 1;
		
	private String[] getBrand(int mode) {
		String codeListTable = mode == MODE_AV ? "tbCodeList_AV": "tbCodelist_AC";
		Cursor c = db.rawQuery("SELECT DISTINCT brandName FROM tbBrandId," + codeListTable + " WHERE tbBrandId.brandId = " + codeListTable + ".brandId", null);
		if(!c.moveToFirst())
			return null;
		ArrayList<String> brandName = new ArrayList<String>();
		
		do {
			brandName.add(c.getString(0));
		} while(c.moveToNext());
		return brandName.toArray(new String[0]);
	}

	/**
	 * Get all A/V device brands names in database.
	 * @return String[] of A/V brands names
	 */
	public String[] getBrandAV() {
		return getBrand(MODE_AV);
	}
	
	/**
	 * Get all air conditioner brands names in database.
	 * @return String[] of air conditioner brands names
	 */
	public String[] getBrandAC() {
		return getBrand(MODE_AC);
	}
	

	private String[] getCodeList(String brand, int mode) {
		String codeListTable = mode == MODE_AV ? "tbCodeList_AV": "tbCodelist_AC";
		Cursor c = c = db.rawQuery("SELECT codeNum FROM " + codeListTable +",tbBrandId" + " WHERE tbBrandId.brandId = " + codeListTable + ".brandId AND brandName =?  ORDER BY rank desc", new String[] {brand});
		if(!c.moveToFirst())
			return null;
		ArrayList<String> array = new ArrayList<String>();
		do {
			array.add(c.getString(0));
		}while(c.moveToNext());
		return array.toArray(new String[0]);
	}
	
	/**
	 * Get a list of code number for a brand of A/V device.
	 * Each code number map to a set of Keys (POWER, VOL+, VOL-,...).
	 * A code number may be used by multiple A/V device with the same brand. 
	 * @param brand specify which brand of code list to get
	 * @return String[]. Each String represent a code number
	 */
	public String[] getCodeListAV(String brand) {
		return getCodeList(brand, MODE_AV);
	}

	/**
	 * Get a list of code number for a brand of air conditioner.
	 * Each code number map to a set of air conditioner states.
	 * A code number may be used by multiple air conditioner with the same brand. 
	 * @param brand specify which brand of code list to get
	 * @return String[]. Each String represent a code number
	 */
	public String[] getCodeListAC(String brand) {
		return getCodeList(brand, MODE_AC);
	}	

	/**
	 * Send a key of an A/V device in database to a IR Tx port
	 * @param port the IR Tx port
	 * @param codeNum the code number to be selected in database
	 * @param key the key to be sent
	 * @return true if success, false otherwise
	 * @throws IOException if fail to access the tty device
	 * @throws InterruptedException if the Thread.sleep is interrupted.
	 */
	public boolean txAV(Port port, String codeNum, Key key) throws IOException, InterruptedException {
		Cursor c = db.rawQuery("SELECT irData FROM tbIrData_AV WHERE codeNum = " + codeNum + " AND keyID = " + key, null);
		if(!c.moveToFirst())
			return false;
		String irData = c.getString(0);
		return txDB(port, irData);
	}
	
	/**
	 * Send a state of an air conditioner in database to a IR Tx port.
	 * @param port the IR Tx port
	 * @param codeNum the code number to be selected in database
	 * @param power the power state. Mode, temp, and fan will be ignored if power is set to off
	 * @param mode the operation mode. Temp and fan will be ignored if mode is AUTO or DRY. Temp will be ignored if mode is FAN
	 * @param temp the temperature 
	 * @param fan the fan speed
	 * @return true if success, false otherwise
	 * @throws IOException if fail to access the tty device
	 * @throws InterruptedException if the Thread.sleep is interrupted.
	 */
	public boolean txAC(Port port, String codeNum, Power power, Mode mode, Temp temp, Fan fan) throws IOException, InterruptedException {
		Cursor c;
		if(power == Power.OFF)
			c = db.rawQuery("SELECT irData FROM tbIrData_AC WHERE codeNum = " + codeNum + " AND stPower = " + power, null);
		else if(mode == Mode.AUTO || mode == Mode.DRY) 
			c = db.rawQuery("SELECT irData FROM tbIrData_AC WHERE codeNum = " + codeNum + " AND stPower = " + power + " AND stMode = " + mode, null);
		else if(mode == Mode.FAN)
			c = db.rawQuery("SELECT irData FROM tbIrData_AC WHERE codeNum = " + codeNum + " AND stPower = " + power + " AND stMode = " + mode + " AND stFan = " + fan, null);
		else
			c = db.rawQuery("SELECT irData FROM tbIrData_AC WHERE codeNum = " + codeNum + " AND stPower = " + power + " AND stMode = " + mode + " AND stFan = " + fan + " AND stTemp = " + temp, null);

		if(!c.moveToFirst())
			return false;
		String irData = c.getString(0);
		return txDB(port, irData);
	}
}
