package fast.common.utilities;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import fast.common.logging.FastLogger;

public class BusinessDaysInfo extends Object {
	private FastLogger logger=FastLogger.getLogger(BusinessDaysInfo.class.getName());
	
	private boolean excludeSunday = true;
	private boolean excludeMonday = false;
	private boolean excludeTuesday = false;
	private boolean excludeWednesday = false;
	private boolean excludeThursday = false;
	private boolean excludeFriday = false;
	private boolean excludeSaturday = true;

    private ArrayList<Date> holidays = new ArrayList<>();

    public List<String> getHolidaysList(){
        DateFormat shortFormat = DateFormat.getDateInstance(DateFormat.SHORT);
    	
            ArrayList<String> list = new ArrayList<>();
            for (Date dt : holidays){
                list.add(shortFormat.format(dt));
            }
            return list;        
    }   

    private boolean loadAppend = false;
    private String holidayFilename;

    public void loadHolidaysList(String holidayStringList) throws ParseException
    {
    	DateFormat shortFormat=DateFormat.getDateInstance(DateFormat.SHORT);
        if (!loadAppend)
            holidays.clear();
        Date dtHoliday;
        for (String str : holidayStringList.split(","))
        {
            try {
            	dtHoliday=shortFormat.parse(str);
            	holidays.add(dtHoliday);
			} catch (ParseException exception) {
				logger.error(exception .toString());
			}               
        }
    }

	public String addBusinessDays(Date date, int days)
    {
    	if(date==null){
    		date=new Date();
    	}
    	SimpleDateFormat shortFormat = new SimpleDateFormat("MM/dd/yyyy");

        if (holidays == null)
            holidays = new ArrayList<>();

        boolean skipDay=false;
        while (days > 0)
        {
        	Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DAY_OF_MONTH, 1);
            date = c.getTime();
            skipDay=false;            
            switch(DayOfWeek.getDayOfWeek(c.get(Calendar.DAY_OF_WEEK)))
            {
                case SUNDAY:
                    skipDay = excludeSunday;
                    break;
                case MONDAY:
                    skipDay = excludeMonday;
                    break;
                case TUESDAY:
                    skipDay = excludeTuesday;
                    break;
                case WEDNESDAY:
                    skipDay = excludeWednesday;
                    break;
                case THURSDAY:
                    skipDay = excludeThursday;
                    break;
                case FRIDAY:
                    skipDay = excludeFriday;
                    break;
                case SATURDAY:
                    skipDay = excludeSaturday;
                    break;
                default:
                    skipDay = false;
                    break;
            }
            if(holidays.indexOf(date)!=-1)
            {
                skipDay = true;
            }
            if (!skipDay)
                days--;            
        }
        return shortFormat.format(date);
    }

    @Override
    public boolean equals(Object other)
    {
    	if(other==null || other.getClass()!=BusinessDaysInfo.class){
    		return false;
    	}
    	BusinessDaysInfo info=(BusinessDaysInfo) other; 
        boolean isEqual = false;
        isEqual = excludeSunday == info.excludeSunday &&
            excludeMonday == info.excludeMonday &&
            excludeThursday == info.excludeThursday &&
            excludeWednesday == info.excludeWednesday &&
            excludeTuesday == info.excludeTuesday &&
            excludeFriday == info.excludeFriday &&
            excludeSaturday == info.excludeSaturday &&
            holidayFilename == info.holidayFilename;

        return isEqual;
    }
    
    @Override
    public int hashCode(){
    	return super.hashCode();
    }

    public Object copy()
    {
        BusinessDaysInfo bdi = new BusinessDaysInfo();
        bdi.holidayFilename = holidayFilename;
        bdi.excludeMonday = excludeMonday;
        bdi.excludeTuesday = excludeTuesday;
        bdi.excludeWednesday = excludeWednesday;
        bdi.excludeThursday = excludeThursday;
        bdi.excludeFriday = excludeFriday;
        bdi.excludeSaturday = excludeSaturday;
        bdi.excludeSunday = excludeSunday;
        return bdi;
    }
}


enum DayOfWeek{
	
    SUNDAY ( 0),
   
    MONDAY ( 1),
   
    TUESDAY ( 2),
   
    WEDNESDAY ( 3),
  
    THURSDAY ( 4),
 
    FRIDAY ( 5),
 
    SATURDAY ( 6),
    
    NONE(-1);
    
    private int day;
    private DayOfWeek(int day){
    	this.day=day;
    }
    
    public static DayOfWeek getDayOfWeek(int day){
    	for(DayOfWeek dayOfWeek :DayOfWeek.values()){
    		if(dayOfWeek.day==day){
    			return dayOfWeek;
    		}
    	}
    	return DayOfWeek.NONE;
    }
}
