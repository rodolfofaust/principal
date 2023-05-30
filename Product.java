package br.com.sankhya.ctba.integracaoapi;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Product {
	
	String idProd ;
	String nameProd ;
	String qtdNeg ;
	String tipoDesc ;
	String vlrProd ;
	String vlrDesc ;
	BigDecimal vlrTot;
	String updated_at ;
	Timestamp updated_atProduct;
	public String getIdProd() {
		return idProd;
	}
	public void setIdProd(String idProd) {
		this.idProd = idProd;
	}
	public String getNameProd() {
		return nameProd;
	}
	public void setNameProd(String nameProd) {
		this.nameProd = nameProd;
	}
	public String getQtdNeg() {
		return qtdNeg;
	}
	public void setQtdNeg(String qtdNeg) {
		this.qtdNeg = qtdNeg;
	}
	public String getTipoDesc() {
		return tipoDesc;
	}
	public void setTipoDesc(String tipoDesc) {
		this.tipoDesc = tipoDesc;
	}
	public String getVlrProd() {
		return vlrProd;
	}
	public void setVlrProd(String vlrProd) {
		this.vlrProd = vlrProd;
	}
	public String getVlrDesc() {
		return vlrDesc;
	}
	public void setVlrDesc(String vlrDesc) {
		this.vlrDesc = vlrDesc;
	}
	public BigDecimal getVlrTot() {
		return vlrTot;
	}
	public void setVlrTot(BigDecimal vlrTot) {
		this.vlrTot = vlrTot;
	}
	public String getUpdated_at() {
		return updated_at;
	}
	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}
	public Timestamp getUpdated_atProduct() {
		return updated_atProduct;
	}
	public void setUpdated_atProduct(Timestamp updated_atProduct) {
		this.updated_atProduct = updated_atProduct;
	}
	@Override
	public String toString() {
		return "Product [idProd=" + idProd + ", nameProd=" + nameProd + ", qtdNeg=" + qtdNeg + ", tipoDesc=" + tipoDesc
				+ ", vlrProd=" + vlrProd + ", vlrDesc=" + vlrDesc + ", vlrTot=" + vlrTot + ", updated_at=" + updated_at
				+ ", updated_atProduct=" + updated_atProduct + "]";
	}
	
	
	

}
