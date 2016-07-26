package security.notification;

import java.util.List;

import security.util.AppEnum;

public class Level extends AppEnum{
	
	private static final long serialVersionUID = 1L;

	static final int FATAL_LEVEL = 0;
	static final int ERROR_LEVEL = FATAL_LEVEL+1;
	static final int WARN_LEVEL = ERROR_LEVEL+1;
	static final int INFO_LEVEL = WARN_LEVEL+1;
	static final int DEBUG_LEVEL = INFO_LEVEL+1;
	
	public static final Level FATAL = new Level("FATAL",FATAL_LEVEL);
	public static final Level ERROR = new Level("ERROR",ERROR_LEVEL);
	public static final Level WARN = new Level("WARN",WARN_LEVEL);
	public static final Level INFO = new Level("INFO",INFO_LEVEL);
	public static final Level DEBUG = new Level("DEBUG",DEBUG_LEVEL);

	final int level;

	private Level(String id,int level){
		super(id,id,id);
		this.level=level;
	}

	public static Level value(String value) {
		return  (Level)AppEnum.value(Level.class, value);			
	}

	public static List<AppEnum> values() {
		return AppEnum.values(Level.class);
	}


}
