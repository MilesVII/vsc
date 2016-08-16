package com.milesseventh.vsc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Random;

public class Task implements Runnable {
	private Mode mode;
	private File source, result, key;
	private byte[][] keys;
	private int akl;
	public final int BUFFER_SIZE;
	public static final int DEFAULT_BUFFER_SIZE = 4096, MINIMUM_KEY_LENGTH = 512, KEY_STEP = 128, KEY_AMOUNT = 3/*Must be odd number, >2*/;
	public enum Mode {ENC, DEC};

	public Task(Mode _m, File _source, File _result, File _key, int _bufsize){
		mode = _m;
		source = _source;
		result = _result;
		key = _key;
		akl = getAverageKeyLength(_source.length());
		BUFFER_SIZE = _bufsize;
		if (_m == Mode.ENC)
			keyGen(key, akl);
		else
			keyRestore(key);
	}
	
	@Override
	public void run() {
		long _startTime = System.currentTimeMillis();
		try {
			process(mode == Mode.ENC);
		} catch (Exception ex){
			ex.printStackTrace();
		}
		System.out.println("Done in " + ((float)(System.currentTimeMillis() - _startTime)/1000) + "secs");
	}
	
	private void process(boolean mode) throws Exception{
		int blockNum = 0;
		FileInputStream sourceIn = new FileInputStream(source);
		FileOutputStream resultOut = new FileOutputStream(result);
		
		byte[] buf = new byte[BUFFER_SIZE];
		byte[] res_buf = new byte[BUFFER_SIZE];
		
		while(sourceIn.available() > BUFFER_SIZE){
			processBlock(sourceIn, resultOut, BUFFER_SIZE, buf, res_buf, blockNum, mode);
			blockNum++;
		}
		
		buf = new byte[sourceIn.available()];
		res_buf = new byte[sourceIn.available()];
		processBlock(sourceIn, resultOut, sourceIn.available(), buf, res_buf, blockNum, mode);

		sourceIn.close();
		resultOut.close();
	}
	
	private void processBlock(FileInputStream in, FileOutputStream out, int length, 
							  byte[] buf, byte[] res_buf, int blockNum, boolean mode) throws Exception{
		in.read(buf);
		byte _sum;
		for (int pony = 0; pony < length; pony++){
			_sum = 0;
			for(byte[] _horse : keys)
				_sum += _horse[(pony + (blockNum + 1) * BUFFER_SIZE) % _horse.length];
			res_buf[pony] = (byte)(buf[pony] + (mode?1:-1) * _sum
							/*(keys[0][(pony + (blockNum + 1) * BUFFER_SIZE) % (akl - 1)] + 
							 keys[1][(pony + (blockNum + 1) * BUFFER_SIZE) % (akl)] + 
							 keys[2][(pony + (blockNum + 1) * BUFFER_SIZE) % (akl + 1)])*/); 
		}
		out.write(res_buf);
	}

	private void keyGen(File _key, int _akl){
		try {
			FileOutputStream keyOut = new FileOutputStream(_key);
			Random _r = new Random();
			keys = new byte[KEY_AMOUNT][];
			for (int _sigh = 0; _sigh < KEY_AMOUNT; _sigh++){
				keys[_sigh] = new byte[akl + (_sigh - (KEY_AMOUNT / 2))];
				_r = new Random(_r.nextInt());
				_r.nextBytes(keys[_sigh]);
				keyOut.write(keys[_sigh]);
			}
			/*firstKey = new byte[akl - 1];
			secondKey = new byte[akl];
			thirdKey = new byte[akl + 1];
			Random _r = new Random();
			_r.nextBytes(firstKey);
			_r.nextBytes(secondKey);
			_r.nextBytes(thirdKey);
			keyOut.write(firstKey);
			keyOut.write(secondKey);
			keyOut.write(thirdKey);*/
			keyOut.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void keyRestore(File _key){
		try {
			long __ = _key.length();
			if (__ % KEY_AMOUNT == 0)
				akl = (int)(__ / KEY_AMOUNT);
			else
				throw new Exception("Wrong key!");
			
			FileInputStream keyIn = new FileInputStream(_key);
			
			
			keys = new byte[KEY_AMOUNT][];
			for (int _sigh = 0; _sigh < KEY_AMOUNT; _sigh++){
				keys[_sigh] = new byte[akl + (_sigh - (KEY_AMOUNT / 2))];
				keyIn.read(keys[_sigh]);
			}
			
			
			/*firstKey = new byte[akl - 1];
			secondKey = new byte[akl];
			thirdKey = new byte[akl + 1];
			keyIn.read(firstKey);
			keyIn.read(secondKey);
			keyIn.read(thirdKey);*/
			keyIn.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private int getAverageKeyLength(long F){
		int L = MINIMUM_KEY_LENGTH;
		while(!aklCheck(F, L))
			L += KEY_STEP;
		return L;
	}
	
	private boolean aklCheck(long F, int L){
		return (F < (long)(L*L*L-L));
	}
}
