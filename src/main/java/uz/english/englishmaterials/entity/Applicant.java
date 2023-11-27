package uz.english.englishmaterials.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "applicants")
public class Applicant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long applicantId;
    private Boolean active;
    private Boolean completed;
    private Boolean single = true;
    private String fullName;
    private String gender;

    private String birthDay;
    private String birthPlace;
    private String birthCountry;
    private String photoId;
    private String educationStatus;
    private String lifeStatus;
    private String childCount;
}
