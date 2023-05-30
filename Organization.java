package br.com.sankhya.ctba.integracaoapi;

import java.sql.Timestamp;

public class Organization {
	
	String idClient ;
	String nameClient ;
	String idUser ;
	String nameUser ;
	String emailUser ;
	Timestamp updated_atOrganization ;
	public String getIdClient() {
		return idClient;
	}
	public void setIdClient(String idClient) {
		this.idClient = idClient;
	}
	public String getNameClient() {
		return nameClient;
	}
	public void setNameClient(String nameClient) {
		this.nameClient = nameClient;
	}
	public String getIdUser() {
		return idUser;
	}
	public void setIdUser(String idUser) {
		this.idUser = idUser;
	}
	public String getNameUser() {
		return nameUser;
	}
	public void setNameUser(String nameUser) {
		this.nameUser = nameUser;
	}
	public String getEmailUser() {
		return emailUser;
	}
	public void setEmailUser(String emailUser) {
		this.emailUser = emailUser;
	}
	public Timestamp getUpdated_atOrganization() {
		return updated_atOrganization;
	}
	public void setUpdated_atOrganization(Timestamp updated_atOrganization) {
		this.updated_atOrganization = updated_atOrganization;
	}
	@Override
	public String toString() {
		return "Organization [idClient=" + idClient + ", nameClient=" + nameClient + ", idUser=" + idUser
				+ ", nameUser=" + nameUser + ", emailUser=" + emailUser + ", updated_atOrganization="
				+ updated_atOrganization + "]";
	}
	
	
	
	
	
	
}
