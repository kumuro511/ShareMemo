package jp.ac.titech.itpro.sdl.yamamoto.sharememo;

public class Note {
	private int id;
	private String note;
	private String user;
	private String lastupdate;
	
	public Note(int id, String note, String user, String lastupdate) {
		this.id = id;
		this.note = note;
		this.user = user;
		this.lastupdate = lastupdate;
	}

	public int getId() {
		return id;
	}

	public String getNote() {
		return note;
	}

	public String getUser() {
		return user;
	}

	public String getLastupdate() {
		return lastupdate;
	}

}
