package server.lib.orm.logger;

import java.lang.reflect.Constructor;

import server.lib.orm.logger.Log.Level;

/**
 * Factory that creates {@link Logger} instances. It uses reflection to see what loggers are installed on the system and
 * tries to find the most appropriate one.
 * 
 * <p>
 * To set the logger to a particular type, set the system property ("server.lib.orm.logger.type") contained in
 * {@link #LOG_TYPE_SYSTEM_PROPERTY} ("server.lib.orm.logger.type") to be one of the values in
 * {@link LogType} enum.
 * </p>
 */
public class LoggerFactory {

	public static final String LOG_TYPE_SYSTEM_PROPERTY = "server.lib.orm.logger.type";
	private static LogFactory logFactory;

	/**
	 * For static calls only.
	 */
	private LoggerFactory() {
	}

	/**
	 * Return a logger associated with a particular class.
	 */
	public static Logger getLogger(Class<?> clazz) {
		return getLogger(clazz.getName());
	}

	/**
	 * Set the log factory to be a specific instance. This allows you to easily redirect log messages to your own
	 * {@link Log} implementation.
	 */
	public static void setLogFactory(LogFactory logFactory) {
		LoggerFactory.logFactory = logFactory;
	}

	/**
	 * Return a logger associated with a particular class name.
	 */
	public static Logger getLogger(String className) {
		if (logFactory == null) {
			logFactory = findLogFactory();
		}
		return new Logger(logFactory.createLog(className));
	}

	/**
	 * Return the single class name from a class-name string.
	 */
	public static String getSimpleClassName(String className) {
		// get the last part of the class name
		String[] parts = className.split("\\.");
		if (parts.length <= 1) {
			return className;
		} else {
			return parts[parts.length - 1];
		}
	}

	/**
	 * Return the most appropriate log factory. This should _never_ return null.
	 */
	private static LogFactory findLogFactory() {

		// see if the log-type was specified as a system property
		String logTypeString = System.getProperty(LOG_TYPE_SYSTEM_PROPERTY);
		if (logTypeString != null) {
			try {
				return LogType.valueOf(logTypeString);
			} catch (IllegalArgumentException e) {
				Log log = new LocalLog(LoggerFactory.class.getName());
				log.log(Level.WARNING, "Could not find valid log-type from system property '" + LOG_TYPE_SYSTEM_PROPERTY
						+ "', value '" + logTypeString + "'");
			}
		}

		for (LogType logType : LogType.values()) {
			if (logType.isAvailable()) {
				return logType;
			}
		}
		// fall back is always LOCAL, probably never reached
		return LogType.LOCAL;
	}

	/**
	 * Log factory for generating Log instances.
	 */
	public interface LogFactory {
		/**
		 * Create a log implementation from the class-label.
		 */
		public Log createLog(String classLabel);
	}

	/**
	 * Type of internal logs supported. This is package permissions for testing.
	 */
	public enum LogType implements LogFactory {
		SLF4J("org.slf4j.LoggerFactory", "server.lib.orm.logger.Slf4jLoggingLog"),
		/**
		 * WARNING: Android log must be _before_ commons logging since Android provides commons logging but logging
		 * messages are ignored that are sent there. Grrrrr.
		 */
		ANDROID("android.util.Log", "server.lib.orm.android.AndroidLog"),
		COMMONS_LOGGING("org.apache.commons.logging.LogFactory", "server.lib.orm.logger.CommonsLoggingLog"),
		LOG4J2("org.apache.logging.log4j.LogManager", "server.lib.orm.logger.Log4j2Log"),
		LOG4J("org.apache.log4j.Logger", "server.lib.orm.logger.Log4jLog"),
		// this should always be at the end as the fall-back, so it's always available
		LOCAL(LocalLog.class.getName(), LocalLog.class.getName()) {
			@Override
			public Log createLog(String classLabel) {
				return new LocalLog(classLabel);
			}

			@Override
			public boolean isAvailable() {
				// always available
				return true;
			}
		},
		// we put this down here because it's always available but we rarely want to use it
		JAVA_UTIL("java.util.logging.Logger", "server.lib.orm.logger.JavaUtilLog"),
		NULL(NullLog.class.getName(), NullLog.class.getName()) {
			@Override
			public Log createLog(String classLabel) {
				return new NullLog(classLabel);
			}

			@Override
			public boolean isAvailable() {
				// never chosen automatically
				return false;
			}
		},
		// end
		;

		private final String detectClassName;
		private final String logClassName;

		private LogType(String detectClassName, String logClassName) {
			this.detectClassName = detectClassName;
			this.logClassName = logClassName;
		}

		@Override
		public Log createLog(String classLabel) {
			try {
				return createLogFromClassName(classLabel);
			} catch (Exception e) {
				// oh well, fall back to the local log
				Log log = new LocalLog(classLabel);
				log.log(Level.WARNING, "Unable to call constructor with single String argument for class "
						+ logClassName + ", so had to use local log: " + e.getMessage());
				return log;
			}
		}

		/**
		 * Return true if the log class is available. This typically is testing to see if a class is available on the
		 * classpath.
		 */
		public boolean isAvailable() {
			if (!isAvailableTestClass()) {
				return false;
			}
			try {
				// try to actually use the logger which resolves problems with the Android stub
				Log log = createLogFromClassName(getClass().getName());
				log.isLevelEnabled(Level.INFO);
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		/**
		 * Try to create the log from the class name which may throw.
		 */
		private Log createLogFromClassName(String classLabel) throws Exception {
			Class<?> clazz = Class.forName(logClassName);
			@SuppressWarnings("unchecked")
			Constructor<Log> constructor = (Constructor<Log>) clazz.getConstructor(String.class);
			return constructor.newInstance(classLabel);
		}

		/**
		 * Is this class available meaning that we should use this logger. This is package permissions for testing
		 * purposes.
		 */
		boolean isAvailableTestClass() {
			try {
				Class.forName(detectClassName);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}
}
