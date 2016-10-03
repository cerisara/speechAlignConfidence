package mfcc;

public class MfccFrame {
	public static final int mfccCoeffCount = 39;
	private double[] mfccCoeff;
	private int numero;
	private String filename;
	
	public MfccFrame(){
		mfccCoeff = new double[mfccCoeffCount];
		numero = -1;
		filename="";
	}
	
	public MfccFrame(String coeffList, int numero, String filename){
		this.numero = numero;
		mfccCoeff = new double[mfccCoeffCount];
		setMfccCoef(coeffList);
		this.filename = filename;
	}
	
	public boolean setMfccCoef(String coeffList){
		String[] list = coeffList.split(";");
		if(list.length != 39){
			System.out.println("Wrong amount of coefficients in string : "
								+list.length +  " instead of "+ mfccCoeffCount + ".");
			return false;
		}
		for(int i =0; i < list.length; ++i){
			mfccCoeff[i] = Double.parseDouble(list[i]);
		}
		return true;
	}
	
	public String toString(){
		String res = "" + mfccCoeff[0];
		for(int i=1; i < mfccCoeff.length ; ++i){
			res += ";" + mfccCoeff[i];
		}
		return res;
	}
	
	public int getNumero(){
		return numero;
	}
	
	public void setNumero(int n){
		numero = n;
	}
	
	public int hashCode(){
		String res = filename + numero;
		return res.hashCode();
	}
	
	public String dumpToString(){
		String res = "" + this.hashCode() + ";" + filename + ";" + numero + "\n";
		res += this.toString();
		return res;
	}

}
