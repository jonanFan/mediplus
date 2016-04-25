package es.mediplus.mediplus;


import java.io.Serializable;


public class Medicamento implements Serializable {

    //@Id
    private int idMedicamentos=-1;

    public int getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(int idLocal) {
        this.idLocal = idLocal;
    }

    private int idLocal;
    private String nombre;
    private String fechaInicial;
    private String fechaFinal;
    private String fechaReposicion;
    private int cantidadCompra;
    //a elegir cada cuantos días
    private String frecuenciaDiaria;//ini + dias (todas las semanas desde fechaInicial a fechaFinal)
    //a elegir cada cuanto en un día
    private String horaInicial;//ini + frec horaria
    private String frecuenciaHoraria;
    private String timeStamp;
    private int flag;
    // este es el campo que me teneis que sincronizar
    private int numAlarmas=0;



//*             GETTERS Y SETTERS             *//

    public int getIdMedicamentos() {
        return idMedicamentos;
    }

    public void setIdMedicamentos(int idMedicamentos) {
        this.idMedicamentos = idMedicamentos;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCantidadCompra() {
        return cantidadCompra;
    }

    public void setCantidadCompra(int cantidadCompra) {
        this.cantidadCompra = cantidadCompra;
    }

    public String getFechaReposicion() {
        return fechaReposicion;
    }

    public void setFechaReposicion(String fechaReposicion) {
        this.fechaReposicion = fechaReposicion;
    }

    public String getFechaInicial() {
        return fechaInicial;
    }

    public void setFechaInicial(String fechaInicial) {
        this.fechaInicial = fechaInicial;
    }

    public String getFechaFinal() {
        return fechaFinal;
    }

    public String getFrecuenciaDiaria() {
        return frecuenciaDiaria;
    }

    public void setFrecuenciaDiaria(String frecuenciaDiaria) {
        this.frecuenciaDiaria = frecuenciaDiaria;
    }

    public String getFrecuenciaHoraria() {
        return frecuenciaHoraria;
    }

    public void setFrecuenciaHoraria(String frecuenciaHoraria) {
        this.frecuenciaHoraria = frecuenciaHoraria;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setFechaFinal(String fechaFinal) {
        this.fechaFinal = fechaFinal;

    }

    public String getHoraInicial() {
        return horaInicial;
    }

    public void setHoraInicial(String horaInicial) {
        this.horaInicial = horaInicial;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getNumAlarmas() {
        return numAlarmas;
    }

    public void setNumAlarmas(int numAlarmas) {
        this.numAlarmas = numAlarmas;
    }
}
