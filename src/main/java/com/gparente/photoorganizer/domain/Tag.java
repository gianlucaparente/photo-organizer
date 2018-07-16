package com.gparente.photoorganizer.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A Tag.
 */
@Entity
@Table(name = "tag")
public class Tag implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "jhi_type")
    private String type;

    @ManyToMany(mappedBy = "tags", cascade = CascadeType.REMOVE)
    private Set<Photo> photos = new HashSet<>();

    @ManyToOne
    private User user;

    @OneToOne
    @JoinColumn
    private Tag parentTag;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Tag name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public Tag type(String type) {
        this.type = type;
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<Photo> getPhotos() {
        return photos;
    }

    public Tag photos(Set<Photo> photos) {
        this.photos = photos;
        return this;
    }

    public Tag addPhotos(Photo photo) {
        this.photos.add(photo);
        photo.getTags().add(this);
        return this;
    }

    public Tag removePhotos(Photo photo) {
        this.photos.remove(photo);
        photo.getTags().remove(this);
        return this;
    }

    public void setPhotos(Set<Photo> photos) {
        this.photos = photos;
    }

    public User getUser() {
        return user;
    }

    public Tag user(User user) {
        this.user = user;
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Tag getParentTag() {
        return parentTag;
    }

    public Tag parentTag(Tag tag) {
        this.parentTag = tag;
        return this;
    }

    public void setParentTag(Tag tag) {
        this.parentTag = tag;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tag tag = (Tag) o;
        if (tag.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), tag.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Tag{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", type='" + getType() + "'" +
            "}";
    }
}
