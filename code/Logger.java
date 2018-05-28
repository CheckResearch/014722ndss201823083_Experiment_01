import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Logger {

	/**
	 * Logger Class.
	 * 
	 * Levels:
	 * 
	 * DEBUG - 1:
	 * Things that are only relevant if you are the Developer debugging the application.
	 * 
	 * INFO - 2:
	 * Everything that is printed in "verbose" mode. (i.e. "Good to know")
	 * 
	 * REPORT - 3:
	 * User should be Informed
	 * 
	 * FATAL - 4:
	 * Errors that make further execution of the application impossible.
	 * 

	 */

	public static final int DEBUG = 1;
	public static final int INFO = 2;
	public static final int REPORT = 3;
	public static final int FATAL = 4;
	
	private static Logger log=null;
	private int logLevel = 0;

	private Logger () {
		super();
	}
	
	public static Logger getInstance()  {
		if(log==null)
			log=new Logger();
		
		return log;
	}
	


	public int getLevel() {
		return logLevel;
	}

	public void setLevel(int level) {
		this.logLevel = level;
	}

	public String getTimeStamp() {
		DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		Calendar calobj = Calendar.getInstance();
		return df.format(calobj.getTime());
	}

	public void DEBUG(String Message) {
		if(this.logLevel<=this.DEBUG)
			System.out.println(getTimeStamp() + " - " + Message);
	}

	public void INFO(String Message) {
		if(this.logLevel<=this.INFO)
			System.out.println(getTimeStamp() + " - " + Message);

	}

	public void REPORT(String Message) {
		if(this.logLevel<=this.REPORT)
			System.out.println(getTimeStamp() + " - " + Message);
	}

	public void FATAL(String Message) {
		if(this.logLevel<=this.FATAL)
			System.out.println(getTimeStamp() + " - " + Message);
	}
}
