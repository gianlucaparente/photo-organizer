package com.gparente.photoorganizer.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.gparente.photoorganizer.domain.Photo;

import com.gparente.photoorganizer.domain.Tag;
import com.gparente.photoorganizer.repository.PhotoRepository;
import com.gparente.photoorganizer.repository.TagRepository;
import com.gparente.photoorganizer.service.dto.PhotoDTO;
import com.gparente.photoorganizer.web.rest.errors.BadRequestAlertException;
import com.gparente.photoorganizer.web.rest.util.HeaderUtil;
import com.gparente.photoorganizer.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.*;

/**
 * REST controller for managing Photo.
 */
@RestController
@RequestMapping("/api")
public class PhotoResource {

    private final Logger log = LoggerFactory.getLogger(PhotoResource.class);

    private static final String PHOTO_BASE_PATH = "images/root";

    private static final String ENTITY_NAME = "photo";

    private final PhotoRepository photoRepository;
    private final TagRepository tagRepository;

    public PhotoResource(PhotoRepository photoRepository, TagRepository tagRepository) {
        this.photoRepository = photoRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * POST  /photos : Create a new photo.
     *
     * @param photo the photo to create
     * @return the ResponseEntity with status 201 (Created) and with body the new photo, or with status 400 (Bad Request) if the photo has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/photos")
    @Timed
    public ResponseEntity<Photo> createPhoto(@RequestBody Photo photo) throws URISyntaxException {
        log.debug("REST request to save Photo : {}", photo);
        if (photo.getId() != null) {
            throw new BadRequestAlertException("A new photo cannot already have an ID", ENTITY_NAME, "idexists");
        }
        setTagsToPhoto(photo);
        Photo result = photoRepository.save(photo);
        return ResponseEntity.created(new URI("/api/photos/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /photos : Updates an existing photo.
     *
     * @param photo the photo to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated photo,
     * or with status 400 (Bad Request) if the photo is not valid,
     * or with status 500 (Internal Server Error) if the photo couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/photos")
    @Timed
    public ResponseEntity<Photo> updatePhoto(@RequestBody Photo photo) throws URISyntaxException {
        log.debug("REST request to update Photo : {}", photo);
        if (photo.getId() == null) {
            return createPhoto(photo);
        }
        setTagsToPhoto(photo);
        Photo result = photoRepository.save(photo);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, photo.getId().toString()))
            .body(result);
    }

    /**
     * GET  /photos : get all the photos.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of photos in body
     */
    @GetMapping("/photos")
    @Timed
    public ResponseEntity<List<PhotoDTO>> getAllPhotos(Pageable pageable) {
        log.debug("REST request to get a page of Photos");

        Page<Photo> page = photoRepository.findAllWithEagerRelationships(pageable);
        List<PhotoDTO> photos = new ArrayList<>();
        PhotoDTO photoDTO;

        for(Photo photo: page.getContent()) {
            photoDTO = new PhotoDTO(photo);
            this.loadImage(photoDTO, true);
            photos.add(photoDTO);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/photos");

        return new ResponseEntity<>(photos, headers, HttpStatus.OK);
    }

    /**
     * GET  /photos : get all the photos.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of photos in body
     */
    @GetMapping("/photos/tag/{id}")
    @Timed
    public ResponseEntity<List<PhotoDTO>> getAllPhotosByTag(@PathVariable Long id, Pageable pageable) {
        log.debug("REST request to get a page of Photos");

        Tag tag;
        if (id == 0) {
            tag = tagRepository.findRootTag();
        } else {
            tag = tagRepository.findOne(id);
        }

        Page<Photo> page = photoRepository.findAllByTagWithEagerRelationships(tag, pageable);
        List<PhotoDTO> photos = new ArrayList<>();
        PhotoDTO photoDTO;

        for(Photo photo: page.getContent()) {
            photoDTO = new PhotoDTO(photo);
            this.loadImage(photoDTO, true);
            photos.add(photoDTO);
        }

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/photos");

        return new ResponseEntity<>(photos, headers, HttpStatus.OK);
    }

    /**
     * GET  /photos/:id : get the "id" photo.
     *
     * @param id the id of the photo to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the photo, or with status 404 (Not Found)
     */
    @GetMapping("/photos/{id}")
    @Timed
    public ResponseEntity<PhotoDTO> getPhoto(@PathVariable Long id) {
        log.debug("REST request to get Photo : {}", id);

        Photo photo = photoRepository.findOneWithEagerRelationships(id);

        PhotoDTO photoDTO = new PhotoDTO(photo);

        this.loadImage(photoDTO, true);

        return ResponseUtil.wrapOrNotFound(Optional.of(photoDTO));
    }

    /**
     * GET  /photos/:id : get the "id" photo.
     *
     * @param id the id of the photo to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the photo, or with status 404 (Not Found)
     */
    @GetMapping("/photos/{id}/image")
    @Timed
    public ResponseEntity<String> getPhotoImage(@PathVariable Long id) throws IOException {
        log.debug("REST request to get Photo image : {}", id);

        Photo photo = photoRepository.findOne(id);
        PhotoDTO photoDTO = new PhotoDTO(photo);

        this.loadImage(photoDTO, false);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());

        return new ResponseEntity<>(photoDTO.getImage(), headers, HttpStatus.OK);

    }

    /**
     * GET  /photos/:id : get the "id" photo.
     *
     * @param id the id of the photo to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the photo, or with status 404 (Not Found)
     */
    @GetMapping("/photos/{id}/thumbnail")
    @Timed
    public ResponseEntity<String> getPhotoThumbnail(@PathVariable Long id) throws IOException {
        log.debug("REST request to get Photo thumbnail : {}", id);

        Photo photo = photoRepository.findOne(id);
        PhotoDTO photoDTO = new PhotoDTO(photo);

        this.loadImage(photoDTO, true);

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noCache().getHeaderValue());

        return new ResponseEntity<>(photoDTO.getImage(), headers, HttpStatus.OK);

    }

    private void loadImage(PhotoDTO photo, boolean isThumbnail) {

        String fullPath;
        if (isThumbnail) {
            fullPath = PhotoResource.PHOTO_BASE_PATH + "/" + photo.getPath() + "/" + photo.getFileName() + "-thumbnail." + photo.getType();
        } else {
            fullPath = PhotoResource.PHOTO_BASE_PATH + "/" + photo.getPath() + "/" + photo.getFileName() + "." + photo.getType();
        }

        ClassPathResource resource = new ClassPathResource(fullPath);

        try {

            byte[] media = IOUtils.toByteArray(resource.getInputStream());
            String image = "data:image/" + photo.getType().toLowerCase() + ";base64," + Base64.getEncoder().encodeToString(media);

            if (isThumbnail) {
                photo.setThumbnail(image);
            } else {
                photo.setImage(image);
            }

        } catch (IOException e) {
            log.info("No thumbnail found for photo with id: " + photo.getId());
            e.printStackTrace();
        }

    }

    /**
     * DELETE  /photos/:id : delete the "id" photo.
     *
     * @param id the id of the photo to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/photos/{id}")
    @Timed
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        log.debug("REST request to delete Photo : {}", id);
        photoRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    private void setTagsToPhoto(Photo photo) {

        Set<Tag> tags = photo.getTags();
        Set<Tag> parentsTag = new HashSet<>();

        for (Tag tag: tags) {
            parentsTag.addAll(this.findParentsOfTag(tag));
        }

        tags.addAll(parentsTag);
        photo.setTags(tags);

    }

    private Set<Tag> findParentsOfTag(Tag tag) {

        Set<Tag> results = new HashSet<>();

        if (tag.getParentTag() == null) {
            return new HashSet<>();
        }

        Tag parentTag = tagRepository.findParentOfTag(tag);
        results.add(parentTag);
        results.addAll(this.findParentsOfTag(parentTag));

        return results;

    }

}
