package br.com.sankhya.ctba.integracaoapi;

import java.util.ArrayList;
import java.util.List;

public class Contact {

    String name;
    String title;
    String email;
    List<Phone> phones = new ArrayList<>();
    
  
	public Contact(String name, String title, String email, List<Phone> phones) {
		this.name = name;
		this.title = title;
		this.email = email;
		this.phones = phones;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public List<Phone> getPhones() {
		return phones;
	}
	public void setPhones(List<Phone> phones) {
		this.phones = phones;
	}
	@Override
	public String toString() {
		
		 StringBuilder phoneList = new StringBuilder();
		    for (Phone phone : phones) {
		        phoneList.append(phone).append(", ");
		    }
		    return "Contact [name=" + name + ", title=" + title + ", email=" + email + ", phones=" + phoneList + "]";
	}
	
    
}
