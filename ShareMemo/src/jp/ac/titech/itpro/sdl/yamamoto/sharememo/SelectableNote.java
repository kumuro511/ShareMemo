package jp.ac.titech.itpro.sdl.yamamoto.sharememo;

public class SelectableNote extends Note {
	private boolean isSelected;

	public SelectableNote(int id, String note, String user, String lastupdate) {
		super(id, note, user, lastupdate);
		this.setIsSelected(false);
	}

	public void setIsSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	public boolean isSelected() {
		return isSelected;
	}
}

