package security.notification;

import org.apache.log4j.Logger;

import security.util.test.TestResult;


class LogNotificationImpl implements NotificationImplInterface{

	private static  Logger log = Logger.getLogger(Logger.class);
	
	static LogNotificationImpl SINGLETON= new LogNotificationImpl();

	private LogNotificationImpl(){
	}

	@Override
	public TestResult test() {
		TestResult ret=new TestResult(Notify.class);
		
		log.debug("Testing log notification impl...");
		return ret;	
	}

	@Override
	public boolean isEnabled(Level level) {
		switch (level.level) {
		case Level.DEBUG_LEVEL:
			return log.isDebugEnabled();
		case Level.INFO_LEVEL:
			return log.isInfoEnabled();
		case Level.WARN_LEVEL:
			return true;
		case Level.ERROR_LEVEL:
			return true;
		case Level.FATAL_LEVEL:
			return true;
		default:
			log.error("Unkown level:"+level);
			return false;
		}
	}

	@Override
	public void notify(Level level, String message, Throwable e) {
		if (!isEnabled(level)){
			return ;
		} else {
			switch (level.level) {
			case Level.DEBUG_LEVEL:
				log.debug(message,e);
				return;
			case Level.INFO_LEVEL:
				log.info(message,e);
				return;
			case Level.WARN_LEVEL:
				log.warn(message,e);
				return;
			case Level.ERROR_LEVEL:
				log.error(message,e);
				return;
			case Level.FATAL_LEVEL:
				log.fatal(message,e);
				return;
			default:
				log.error("Unkown level:"+level);
				log.error(message,e);
				return;
			}
		}
		
	}
	
	

//	public static final NotificationImplInterface.Level DEBUG=new NotificationImplInterface.Level() {		
//		@Override
//		public void notify(String message, Throwable e) {
//			log.debug(message,e);
//		}
//		
//		@Override
//		public boolean isEnabled() {
//			return log.isDebugEnabled();
//		}
//	};
//
//	public static final NotificationImplInterface.Level INFO=new NotificationImplInterface.Level() {		
//		@Override
//		public void notify(String message, Throwable e) {
//			log.info(message,e);
//		}
//		
//		@Override
//		public boolean isEnabled() {
//			return log.isInfoEnabled();
//		}
//	};
//
//	public static final NotificationImplInterface.Level WARN=new NotificationImplInterface.Level() {		
//		@Override
//		public void notify(String message, Throwable e) {
//			log.warn(message,e);
//		}
//		
//		@Override
//		public boolean isEnabled() {
//			return true;
//		}
//	};
//
//
//	public static final NotificationImplInterface.Level ERROR=new NotificationImplInterface.Level() {		
//		@Override
//		public void notify(String message, Throwable e) {
//			log.error(message,e);
//		}
//		
//		@Override
//		public boolean isEnabled() {
//			return true;
//		}
//	};
//
//
//	public static final NotificationImplInterface.Level FATAL=new NotificationImplInterface.Level() {		
//		@Override
//		public void notify(String message, Throwable e) {
//			log.fatal(message,e);
//		}
//		
//		@Override
//		public boolean isEnabled() {
//			return true;
//		}
//	};
//
//
//
//	@Override
//	public Level getDebug() {
//		return DEBUG;
//	}
//
//	@Override
//	public Level getInfo() {
//		return INFO;
//	}
//
//	@Override
//	public Level getWarn() {
//		return WARN;
//	}
//
//	@Override
//	public Level getError() {
//		return ERROR;
//	}
//
//	@Override
//	public Level getFatal() {
//		return FATAL;
//	}	
}
