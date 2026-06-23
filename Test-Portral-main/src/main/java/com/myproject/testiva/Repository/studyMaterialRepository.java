/*
 * package com.myproject.testiva.Repository;
 * 
 * import java.util.List;
 * 
 * import org.springframework.data.jpa.repository.JpaRepository;
 * 
 * import com.myproject.testiva.Model.StudyMaterial; import
 * com.myproject.testiva.Model.StudyMaterial.MaterialType; import
 * com.myproject.testiva.Model.StudyMaterial.MediaVisibility;
 * 
 * public interface studyMaterialRepository extends JpaRepository<StudyMaterial,
 * Long> {
 * 
 * // ✅ Manage PDF page List<StudyMaterial> findByMaterialType(MaterialType
 * materialType);
 * 
 * // ✅ Student dashboard List<StudyMaterial>
 * findByCourseAndBranchAndYearAndIsVisible( String course, String branch,
 * String year, MediaVisibility isVisible );
 * 
 * // ✅ Analytics List<StudyMaterial> findTop10ByIsVisibleOrderByViewCountDesc(
 * MediaVisibility isVisible ); }
 */

package com.myproject.testiva.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.myproject.testiva.Model.StudyMaterial;
import com.myproject.testiva.Model.StudyMaterial.MaterialType;
import com.myproject.testiva.Model.StudyMaterial.MediaVisibility;

public interface studyMaterialRepository
        extends JpaRepository<StudyMaterial, Long> {

    // ✅ Admin: Manage PDF
    List<StudyMaterial> findByMaterialType(MaterialType materialType);

    // ✅ Admin: Manage Video
    List<StudyMaterial> findByMaterialTypeOrderByUploadedAtDesc(MaterialType materialType);

	List<StudyMaterial> findByMaterialTypeAndIsVisible(MaterialType video, MediaVisibility visible);
}




