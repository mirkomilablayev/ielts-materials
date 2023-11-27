package uz.english.englishmaterials.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicantRepository extends JpaRepository<Applicant, Long> {
    List<Applicant> findAllByApplicantIdAndActive(Long applicantId, Boolean active);
    Optional<Applicant> findByApplicantIdAndActiveAndCompleted(Long applicantId, Boolean active, Boolean completed);
}
