package collector;

public class SensorData {
	
	public long time;
	public String timeStamp;
	public double accX; // m/s^2
	public double accY;
	public double accZ;
	
	public double azimuth; // degree
	public double pitch;
	public double roll;
	
	public double rotX; // rad/s
	public double rotY;
	public double rotZ;
	
	
	public void roundData(){
		accX = round(accX);
		accY = round(accY);
		accZ = round(accZ);
		azimuth = round(azimuth);
		pitch = round(pitch);
		roll = round(roll);
		rotX = round(rotX);
		rotY = round(rotY);
		rotZ = round(rotZ);
	}
	

	public String getHeader(){
		String string="";
		string+="time";
		string+=",";
		
		string+="accX";
		string+=",";
		
		string+="accY";
		string+=",";

		string+="accZ";
		string+=",";

		string+="azimuth";
		string+=",";

		string+="pitch";
		string+=",";

		string+="roll";
		string+=",";

		string+="rotX";
		string+=",";

		string+="rotY";
		string+=",";

		string+="rotZ";
		
		return string;
	}
	
	public String getSerialization(){
		String string="";
		string+=timeStamp;
		string+=",";
		
		string+=accX;
		string+=",";
		
		string+=accY;
		string+=",";

		string+=accZ;
		string+=",";

		string+=azimuth;
		string+=",";

		string+=pitch;
		string+=",";

		string+=roll;
		string+=",";

		string+=rotX;
		string+=",";

		string+=rotY;
		string+=",";

		string+=rotZ;
		
		return string;
	} 
	

	
	public static double round(double v){
		return Math.round(v * 1000000.0) / 1000000.0;
	}
	
}
