/*
 * Copyright (c) 2018 Zjtech. All rights reserved.
 * This material is the confidential property of Zjtech or its
 * licensors and may be used, reproduced, stored or transmitted only in
 * accordance with a valid MIT license or sublicense agreement.
 */

package zjtech.piczz.downloadbook;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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
    INCOMPLETE("Incomplete"),
    COMPLETED("Completed");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    public String value() {
      return value;
    }

    public static StatusEnum of(String value) {
      Optional<StatusEnum> optional = Stream.of(StatusEnum.values())
          .filter(statusEnum -> statusEnum.value().equals(value)).findFirst();

      return optional.get();
    }

  }
}

