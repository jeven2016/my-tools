/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz.downloadbook;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "piczz_single_book")
@Getter
@Setter
@NoArgsConstructor
@NamedEntityGraph(name = "book.all", attributeNodes = {@NamedAttributeNode("pictures")})
public class SingleBookEntity {

  @JsonIgnore
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column
  private String name;

  @Column
  private String url;

  @Column
  private int picPageCount;

  @Column
  private int picCount;

  @Column
  private StatusEnum status;


  @OneToMany(
      cascade = {CascadeType.ALL},
      fetch = FetchType.LAZY,
      orphanRemoval = true
  )
  @JoinColumn(name = "piczz_single_book_id", nullable = false)
  private Set<SinglePictureEntity> pictures = new HashSet<>();

  public Set<SinglePictureEntity> getPictures() {
    return pictures;
  }

  public void setPictures(Set<SinglePictureEntity> pictures) {
    this.pictures = pictures;
  }

  public void addPicture(SinglePictureEntity singlePictureEntity) {
    this.pictures.add(singlePictureEntity);
  }

  public static enum StatusEnum {
    NEW_ADDED("New added"),
    FAILED("Failed"),
    PARSING("Parsing"),
    PARSED("Parsed"),
    IGNORED("Ignored"),
    DWONLOADING("Dwonloading"),
    COMPLETED("Completed");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    public String value() {
      return value;
    }
  }
}

