package com.tyczj.extendedcalendarview;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CalendarAdapter extends BaseAdapter{

	static final int FIRST_DAY_OF_WEEK =1; //ESTABA A 0
	Context context;
	Calendar cal;
	public String[] days;
//	OnAddNewEventClick mAddEvent;
	
	ArrayList<Day> dayList = new ArrayList<Day>();
	
	public CalendarAdapter(Context context, Calendar cal){
		this.cal = cal;
		this.context = context;
		cal.set(Calendar.DAY_OF_MONTH, 1);
		refreshDays();
	}

	@Override
	public int getCount() {
		return days.length;
	}

	@Override
	public Object getItem(int position) {
		return dayList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	public int getPrevMonth(){
		if(cal.get(Calendar.MONTH) == cal.getActualMinimum(Calendar.MONTH)){
			cal.set(Calendar.YEAR, cal.get( Calendar.YEAR-1));
		}

		int month = cal.get(Calendar.MONTH);
		if(month == 0){
			return 11;
		}
		
		return month-1;
	}
	
	public int getMonth(){
		return cal.get(Calendar.MONTH);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View v = convertView;
		LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if(position >= 0 && position < 7){
			v = vi.inflate(R.layout.day_of_week, null);
			TextView day = (TextView)v.findViewById(R.id.textView1);
			
			if(position == 0){
				day.setText(R.string.monday);
			}else if(position == 1){
				day.setText(R.string.tuesday);
			}else if(position == 2){
				day.setText(R.string.wednesday);
			}else if(position == 3){
				day.setText(R.string.thursday);
			}else if(position == 4){
				day.setText(R.string.friday);
			}else if(position == 5){
				day.setText(R.string.saturday);
			}else if(position == 6){
				day.setText(R.string.sunday);
			}

        }else{
			
	        v = vi.inflate(R.layout.day_view, null);
			FrameLayout today = (FrameLayout)v.findViewById(R.id.today_frame);
			Calendar cal = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
			Day d = dayList.get(position);
			if(d.getYear() == cal.get(Calendar.YEAR) && d.getMonth() == cal.get(Calendar.MONTH) && d.getDay() == cal.get(Calendar.DAY_OF_MONTH)){
				today.setVisibility(View.VISIBLE);
			}else{
				today.setVisibility(View.GONE);
			}
			
			TextView dayTV = (TextView)v.findViewById(R.id.textView1);
			
			RelativeLayout rl = (RelativeLayout)v.findViewById(R.id.rl);
			ImageView iv = (ImageView)v.findViewById(R.id.imageView1);
			ImageView blue = (ImageView)v.findViewById(R.id.imageView2);
			ImageView purple = (ImageView)v.findViewById(R.id.imageView3);
			ImageView green = (ImageView)v.findViewById(R.id.imageView4);
			ImageView orange = (ImageView)v.findViewById(R.id.imageView5);
			ImageView red = (ImageView)v.findViewById(R.id.imageView6);	
			
			blue.setVisibility(View.VISIBLE);
			purple.setVisibility(View.VISIBLE);
			green.setVisibility(View.VISIBLE);
			purple.setVisibility(View.VISIBLE);
			orange.setVisibility(View.VISIBLE);
			red.setVisibility(View.VISIBLE);
			
			iv.setVisibility(View.VISIBLE);
			dayTV.setVisibility(View.VISIBLE);
			rl.setVisibility(View.VISIBLE);
			
			Day day = dayList.get(position);
			
			if(day.getNumOfEvenets() > 0){
				Set<Integer> colors = day.getColors();
				
				iv.setVisibility(View.INVISIBLE);
				blue.setVisibility(View.INVISIBLE);
				purple.setVisibility(View.INVISIBLE);
				green.setVisibility(View.INVISIBLE);
				purple.setVisibility(View.INVISIBLE);
				orange.setVisibility(View.INVISIBLE);
				red.setVisibility(View.INVISIBLE);
				
				if(colors.contains(0)){
					iv.setVisibility(View.VISIBLE);
				}
				if(colors.contains(2)){
					blue.setVisibility(View.VISIBLE);
				}
				if(colors.contains(4)){
					purple.setVisibility(View.VISIBLE);
				}
				if(colors.contains(5)){
					green.setVisibility(View.VISIBLE);
				}
				if(colors.contains(3)){
					orange.setVisibility(View.VISIBLE);
				}
				if(colors.contains(1)){
					red.setVisibility(View.VISIBLE);
				}
				
			}else{
				iv.setVisibility(View.INVISIBLE);
				blue.setVisibility(View.INVISIBLE);
				purple.setVisibility(View.INVISIBLE);
				green.setVisibility(View.INVISIBLE);
				purple.setVisibility(View.INVISIBLE);
				orange.setVisibility(View.INVISIBLE);
				red.setVisibility(View.INVISIBLE);
			}
				
			if(day.getDay() == 0){
				rl.setVisibility(View.GONE);
			}else{
				dayTV.setVisibility(View.VISIBLE);
				dayTV.setText(String.valueOf(day.getDay()));
			}
		}
		
		return v;
	}
	//TODO MIRAR ESTO PORQUE FALLA MAYO -> OJO PATADA FALLA AL CAMBIAR QUE HAGA EL CALCULO EN FORMATO ESPAÑOL
    //TODO MAYO deberia tener 12 vacios delante y un total de 43

	public void refreshDays()
    {
    	// clear items
    	dayList.clear();
    	
    	int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)+7;
        int firstDay = (int)cal.get(Calendar.DAY_OF_WEEK);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        TimeZone tz = TimeZone.getDefault();
		Log.d("asd", "Last day es "+lastDay+" first day es "+firstDay+" year es "+year+" month es"+month);

		// figure size of the array
        if(firstDay==1){
        	days = new String[lastDay+(FIRST_DAY_OF_WEEK*6)];
        }
        else {
        	days = new String[lastDay+firstDay-(FIRST_DAY_OF_WEEK+1)];
        }
        Log.d("asd", "Los dias son "+days.length);
        int j=FIRST_DAY_OF_WEEK;
        
        // populate empty days before first real day
        if(firstDay>1) {
	        for(j=0;j<(firstDay-FIRST_DAY_OF_WEEK)+7;j++) {
                Log.d("asd","Estamos en el de arriba: "+j);
	        	days[j] = "";
	        	Day d = new Day(context,0,0,0);
	        	dayList.add(d);
	        }
        }
	    else {
			int fin=FIRST_DAY_OF_WEEK*6; //TODO POSIBLE APAÑO PARA EL TOPE DE ARRIBA
			if(FIRST_DAY_OF_WEEK==1)
				fin++;
	    	for(j=0;j<(fin)+7;j++) {
                Log.d("asd","Estamos en el de abajo: "+j);
                days[j] = "";
	        	Day d = new Day(context,0,0,0);
	        	dayList.add(d);
	        }
	    	if(FIRST_DAY_OF_WEEK==0)
				j=FIRST_DAY_OF_WEEK*6+1; // sunday => 1, monday => 7
        }
        
        // populate days
        int dayNumber = 1;

        if(j>0 && dayList.size() > 0 && j != 1){
        	dayList.remove(j-1);
        }

        Log.d("asd","J vale "+j);
        for(int i=j-1;i<days.length;i++) {
        	Day d = new Day(context,dayNumber,year,month);
        	
        	Calendar cTemp = Calendar.getInstance();
        	cTemp.set(year, month, dayNumber);
        	int startDay = Time.getJulianDay(cTemp.getTimeInMillis(), TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(cTemp.getTimeInMillis())));
        	
        	d.setAdapter(this);
        	d.setStartDay(startDay);

            //Log.d("asd", "La i es "+i+" y el dayNumber es "+dayNumber);
        	days[i] = ""+dayNumber;
        	dayNumber++;
        	dayList.add(d);
        }

       Log.d("asd", "VISUALIZA DAYS");
        for(int i=0; i < days.length; i++)
           Log.d("asd","La i es "+i+" y el dia "+days[i]);
    }
	
//	public abstract static class OnAddNewEventClick{
//		public abstract void onAddNewEventClick();
//	}
	
}
