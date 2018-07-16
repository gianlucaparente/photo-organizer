package com.gparente.photoorganizer.repository;

import com.gparente.photoorganizer.domain.Photo;
import com.gparente.photoorganizer.domain.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import java.util.List;
import java.util.Set;

/**
 * Spring Data JPA repository for the Tag entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    @Query("select tag from Tag tag left join fetch tag.photos where tag.id = :id")
    Tag findOneWithEagerRelationships(@Param("id") Long id);

    @Query("select tag from Tag tag left join fetch tag.photos where tag.name = 'ROOT'")
    Tag findRootTagWithEagerRelationships();

    @Query("select tag from Tag tag where tag.user.login = ?#{principal.username}")
    List<Tag> findByUserIsCurrentUser();

    @Query(
        value = "select distinct tag from Tag tag left join fetch tag.photos",
        countQuery = "select count(distinct tag) from Tag tag"
    )
    Page<Tag> findAllWithEagerRelationships(Pageable pageable);

    @Query("select tag.parentTag from Tag tag where tag = :tag")
    Tag findParentOfTag(@Param("tag") Tag tag);

    @Query("select distinct tag from Tag tag where tag.parentTag = :tag")
    Set<Tag> findSonsOfTag(@Param("tag") Tag tag);

    @Query("select tag from Tag tag where tag.name = 'ROOT'")
    Tag findRootTag();

    @Query("select tag from Tag tag where tag.id in :tagIds")
    Set<Tag> findAllByIds(@Param("tagIds") List<Long> tagIds);

}
