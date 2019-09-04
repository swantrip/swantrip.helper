
package net.swantrip;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SwantripApplication.class)
public class OptNotesTest {

	@Autowired
	private NoteDao noteDao;

	@Test
	public void testOptNotes() {
		List<Note> notes = noteDao.findAll();
		for (Note n : notes) {
			n.setName(n.getName().replace("途牛", "天鹅之旅"));
			n.setContent(n.getContent().replace("途牛", "天鹅之旅"));
			n.setSummary(n.getSummary().replace("途牛", "天鹅之旅"));
			noteDao.save(n);
		}
	}

}
