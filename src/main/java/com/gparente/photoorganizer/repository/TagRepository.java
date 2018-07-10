package com.gparente.photoorganizer.repository;

import com.gparente.photoorganizer.domain.Photo;
import com.gparente.photoorganizer.domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
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

    @Query("select tag.parentTag from Tag tag where tag = :tag")
//    @Query("SELECT t1 " +
//        "FROM Tag t1 " +
//        "INNER JOIN Tag t2 " +
//        "WHERE t1.id = t2.parentTag AND t2 = :tag")
    Tag findParentOfTag(@Param("tag") Tag tag);

}
