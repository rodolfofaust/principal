package br.com.sankhya.ctba.integracaoapi;

import java.math.BigDecimal;

public class Vendedor {
	
	String idVend ;
	String nameVend ;
	String emailVend ;
	String nNameVend ;
	
	BigDecimal codusu;
	BigDecimal codvend;
	
	
	

	public String getIdVend() {
		return idVend;
	}
	public void setIdVend(String idVend) {
		this.idVend = idVend;
	}
	public String getNameVend() {
		return nameVend;
	}
	public void setNameVend(String nameVend) {
		this.nameVend = nameVend;
	}
	public String getEmailVend() {
		return emailVend;
	}
	public void setEmailVend(String emailVend) {
		this.emailVend = emailVend;
	}
	public String getnNameVend() {
		return nNameVend;
	}
	public void setnNameVend(String nNameVend) {
		this.nNameVend = nNameVend;
	}
	public BigDecimal getCodusu() {
		return codusu;
	}
	public void setCodusu(BigDecimal codusu) {
		this.codusu = codusu;
	}
	public BigDecimal getCodvend() {
		return codvend;
	}
	public void setCodvend(BigDecimal codvend) {
		this.codvend = codvend;
	}
	@Override
	public String toString() {
		return "Vendedor [idVend=" + idVend + ", nameVend=" + nameVend + ", emailVend=" + emailVend + ", nNameVend="
				+ nNameVend + ", codusu=" + codusu + ", codvend=" + codvend + "]";
	}
	
	
	
	
	
	

}
