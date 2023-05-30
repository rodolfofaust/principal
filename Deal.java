package br.com.sankhya.ctba.integracaoapi;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Deal {
	
	String idDeal ;
	String nameDeal ;
	String vlrTotDeal ;
	String vlrUniDeal ;
	String vlrMesDeal ;
	String updated_atStr ;
	String updated_atDeal_stageStr ;
	BigDecimal RatingDeal ;
	Timestamp updated_at ;
	Timestamp updated_atDeal_stage ;
	String deal_stage ;

	
	
	
	
	public String getUpdated_atDeal_stageStr() {
		return updated_atDeal_stageStr;
	}
	public void setUpdated_atDeal_stageStr(String updated_atDeal_stageStr) {
		this.updated_atDeal_stageStr = updated_atDeal_stageStr;
	}
	public String getIdDeal() {
		return idDeal;
	}
	public void setIdDeal(String idDeal) {
		this.idDeal = idDeal;
	}
	public String getNameDeal() {
		return nameDeal;
	}
	public void setNameDeal(String nameDeal) {
		this.nameDeal = nameDeal;
	}
	public String getVlrTotDeal() {
		return vlrTotDeal;
	}
	public void setVlrTotDeal(String vlrTotDeal) {
		this.vlrTotDeal = vlrTotDeal;
	}
	public String getVlrUniDeal() {
		return vlrUniDeal;
	}
	public void setVlrUniDeal(String vlrUniDeal) {
		this.vlrUniDeal = vlrUniDeal;
	}
	public String getVlrMesDeal() {
		return vlrMesDeal;
	}
	public void setVlrMesDeal(String vlrMesDeal) {
		this.vlrMesDeal = vlrMesDeal;
	}
	public String getUpdated_atStr() {
		return updated_atStr;
	}
	public void setUpdated_atStr(String updated_atStr) {
		this.updated_atStr = updated_atStr;
	}
	public BigDecimal getRatingDeal() {
		return RatingDeal;
	}
	public void setRatingDeal(BigDecimal ratingDeal) {
		RatingDeal = ratingDeal;
	}
	public Timestamp getUpdated_at() {
		return updated_at;
	}
	public void setUpdated_at(Timestamp updated_at) {
		this.updated_at = updated_at;
	}
	public String getDeal_stage() {
		return deal_stage;
	}
	public void setDeal_stage(String deal_stage) {
		this.deal_stage = deal_stage;
	}
	public Timestamp getUpdated_atDeal_stage() {
		return updated_atDeal_stage;
	}
	public void setUpdated_atDeal_stage(Timestamp updated_atDeal_stage) {
		this.updated_atDeal_stage = updated_atDeal_stage;
	}
	@Override
	public String toString() {
		return "Deal [idDeal=" + idDeal + ", nameDeal=" + nameDeal + ", vlrTotDeal=" + vlrTotDeal + ", vlrUniDeal="
				+ vlrUniDeal + ", vlrMesDeal=" + vlrMesDeal + ", updated_atStr=" + updated_atStr + ", RatingDeal="
				+ RatingDeal + ", updated_at=" + updated_at + ", updated_atDeal_stage=" + updated_atDeal_stage
				+ ", deal_stage=" + deal_stage + "]";
	}


	
    
}
