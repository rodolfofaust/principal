package br.com.sankhya.ctba.integracaoapi;

public class Phone {
	  String phone;
	  String type;
	  
	  
	public Phone(String phone, String type) {
		this.phone = phone;
		this.type = type;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	  
	  
}
