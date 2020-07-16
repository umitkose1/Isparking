package com.codebugfix.isparking.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IsparkDetail {

    @SerializedName("ParkID")
    @Expose
    private Integer parkID;
    @SerializedName("ParkAdi")
    @Expose
    private String parkAdi;
    @SerializedName("Latitude")
    @Expose
    private String latitude;
    @SerializedName("Longitude")
    @Expose
    private String longitude;
    @SerializedName("Kapasitesi")
    @Expose
    private Integer kapasitesi;
    @SerializedName("BosKapasite")
    @Expose
    private Integer bosKapasite;
    @SerializedName("ParkTipi")
    @Expose
    private String parkTipi;
    @SerializedName("Ilce")
    @Expose
    private String ilce;
    @SerializedName("GuncellemeTarihi")
    @Expose
    private String guncellemeTarihi;
    @SerializedName("CalismaSaatleri")
    @Expose
    private String calismaSaatleri;
    @SerializedName("UcretsizParklanmaDk")
    @Expose
    private Integer ucretsizParklanmaDk;
    @SerializedName("AylikAbonelikUcreti")
    @Expose
    private Integer aylikAbonelikUcreti;
    @SerializedName("Adres")
    @Expose
    private String adres;

    @SerializedName("LokasyonAdi")
    @Expose
    private String lokasyonAdi;

    public Integer getParkID() {
        return parkID;
    }

    public void setParkID(Integer parkID) {
        this.parkID = parkID;
    }

    public String getParkAdi() {
        return parkAdi;
    }

    public void setParkAdi(String parkAdi) {
        this.parkAdi = parkAdi;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Integer getKapasitesi() {
        return kapasitesi;
    }

    public void setKapasitesi(Integer kapasitesi) {
        this.kapasitesi = kapasitesi;
    }

    public Integer getBosKapasite() {
        return bosKapasite;
    }

    public void setBosKapasite(Integer bosKapasite) {
        this.bosKapasite = bosKapasite;
    }

    public String getParkTipi() {
        return parkTipi;
    }

    public void setParkTipi(String parkTipi) {
        this.parkTipi = parkTipi;
    }

    public String getIlce() {
        return ilce;
    }

    public void setIlce(String ilce) {
        this.ilce = ilce;
    }

    public String getGuncellemeTarihi() {
        return guncellemeTarihi;
    }

    public void setGuncellemeTarihi(String guncellemeTarihi) {
        this.guncellemeTarihi = guncellemeTarihi;
    }

    public String getCalismaSaatleri() {
        return calismaSaatleri;
    }

    public void setCalismaSaatleri(String calismaSaatleri) {
        this.calismaSaatleri = calismaSaatleri;
    }

    public Integer getUcretsizParklanmaDk() {
        return ucretsizParklanmaDk;
    }

    public void setUcretsizParklanmaDk(Integer ucretsizParklanmaDk) {
        this.ucretsizParklanmaDk = ucretsizParklanmaDk;
    }

    public Integer getAylikAbonelikUcreti() {
        return aylikAbonelikUcreti;
    }

    public void setAylikAbonelikUcreti(Integer aylikAbonelikUcreti) {
        this.aylikAbonelikUcreti = aylikAbonelikUcreti;
    }

    public String getAdres() {
        return adres;
    }

    public void setAdres(String adres) {
        this.adres = adres;
    }

    public String getLokasyonAdi() {
        return lokasyonAdi;
    }

    public void setLokasyonAdi(String lokasyonAdi) {
        this.lokasyonAdi = lokasyonAdi;
    }

}
