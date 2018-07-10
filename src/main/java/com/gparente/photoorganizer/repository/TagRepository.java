package com.gparente.photoorganizer.repository;

import com.gparente.photoorganizer.domain.Tag;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import java.util.List;

/**
 * Spring Data JPA repository for the Tag entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    @Query("select tag from Tag tag where tag.user.login = ?#{principal.username}")
    List<Tag> findByUserIsCurrentUser();

}
