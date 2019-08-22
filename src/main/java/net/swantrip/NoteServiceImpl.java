package net.swantrip;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NoteServiceImpl implements NoteService {

	@Autowired
	private NoteDao noteDao;

	@Override
	public Note save(Note note) {
		return noteDao.save(note);
	}

	@Override
	public List<Note> list() {
		return noteDao.findAll();
	}

	@Override
	public boolean existsByOriginUrl(String originUrl) {
		return noteDao.existsByOriginUrl(originUrl);
	}
}
