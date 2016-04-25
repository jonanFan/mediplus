package es.mediplus.mediplus;

import java.io.Serializable;

/**
 * Created by jon on 20/04/16.
 */
public class Cita implements Serializable {
    private int startYear=-1, startMonth=-1, startDay=-1, startHour=-1, startMinute=-1;
    private int stopYear=-1, stopMonth=-1, stopDay=-1, stopHour=-1, stopMinute=-1;
    private String eventName="", description="", location="";
    private String idGoogle="";
    private int idLocal=-1;

    Cita(String idGoogle)
    {
        this.idGoogle=idGoogle;
    }

    Cita(String eventName, String description, String location)
    {
        this.eventName=eventName;
        this.description=description;
        this.location=location;
    }

    Cita(String eventName, String description, String location, String idGoogle)
    {
        this.idGoogle=idGoogle;
        this.eventName=eventName;
        this.description=description;
        this.location=location;
    }

    protected void restartTime() {
        this.startYear = -1;
        this.startMonth = -1;
        this.startDay = -1;
        this.startHour = -1;
        this.startMinute = -1;

        this.stopYear = -1;
        this.stopMonth = -1;
        this.stopDay = -1;
        this.stopHour = -1;
        this.stopMinute = -1;
    }

    protected void setTime(String fechaInicio, String fechaFinal, boolean isGoogle, boolean fullDay) {
        String[] inicio, fin;
        // Log.d("asd","Entramos a establecer el tiempo, la fecha inicio es "+fechaInicio+" y la final es "+fechaFinal+" y el booleano es "+isGoogle);
        if (isGoogle) {
            inicio = fechaInicio.split("-|T|:");
            fin = fechaFinal.split("-|T|:");
        } else {
            inicio = fechaInicio.split("-| |:");
            fin = fechaFinal.split("-| |:");
        }

        this.startYear = Integer.parseInt(inicio[0]);
        this.startMonth = Integer.parseInt(inicio[1]) - 1;
        this.startDay = Integer.parseInt(inicio[2]);
        this.startHour = Integer.parseInt(inicio[3]);
        this.startMinute = Integer.parseInt(inicio[4]);

        // Log.d("asd","El inicio es "+startYear+"-"+startMonth+"-"+startDay+" "+startHour+":"+startMinute);

        this.stopYear = Integer.parseInt(fin[0]);
        this.stopMonth = Integer.parseInt(fin[1]) - 1;
        if (fullDay)
            this.stopDay = Integer.parseInt(fin[2]) - 1;
        else
            this.stopDay = Integer.parseInt(fin[2]);

        this.stopHour = Integer.parseInt(fin[3]);
        this.stopMinute = Integer.parseInt(fin[4]);
        // Log.d("asd","El final es "+stopYear+"-"+stopMonth+"-"+stopDay+" "+stopHour+":"+stopMinute);

    }

    public int getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(int idLocal) {
        this.idLocal = idLocal;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIdGoogle() {
        return idGoogle;
    }

    public void setIdGoogle(String idGoogle) {
        this.idGoogle = idGoogle;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(int startMonth) {
        this.startMonth = startMonth;
    }

    public int getStartDay() {
        return startDay;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getStopYear() {
        return stopYear;
    }

    public void setStopYear(int stopYear) {
        this.stopYear = stopYear;
    }

    public int getStopMonth() {
        return stopMonth;
    }

    public void setStopMonth(int stopMonth) {
        this.stopMonth = stopMonth;
    }

    public int getStopDay() {
        return stopDay;
    }

    public void setStopDay(int stopDay) {
        this.stopDay = stopDay;
    }

    public int getStopHour() {
        return stopHour;
    }

    public void setStopHour(int stopHour) {
        this.stopHour = stopHour;
    }

    public int getStopMinute() {
        return stopMinute;
    }

    public void setStopMinute(int stopMinute) {
        this.stopMinute = stopMinute;
    }
}
