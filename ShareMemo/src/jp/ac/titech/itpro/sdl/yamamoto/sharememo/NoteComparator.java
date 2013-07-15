package jp.ac.titech.itpro.sdl.yamamoto.sharememo;

import java.util.Comparator;

public class NoteComparator implements Comparator<Note> {

	@Override
	public int compare(Note lhs, Note rhs) {
		return -lhs.getLastupdate().compareTo(rhs.getLastupdate());
	}
}
