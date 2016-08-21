package com.milesseventh.vsc;

import java.io.File;
import org.apache.commons.cli.*;

public class Main {
	public static final String OPTION_MODE_ENC = "e";
	public static final String OPTION_MODE_DEC = "d";
	public static final String OPTION_FILE_IN = "in";
	public static final String OPTION_FILE_OUT = "out";
	public static final String OPTION_FILE_KEY = "key";
	public static final String OPTION_BUFFER_SIZE = "bs";
	public static final String OPTION_ALLOW_REWRITE = "r";
	
	public static void main(String[] args) throws Exception {
		Options opts = new Options();
		opts.addOption(OPTION_MODE_ENC, false, "Encrypt file*");
		opts.addOption(OPTION_MODE_DEC, false, "Decrypt file*");
		opts.addOption(OPTION_FILE_IN, true, "Input file*");
		opts.addOption(OPTION_FILE_OUT, true, "Output file");
		opts.addOption(OPTION_FILE_KEY, true, "Key file");
		opts.addOption(OPTION_BUFFER_SIZE, true, "Buffer size");
		opts.addOption(OPTION_ALLOW_REWRITE, false, "Allow rewrite of output/key file");
		
		CommandLine unicorn = new DefaultParser().parse(opts, args);
		
		if (!(unicorn.hasOption(OPTION_MODE_DEC) ^ unicorn.hasOption(OPTION_MODE_ENC)) ||
			!unicorn.hasOption(OPTION_FILE_IN))
			getHelp(opts);

		File sourceFile = new File(unicorn.getOptionValue(OPTION_FILE_IN));
		if (!sourceFile.exists()){
			throwErr("Error: Source file Not Found");
		}
		
		File resultFile = new File(unicorn.hasOption(OPTION_FILE_OUT)?unicorn.getOptionValue(OPTION_FILE_OUT):sourceFile.getPath() + (unicorn.hasOption(OPTION_MODE_DEC)?".enc":".dec"));
		checkNewFile(resultFile, unicorn.hasOption(OPTION_ALLOW_REWRITE));
		resultFile.createNewFile();
		File keyFile = new File(unicorn.hasOption(OPTION_FILE_KEY)?unicorn.getOptionValue(OPTION_FILE_KEY):sourceFile.getPath() + ".key");
		if (unicorn.hasOption(OPTION_MODE_ENC)){
			checkNewFile(keyFile, unicorn.hasOption(OPTION_ALLOW_REWRITE));
		} else {
			if (!keyFile.exists())
				throwErr("Error: Key file Not Found");
		}
		int _bufsize = (unicorn.hasOption(OPTION_BUFFER_SIZE)?Integer.parseInt(unicorn.getOptionValue(OPTION_BUFFER_SIZE)):Task.DEFAULT_BUFFER_SIZE);
		Task task = new Task(unicorn.hasOption(OPTION_MODE_ENC)?Task.Mode.ENC:Task.Mode.DEC, sourceFile, resultFile, keyFile, _bufsize);
		task.run();
	}
	
	private static void checkNewFile(File _fluffytail, boolean _r) throws Exception{
		if (_fluffytail.exists() && !_r)
			throwErr("Error: " + _fluffytail.getName() + " already exist. Use -r option to rewrite");
		else
			_fluffytail.createNewFile();
	}
	
	public static void throwErr(String _mess){
		System.out.println(_mess);
		System.exit(0);
	}
	
	private static void getHelp(Options _opts){
		new HelpFormatter().printHelp("vsc", _opts );
		System.exit(0);
	}
}
