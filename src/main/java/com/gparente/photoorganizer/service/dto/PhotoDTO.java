package com.gparente.photoorganizer.service.dto;

import com.gparente.photoorganizer.domain.Photo;
import com.gparente.photoorganizer.domain.Tag;
import com.gparente.photoorganizer.domain.User;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO representing a photo.
 */
public class PhotoDTO {

    // Photo fields
    private Long id;
    private String fileName;
    private String path;
    private String type;
    private Instant dateCreated;
    private Set<Tag> tags = new HashSet<>();
    private User user;

    // Photo DTO fields
    private String thumbnail;
    private String image;

    public PhotoDTO() {}

    public PhotoDTO(Photo photo) {
        this.id = photo.getId();
        this.fileName = photo.getFileName();
        this.path = photo.getPath();
        this.type = photo.getType();
        this.dateCreated = photo.getDateCreated();
        this.tags = photo.getTags();
        this.user = photo.getUser();
    }

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Photo photo = (Photo) o;
        if (photo.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), photo.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Photo{" +
            "id=" + getId() +
            ", fileName='" + getFileName() + "'" +
            ", path='" + getPath() + "'" +
            ", type='" + getType() + "'" +
            ", dateCreated='" + getDateCreated() + "'" +
            "}";
    }
}
