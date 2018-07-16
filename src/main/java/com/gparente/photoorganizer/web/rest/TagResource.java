package com.gparente.photoorganizer.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.gparente.photoorganizer.domain.Photo;
import com.gparente.photoorganizer.domain.Tag;

import com.gparente.photoorganizer.repository.PhotoRepository;
import com.gparente.photoorganizer.repository.TagRepository;
import com.gparente.photoorganizer.web.rest.errors.BadRequestAlertException;
import com.gparente.photoorganizer.web.rest.util.HeaderUtil;
import com.gparente.photoorganizer.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.*;

/**
 * REST controller for managing Tag.
 */
@RestController
@RequestMapping("/api")
public class TagResource {

    private final Logger log = LoggerFactory.getLogger(TagResource.class);

    private static final String ENTITY_NAME = "tag";

    private final TagRepository tagRepository;
    private final PhotoRepository photoRepository;
    private final PhotoResource photoResource;

    public TagResource(TagRepository tagRepository, PhotoRepository photoRepository, PhotoResource photoResource) {
        this.tagRepository = tagRepository;
        this.photoRepository = photoRepository;
        this.photoResource = photoResource;
    }

    /**
     * POST  /tags : Create a new tag.
     *
     * @param tag the tag to create
     * @return the ResponseEntity with status 201 (Created) and with body the new tag, or with status 400 (Bad Request) if the tag has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/tags")
    @Timed
    public ResponseEntity<Tag> createTag(@RequestBody Tag tag) throws URISyntaxException {
        log.debug("REST request to save Tag : {}", tag);
        if (tag.getId() != null) {
            throw new BadRequestAlertException("A new tag cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Tag result = tagRepository.save(tag);
        return ResponseEntity.created(new URI("/api/tags/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /tags : Updates an existing tag.
     *
     * @param tag the tag to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated tag,
     * or with status 400 (Bad Request) if the tag is not valid,
     * or with status 500 (Internal Server Error) if the tag couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/tags")
    @Timed
    public ResponseEntity<Tag> updateTag(@RequestBody Tag tag) throws URISyntaxException {
        log.debug("REST request to update Tag : {}", tag);
        if (tag.getId() == null) {
            return createTag(tag);
        }
        Tag result = tagRepository.save(tag);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, tag.getId().toString()))
            .body(result);
    }

    /**
     * GET  /tags : get all the tags.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of tags in body
     */
    @GetMapping("/tags")
    @Timed
    public ResponseEntity<List<Tag>> getAllTags(Pageable pageable) {
        log.debug("REST request to get a page of Tags");
        Page<Tag> page = tagRepository.findAllWithEagerRelationships(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/tags");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /tags/:id : get the "id" tag.
     *
     * @param id the id of the tag to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the tag, or with status 404 (Not Found)
     */
    @GetMapping("/tags/{id}")
    @Timed
    public ResponseEntity<Tag> getTag(@PathVariable Long id) {
        log.debug("REST request to get Tag : {}", id);
        Tag tag = findTagById(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(tag));
    }

    /**
     * GET  /tags/:id/sons : get the "id" tag.
     *
     * @param id the id of the tag to retrieve the sons
     * @return the ResponseEntity with status 200 (OK) and with body the tags sons, or with status 404 (Not Found)
     */
    @GetMapping("/tags/{id}/sons")
    @Timed
    public ResponseEntity<Set<Tag>> getSonsOfTag(@PathVariable Long id) {
        log.debug("REST request to get Tag : {}", id);
        Tag tag = findTagById(id);
        Set<Tag> sonsTags = tagRepository.findSonsOfTag(tag);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(sonsTags));
    }

    /**
     * DELETE  /tags/:id : delete the "id" tag.
     *
     * @param id the id of the tag to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/tags/{id}")
    @Timed
    @Transactional
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        log.debug("REST request to delete Tag : {}", id);
        Tag tag = findTagByIdWithEagerRelationships(id);

        Set<Tag> tagsDeleted = this.deleteRecursively(tag);
        log.info("Tags deleted: " + tagsDeleted.toString());

        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    private Set<Tag> deleteRecursively(Tag tag) {

        Set<Tag> sonsTags = tagRepository.findSonsOfTag(tag);

        if (sonsTags.isEmpty()) {

            this.deleteSingleTagAndPhotos(tag);

            Set<Tag> tags = new HashSet<>();
            tags.add(tag);

            return tags;
        }

        Set<Tag> tagsDeleted = new HashSet<>();
        for(Tag tagSon: sonsTags) {
            tagsDeleted.addAll(deleteRecursively(tagSon));
        }

        this.deleteSingleTagAndPhotos(tag);

        tagsDeleted.add(tag);

        return tagsDeleted;

    }

    private void deleteSingleTagAndPhotos(Tag tag) {

        Set<Photo> photos = this.photoRepository.findAllByTagWithEagerRelationships(tag);

        for (Photo photo: photos) {
            photo.getTags().remove(tag);
            if (photo.getTags().size() == 0) {
                this.photoResource.deletePhoto(photo.getId());
            }
        }

        tagRepository.delete(tag.getId());

    }

    private Tag findTagById(Long id) {
        if (id == 0) {
            return tagRepository.findRootTag();
        } else {
            return tagRepository.findOne(id);
        }
    }

    private Tag findTagByIdWithEagerRelationships(Long id) {
        if (id == 0) {
            return tagRepository.findRootTagWithEagerRelationships();
        } else {
            return tagRepository.findOneWithEagerRelationships(id);
        }
    }

}
