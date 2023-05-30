package br.com.sankhya.ctba.integracaoapi;

import java.util.List;

public class Task {

    private String id;
    private String subject;
    private String type;
    private String hour;
    private String markup;
    private boolean done;
    private List<String> user_ids;
    private String notes;
    private String deal_id;
    private String done_date;
    private String created_at;
    private String date;
    private Deal deal;
    private List<User> users;

    
  /*  
    
    
    public Task(String id, String subject, String type, String hour, String markup, boolean done, List<String> user_ids,
			String notes, String deal_id, String done_date, String created_at, String date, Deal deal,
			List<User> users) {

		this.id = id;
		this.subject = subject;
		this.type = type;
		this.hour = hour;
		this.markup = markup;
		this.done = done;
		this.user_ids = user_ids;
		this.notes = notes;
		this.deal_id = deal_id;
		this.done_date = done_date;
		this.created_at = created_at;
		this.date = date;
		this.deal = deal;
		this.users = users;
	}
*/
	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMarkup() {
        return markup;
    }

    public void setMarkup(String markup) {
        this.markup = markup;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public List<String> getUserIds() {
        return user_ids;
    }

    public void setUserIds(List<String> user_ids) {
        this.user_ids = user_ids;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDealId() {
        return deal_id;
    }

    public void setDealId(String deal_id) {
        this.deal_id = deal_id;
    }

    public String getDoneDate() {
        return done_date;
    }

    public void setDoneDate(String done_date) {
        this.done_date = done_date;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(String created_at) {
        this.created_at = created_at;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Deal getDeal() {
        return deal;
    }

    public void setDeal(Deal deal) {
        this.deal = deal;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

	@Override
	public String toString() {
		return "Task [id=" + id + ", subject=" + subject + ", type=" + type + ", hour=" + hour + ", markup=" + markup
				+ ", done=" + done + ", user_ids=" + user_ids + ", notes=" + notes + ", deal_id=" + deal_id
				+ ", done_date=" + done_date + ", created_at=" + created_at + ", date=" + date + ", deal=" + deal
				+ ", users=" + users + "]";
	}

}

   