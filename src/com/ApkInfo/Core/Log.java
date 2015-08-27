package com.ApkInfo.Core;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;


public class Log
{
	static private Logger logger = getLogger(Log.class.getName());
	static private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd hh:mm:ss.ms");
	static private StreamHandler consoleHandler;
	static private StreamHandler streamHandler;
	static private ByteArrayOutputStream logOutputStream;
	static private boolean enableConsoleHandler = true; 

	static public enum Level
	{
		ALL(java.util.logging.Level.ALL, ' '),
		VERBOSE(java.util.logging.Level.FINEST, 'V'),
		DEBUG(java.util.logging.Level.FINE, 'D'),
		INFO(java.util.logging.Level.INFO, 'I'),
		WARN(java.util.logging.Level.WARNING, 'W'),
		ERROR(java.util.logging.Level.SEVERE, 'E');
		
		private java.util.logging.Level loggerLevel;
		private char acronym;
		
		private Level(java.util.logging.Level level, char acronym)
		{
			this.loggerLevel = level;
			this.acronym = acronym;
		}
		
		public char getAcronym()
		{
			return this.acronym;
		}
		
		public java.util.logging.Level getLoggerLevel()
		{
			return this.loggerLevel;
		}
		
		static public char getAcronym(java.util.logging.Level level) {
			for(Level l: Level.values()) {
				if(l.loggerLevel == level)
					return l.getAcronym();
			}
			return ' ';
		}
		
	}
	
	static public void e(String msg) {
		logger.severe(getCaller() + " : " + msg);
	}

	static public void e(String tag, String msg) {
		logger.severe(tag + " : " + msg);
	}

	static public void w(String msg) {
		logger.warning(getCaller() + " : " + msg);
	}

	static public void w(String tag, String msg) {
		logger.warning(getCaller() + " : " + msg);
	}
	
	static public void i(String msg) {
		logger.info(getCaller() + " : " + msg);
	}
	
	static public void i(String tag, String msg) {
		logger.info(getCaller() + " : " + msg);
	}

	static public void d(String msg) {
		logger.fine(getCaller() + " : " + msg);
	}

	static public void d(String tag, String msg) {
		logger.fine(getCaller() + " : " + msg);
	}

	static public void v(String msg) {
		logger.finest(getCaller() + " : " + msg);
	}

	static public void v(String tag, String msg) {
		logger.finest(getCaller() + " : " + msg);
	}
	
	static public void setLevel(Level level)
	{
		logger.setLevel(level.getLoggerLevel());
	}
	
	static public void enableConsoleLog(boolean enable)
	{
		if(enable && !enableConsoleHandler)
			logger.addHandler(consoleHandler);
		else if(!enable && enableConsoleHandler)
			logger.removeHandler(consoleHandler);
		enableConsoleHandler = enable; 
	}

	static public Logger getLogger()
	{
		return logger;
	}
	
	static public String getLog()
	{
		streamHandler.flush();
		return logOutputStream.toString();
	}
	
	static public void saveLogFile(String name)
	{
		streamHandler.flush();
		try {
			logOutputStream.writeTo(new FileOutputStream(name));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static private Logger getLogger(String name)
	{
		logger = Logger.getLogger(name);
		logger.setLevel(Level.ALL.getLoggerLevel());
		logger.setUseParentHandlers(false);
		
		Formatter ft = new LogFormatter();

		consoleHandler = new ConsoleHandlerStd();
		consoleHandler.setFormatter(ft);
		consoleHandler.setLevel(Level.ALL.getLoggerLevel());
		logger.addHandler(consoleHandler);
		enableConsoleHandler = true;

		logOutputStream = new ByteArrayOutputStream();
		streamHandler = new StreamHandler(logOutputStream, ft);
		streamHandler.setLevel(Level.ALL.getLoggerLevel());
		logger.addHandler(streamHandler);

        return logger;
	}
	
	static private String getCaller()
	{
		StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
		return caller.getClassName().replaceAll(".*\\.([^$]*).*", "$1") + "(" + caller.getLineNumber() + ")";
	}
	
	static private class LogFormatter extends Formatter
	{
		@Override
		public String format(LogRecord rec) {
			return String.format("%s %03d %c %s\n", dateFormat.format(new Date(rec.getMillis())), rec.getThreadID(), Level.getAcronym(rec.getLevel()), rec.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	static private class ConsoleHandlerToStdout extends ConsoleHandler {
		public ConsoleHandlerToStdout() {
			super();
			setOutputStream(System.out);
		}
	}
	
	static private class ConsoleHandlerStd extends StreamHandler {           
	     public void publish(LogRecord record){      
	         if(record.getLevel().intValue() < java.util.logging.Level.WARNING.intValue())
	             System.out.print(getFormatter().format(record));            
	         else
	             System.err.print(getFormatter().format(record));
	     }
	}

}
