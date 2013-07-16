package jp.ac.titech.itpro.sdl.yamamoto.sharememo;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Note implements Serializable {
	private int id;
	private String note;
	private String user;
	private String lastupdate;
	
	public Note(int id, String note, String user, String lastupdate) {
		this.id = id;
		this.setNote(note);
		this.setUser(user);
		this.setLastupdate(lastupdate);
	}

	public int getId() {
		return id;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getLastupdate() {
		return lastupdate;
	}

	public void setLastupdate(String lastupdate) {
		this.lastupdate = lastupdate;
	}

}
