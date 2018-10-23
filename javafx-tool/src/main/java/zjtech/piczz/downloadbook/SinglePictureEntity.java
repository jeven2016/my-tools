package zjtech.piczz.downloadbook;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Table(name = "piczz_single_picture")
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = {"url", "subPageNo", "booKName"})
public class SinglePictureEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column
  private String booKName;

  @Column
  private String url;

  @Column
  private long subPageNo;

  @Column
  private int picIndex;


  public String getPicIndexInfo() {
    String indexInfo = picIndex + "";
    int len = indexInfo.length();
    if (len < 3) {
      for (int i = 0; i < 3 - len; i++) {
        indexInfo = 0 + indexInfo;
      }
    }
    return indexInfo;
  }


}
