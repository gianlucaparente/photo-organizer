package com.gparente.photoorganizer.repository;

import com.gparente.photoorganizer.domain.Photo;
import com.gparente.photoorganizer.domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(
        value = "select distinct tag from Tag tag left join fetch tag.photos",
        countQuery = "select count(distinct tag) from Tag tag"
    )
    Page<Tag> findAllWithEagerRelationships(Pageable pageable);

}
