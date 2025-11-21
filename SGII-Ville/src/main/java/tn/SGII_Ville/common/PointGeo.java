package tn.SGII_Ville.common;

public class PointGeo {

	    private float latitude;
	    private float longitude;
	    private String content; // Contenu textuel si nécessaire

	    // Constructeurs
	    public PointGeo() {}

	    public PointGeo(float latitude, float longitude) {
	        this.latitude = latitude;
	        this.longitude = longitude;
	    }

	    public PointGeo(float latitude, float longitude, String content) {
	        this.latitude = latitude;
	        this.longitude = longitude;
	        this.content = content;
	    }

	    // Getters et Setters
	    public float getLatitude() {
	        return latitude;
	    }

	    public void setLatitude(float latitude) {
	        this.latitude = latitude;
	    }

	    public float getLongitude() {
	        return longitude;
	    }

	    public void setLongitude(float longitude) {
	        this.longitude = longitude;
	    }

	    public String getContent() {
	        return content;
	    }

	    public void setContent(String content) {
	        this.content = content;
	    }

	    @Override
	    public String toString() {
	        return "PointGeo{" +
	                "latitude=" + latitude +
	                ", longitude=" + longitude +
	                ", content='" + content + '\'' +
	                '}';
	    }
	
}
