package org.mcupdater.gui;

import javax.swing.text.Style;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class CALogHandler extends Handler
{
	private ConsoleArea console;
	private SimpleDateFormat sdFormat = new SimpleDateFormat("[HH:mm:ss.SSS] ");

	public CALogHandler(ConsoleArea console) {
		this.console = console;
	}

	@Override
	public void publish(LogRecord record) {
		if (this.isLoggable(record)) {
			Calendar recordDate = Calendar.getInstance();
			recordDate.setTimeInMillis(record.getMillis());
			Style a = null;
			if (record.getLevel() == Level.INFO) {
				a = console.infoStyle;
			}
			if (record.getLevel() == Level.WARNING) {
				a = console.warnStyle;
			}
			if (record.getLevel() == Level.SEVERE) {
				a = console.errorStyle;
			}
			Throwable thrown = record.getThrown();
			try {
				console.log(sdFormat.format(recordDate.getTime()) + record.getMessage() + (thrown != null ? " (stacktrace in " + record.getLoggerName() + " log)" : "") + "\n", a);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void flush() {}

	@Override
	public void close() throws SecurityException {}
}
