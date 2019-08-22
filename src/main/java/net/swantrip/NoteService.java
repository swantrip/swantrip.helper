package net.swantrip;

import java.util.List;

public interface NoteService {
	Note save(Note note);

	List<Note> list();

	boolean existsByOriginUrl(String originUrl);
}
