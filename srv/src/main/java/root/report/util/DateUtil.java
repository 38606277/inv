package root.report.util;

public class DateUtil {
	public static String getMonthAbbr(int month){
		String abbr = "";
		switch(month){
			case 1: abbr = "JAN";break;
			case 2: abbr = "FEB";break;
			case 3: abbr = "MAR";break;
			case 4: abbr = "APR";break;
			case 5: abbr = "MAY";break;
			case 6: abbr = "JUN";break;
			case 7: abbr = "JUL";break;
			case 8: abbr = "AUG";break;
			case 9: abbr = "SEP";break;
			case 10: abbr = "OCT";break;
			case 11: abbr = "NOV";break;
			case 12: abbr = "DEC";break;
		}
		return abbr;
	}
	
	public static String getEnMonth(int month){
		String abbr = "";
		switch(month){
			case 1: abbr = "01";break;
			case 2: abbr = "02";break;
			case 3: abbr = "03";break;
			case 4: abbr = "04";break;
			case 5: abbr = "05";break;
			case 6: abbr = "06";break;
			case 7: abbr = "07";break;
			case 8: abbr = "08";break;
			case 9: abbr = "09";break;
			case 10: abbr = "10";break;
			case 11: abbr = "11";break;
			case 12: abbr = "12";break;
		}
		return abbr;
	}
	public static String getNextEnMonth(int month){
		String abbr = "";
		switch(month){
			case 1: abbr = "02";break;
			case 2: abbr = "03";break;
			case 3: abbr = "04";break;
			case 4: abbr = "05";break;
			case 5: abbr = "06";break;
			case 6: abbr = "07";break;
			case 7: abbr = "08";break;
			case 8: abbr = "09";break;
			case 9: abbr = "10";break;
			case 10: abbr = "11";break;
			case 11: abbr = "12";break;
			case 12: abbr = "01";break;
		}
		return abbr;
	}
}
