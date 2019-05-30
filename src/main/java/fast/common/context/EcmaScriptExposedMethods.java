package fast.common.context;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import fast.common.fix.FixHelper;
import fast.common.logging.FastLogger;

/**
 * Created by ao94803 on 4/27/2017. Static methods added here and bound at
 * EcmaScriptInterpreter.exposeJavaMethods() will be visible in JS code embedded
 * in Cucumber
 *
 */
public class EcmaScriptExposedMethods {
	private static FastLogger _logger = FastLogger.getLogger("EcmaScriptExposedMethods");
	private static final FastDateFormat eodTimeFormatter;
	static {
		eodTimeFormatter = FastDateFormat.getInstance("yyyyMMdd-17:00:00.000", TimeZone.getTimeZone("UTC"));
	}
	private static final FastDateFormat hrTimeFormatter;
	static {
		hrTimeFormatter = FastDateFormat.getInstance("yyyyMMdd-HH:mm:ss", TimeZone.getTimeZone("UTC"));
	}

	public static String eod() {
		return eodTimeFormatter.format(new Date());
	}

	public static String inhour() {
		Calendar ca = Calendar.getInstance();
		ca.setTime(new Date());
		ca.add(Calendar.HOUR, +2);

		Date date = ca.getTime();
		return hrTimeFormatter.format(date);
	}

	public static String timestamp() {
		return FixHelper.getTransactTimeStr();
	}

	public static String unique() {
		return FixHelper.generateClOrdID();
	}

	static String readNum() {
		String result = "";

		String fl = System.getProperty("user.dir") + "/flag";

		BufferedReader reader = null;
		FileReader fr = null;

		try {
			fr = new FileReader(fl);
			reader = new BufferedReader(fr);

			result = reader.readLine();

		} catch (Exception ex) {
			_logger.error("Failed to read file with exception :\n" + ex.getMessage());
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (fr != null) {
					fr.close();
				}

			} catch (Exception x) {
			}

		}

		return result;
	}

	public static int sumUp(String st) {
		String[] toSum = st.split("\\+");
		int result = 0;

		for (String ar : toSum) {
			try {
				int add = Integer.parseInt(ar);
				result = result + add;

			} catch (Exception x) {
				result = -1;
				break;
			}
		}

		return result;
	}

	public static String isgmt(String tstamp) {
		String result = tstamp;

		String FORMAT = "yyyyMMdd-HH:mm:ss.SSS";
		DateFormat df = new SimpleDateFormat(FORMAT);
		try {
			Date d = df.parse(tstamp);
			Calendar tsc = Calendar.getInstance();
			tsc.setTime(d);
			int tsh = tsc.get(Calendar.HOUR_OF_DAY);

			Calendar nw = Calendar.getInstance();
			nw.setTime(d);
			int nwh = nw.get(Calendar.HOUR_OF_DAY);

			if (tsh != nwh) {
				result = "false";
			}

		} catch (Exception x) {
			result = "false";
		}

		return result;
	}

	public static String threeDecPlaces(String s) {
		String result = "wrong";
		try {
			String dec = s.split("\\.")[1];
			int re = dec.length();

			if (re > 2) {
				result = s;
			}
		} catch (Exception ex) {
			result = "wrong";
		}

		return result;
	}

	public static double sumDouble(String st) {
		String[] toSum = st.split("\\+");
		double result = 0;

		for (String ar : toSum) {
			try {
				double add = Double.parseDouble(ar);
				result = result + add;

			} catch (Exception x) {
				result = -1;
				break;
			}
		}

		return result;
	}

	public static String addSideBased(String pr) {

		String[] temp = pr.split(",");
		String price = temp[0];
		int places = Integer.parseInt(temp[1]);
		double dc = Double.parseDouble(temp[2]);
		String side = temp[3];
		String pri[] = price.split("\\.");
		String cel = pri[0];
		String result = cel + ".";
		String dec = pri[1];
		char[] ch = dec.toCharArray();

		for (int i = 0; i < places; i++) {
			result = result + ch[i];
		}

		DecimalFormat df = new DecimalFormat("#.#");
		df.setRoundingMode(RoundingMode.CEILING);
		double rez = 0;

		switch (side) {
		case "BUY": {
			rez = Double.parseDouble(result) + dc;
			break;
		}
		case "SELL": {
			rez = Double.parseDouble(result) - dc;
		}

		}

		try {
			result = df.format(rez);

		} catch (Exception x) {
			result = Double.toString(rez);
		}

		return result;
	}

	public static String truncadd(String pr) {

		String[] temp = pr.split(",");
		String price = temp[0];
		int places = Integer.parseInt(temp[1]);
		double dc = Double.parseDouble(temp[2]);
		String pri[] = price.split("\\.");
		String cel = pri[0];
		String result = cel + ".";
		String dec = pri[1];
		char[] ch = dec.toCharArray();

		for (int i = 0; i < places; i++) {
			result = result + ch[i];
		}

		DecimalFormat df = new DecimalFormat("#.#");
		df.setRoundingMode(RoundingMode.CEILING);
		double rez = Double.parseDouble(result) + dc;

		try {
			result = df.format(rez);

		} catch (Exception x) {
			result = Double.toString(rez);
		}

		return result;
	}

	public static String trunc(String pr) {

		String[] temp = pr.split(",");
		String price = temp[0];
		int places = Integer.parseInt(temp[1]);

		String pri[] = price.split("\\.");
		String cel = pri[0];
		String result = cel + ".";
		String dec = pri[1];
		char[] ch = dec.toCharArray();

		for (int i = 0; i < places; i++) {
			result = result + ch[i];
		}

		return result;
	}

	public static String changed(String value) {
		String[] vals = value.split("_");

		String newvalue = vals[0];
		String previous = vals[1];
		String result = newvalue;
		if (newvalue.equals(previous)) {
			result = "same";
		}

		return result;
	}

	public static String notzero(String value) {

		String newvalue = value;
		String previous = "0";
		String result = newvalue;
		if (newvalue.equals(previous)) {
			result = "same";
		}

		return result;
	}

	public static String GetClOrdIDForToday(int s) {

		String inc = Integer.toString(Integer.parseInt(readNum()) + 1);

		Date day = DateUtils.truncate(new Date(), java.util.Calendar.DAY_OF_MONTH);
		return day.getTime() + inc + Integer.toString(s);
	}

	public static Date addSecondsToNow(int seconds) {
		return DateUtils.addSeconds(new Date(), seconds);
	}	
}
