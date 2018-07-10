package com.gparente.photoorganizer.repository;

import com.gparente.photoorganizer.domain.Photo;
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

    @Query("select photo from Photo photo left join fetch photo.tags where photo.id =:id")
    Photo findOneWithEagerRelationships(@Param("id") Long id);

}
