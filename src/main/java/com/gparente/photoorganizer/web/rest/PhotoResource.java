package com.gparente.photoorganizer.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.gparente.photoorganizer.domain.Photo;

import com.gparente.photoorganizer.domain.Tag;
import com.gparente.photoorganizer.domain.User;
import com.gparente.photoorganizer.repository.PhotoRepository;
import com.gparente.photoorganizer.repository.TagRepository;
import com.gparente.photoorganizer.repository.UserRepository;
import com.gparente.photoorganizer.service.dto.PhotoDTO;
import com.gparente.photoorganizer.web.rest.errors.BadRequestAlertException;
import com.gparente.photoorganizer.web.rest.util.HeaderUtil;
import com.gparente.photoorganizer.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.*;
import java.util.List;

/**
 * REST controller for managing Photo.
 */
@RestController
@RequestMapping("/api")
public class PhotoResource {

    private final Logger log = LoggerFactory.getLogger(PhotoResource.class);

    private static final String PHOTO_BASE_PATH = "source";

    private static final String ENTITY_NAME = "photo";

    private final PhotoRepository photoRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    public PhotoResource(PhotoRepository photoRepository, TagRepository tagRepository, UserRepository userRepository) {
        this.photoRepository = photoRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
    }

    /**
     * POST  /photos : Create a new photo.
     *
     * @param image the photo to create
     * @param tagIds the photo to create
     * @param userId the photo to create
     * @return the ResponseEntity with status 201 (Created) and with body the new photo, or with status 400 (Bad Request) if the photo has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/photos/create")
    @Timed
    public ResponseEntity<Photo> createPhoto(
        @RequestParam("image") MultipartFile image,
        @RequestParam("tagIds") String tagIds,
        @RequestParam("userId") String userId
    ) throws Exception {
        log.debug("REST request to save Photo : {}");

        Photo photo = this.storeImage(image);

        List<Long> tagIdsLong = new ArrayList<>();
        for (String s: tagIds.split(",")) {
            tagIdsLong.add(Long.parseLong(s));
        }

        Set<Tag> tags = tagRepository.findAllByIds(tagIdsLong);
        photo.setTags(tags);
        User user = userRepository.findOne(Long.parseLong(userId));
        photo.setUser(user);
        photoRepository.save(photo);

        return ResponseEntity.created(new URI("/api/photos/" + photo.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, photo.getId().toString()))
            .body(photo);
    }

    /**
     * PUT  /photos : Updates an existing photo.
     *
     * @param photoId the photo to update
     * @param image the photo to update
     * @param tagIds the photo to update
     * @param userId the photo to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated photo,
     * or with status 400 (Bad Request) if the photo is not valid,
     * or with status 500 (Internal Server Error) if the photo couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/photos/update")
    @Timed
    public ResponseEntity<Photo> updatePhoto(
        @RequestParam("photoId") String photoId,
        @RequestParam(value = "image", required = false) MultipartFile image,
        @RequestParam("tagIds") String tagIds,
        @RequestParam("userId") String userId
    ) throws Exception {
        log.debug("REST request to update Photo : {}");

        Photo oldPhoto = photoRepository.findOneWithEagerRelationships(Long.parseLong(photoId));

        Photo photo;
        if (image != null) {
            photo = this.storeImage(image, oldPhoto);
        } else {
            photo = oldPhoto;
        }

        List<Long> tagIdsLong = new ArrayList<>();
        for (String s: tagIds.split(",")) {
            tagIdsLong.add(Long.parseLong(s));
        }

        Set<Tag> tags = tagRepository.findAllByIds(tagIdsLong);
        photo.setTags(tags);
        User user = userRepository.findOne(Long.parseLong(userId));
        photo.setUser(user);
        photoRepository.save(photo);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, photo.getId().toString()))
            .body(photo);
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
        Photo photo = this.photoRepository.findOne(id);
        deleteThumbnail(photo);
        deleteImage(photo);
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

    private void loadImage(PhotoDTO photo, boolean isThumbnail) {

        String fileName;
        if (isThumbnail) {
            fileName = photo.getFileName() + "-thumbnail." + photo.getType();
        } else {
            fileName = photo.getFileName() + "." + photo.getType();
        }

        try {

            FileInputStream fileInputStream = new FileInputStream(PhotoResource.PHOTO_BASE_PATH + File.separator + fileName);
            byte[] media = IOUtils.toByteArray(fileInputStream);
            String image = "data:image/" + photo.getType().toLowerCase() + ";base64," + Base64.getEncoder().encodeToString(media);

            if (isThumbnail) {
                photo.setThumbnail(image);
            } else {
                photo.setImage(image);
            }

        } catch (IOException e) {
            log.info("No image to load found for photo with id: " + photo.getId());
            e.printStackTrace();
        }

    }

    private Photo storeImage(MultipartFile image) throws Exception {
        Photo photo = new Photo();
        return this.storeImage(image, photo);
    }

    private Photo storeImage(MultipartFile image, Photo photo) throws Exception {

        if (!image.isEmpty()) {

            try {

                String[] nameSplit = image.getOriginalFilename().split("\\.");

                photo.setFileName(nameSplit[0]);
                photo.setType(nameSplit[1]);

                byte[] bytes = image.getBytes();
                File file = new File(PhotoResource.PHOTO_BASE_PATH + File.separator + image.getOriginalFilename());
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
                stream.write(bytes);
                stream.close();

                log.info("Image stored for photo");

                Image imgThumb = ImageIO.read(file).getScaledInstance(-1, 200, Image.SCALE_SMOOTH);
                BufferedImage imgBI = new BufferedImage(imgThumb.getWidth(null), imgThumb.getHeight(null), BufferedImage.TYPE_INT_RGB);
                imgBI.createGraphics().drawImage(imgThumb,0,0,null);
                ImageIO.write(imgBI, photo.getType(), new File(PhotoResource.PHOTO_BASE_PATH + File.separator + photo.getFileName() + "-thumbnail." + photo.getType()));

                log.info("Thumbnail stored for photo");

            } catch (IOException e) {
                log.warn("Error in store image file for photo");
                e.printStackTrace();
            }

            return photo;

        } else {

            log.info("No image to store found for photo. The image file of request is empty.");
            throw new Exception("No image to store found for photo. The image file of request is empty.");

        }

    }

    private void deleteImage(Photo photo) {

        String fileName = photo.getFileName() + "." + photo.getType();
        File file = new File(PhotoResource.PHOTO_BASE_PATH + File.separator + fileName);

        if(file.delete()){
            log.info("Image of photo " + photo + " with name " + fileName + " is deleted.");
        }else{
            log.info("No image found to delete for photo " + photo);
        }

    }

    private void deleteThumbnail(Photo photo) {

        String fileName = photo.getFileName() + "-thumbnail." + photo.getType();
        File file = new File(PhotoResource.PHOTO_BASE_PATH + File.separator + fileName);

        if(file.delete()){
            log.info("Thumbnail of photo " + photo + " with name " + fileName + " is deleted.");
        }else{
            log.info("No Thumbnail found to delete for photo " + photo);
        }

    }

}
