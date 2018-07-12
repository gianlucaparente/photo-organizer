package com.gparente.photoorganizer.repository;

import com.gparente.photoorganizer.domain.Photo;
import com.gparente.photoorganizer.domain.Tag;
import com.gparente.photoorganizer.service.dto.PhotoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Spring Data JPA repository for the Photo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    @Query("select photo from Photo photo where photo.user.login = ?#{principal.username}")
    List<Photo> findByUserIsCurrentUser();

    @Query("select distinct photo from Photo photo left join fetch photo.tags")
    List<Photo> findAllWithEagerRelationships();

    @Query(
        value = "select distinct photo from Photo photo left join fetch photo.tags",
        countQuery = "select count(distinct photo) from Photo photo"
    )
    Page<Photo> findAllWithEagerRelationships(Pageable pageable);

    @Query(
        value = "select distinct photo from Photo photo where :tag member of photo.tags",
        countQuery = "select count(distinct photo) from Photo photo where :tag member of photo.tags"
    )
    Page<Photo> findAllByTagWithEagerRelationships(@Param("tag") Tag tag, Pageable pageable);

    @Query("select photo from Photo photo left join fetch photo.tags where photo.id =:id")
    Photo findOneWithEagerRelationships(@Param("id") Long id);

}
