package net.swantrip;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteDao extends JpaRepository<Note, Long> {

	boolean existsByOriginUrl(String originUrl);

}
