package com.mycompany.senaattendance.repository;

import com.mycompany.senaattendance.domain.UserProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the UserProfile entity.
 */
@Repository
public interface UserProfileRepository extends MongoRepository<UserProfile, String> {
    @Query("{}")
    Page<UserProfile> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<UserProfile> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<UserProfile> findOneWithEagerRelationships(String id);

    // Busca un UserProfile por el ID del usuario asociado
    @Query("{'user._id': ?0}")
    Optional<UserProfile> findOneByUserId(String userId);

    // ------- SEARCH USER PROFILE BY DOCUMENT NUMBER -------
    Optional<UserProfile> findByDocumentNumber(String documentNumber);

    /**
     * Returns every profile that uses the given document number. The number alone does not
     * identify a profile: the unique key is the (documentType, documentNumber) pair, so the same
     * number can belong to profiles with different document types.
     *
     * @param documentNumber the document number to look up.
     * @return all profiles with that document number, possibly empty.
     */
    List<UserProfile> findAllByDocumentNumber(String documentNumber);

    // ------- SEARCH USER PROFILE BY DOCUMENT TYPE AND DOCUMENT NUMBER -------
    Optional<UserProfile> findByDocumentTypeAndDocumentNumber(String documentTypeId, String documentNumber);

    /**
     * Returns whether any user profile references the given document type.
     * Used to block changing the initials of, or deleting, a document type that is still in use.
     *
     * @param documentTypeId the document type id to check.
     * @return {@code true} if at least one user profile references this document type.
     */
    boolean existsByDocumentTypeId(String documentTypeId);

    // ------- SEARCH USERPROFILE BY DOCUMENT NUMBER -------
    @Query("{ 'documentNumber': {$regex: ?0, $options: 'i' } }")
    Page<UserProfile> findByDocumentNumberContaining(String documentNumber, Pageable pageable);

    // ------- SEARCH USERPROFILE BY FIRST NAME OR LAST NAME -------
    @Query("{ $or: [ { 'firstName': { $regex: ?0, $options: 'i' } }, { 'firstLastName': { $regex: ?0, $options: 'i' } }] }")
    Page<UserProfile> findByFirstNameContainingOrFirstLastNameContaining(String searchTerm, Pageable pageable);

    // ------- SEARCH USERPROFILE BY ID -------
    @Query("{ 'user._id': { $in: ?0 } }")
    Page<UserProfile> findByUserIdIn(List<String> userIds, Pageable pageable);

    /**
     * Returns the profiles whose document number contains the given fragment, ignoring case.
     * Used to resolve the text filters of the apprentice list (UC008, A2), because the
     * enrollment stores the student as a {@code @DBRef} and cannot be filtered by profile
     * fields in the same query.
     *
     * @param documentNumber the document number fragment to look for.
     * @return the matching profiles, possibly empty.
     */
    List<UserProfile> findByDocumentNumberContainingIgnoreCase(String documentNumber);

    /**
     * Returns the profiles whose first name or first last name contains the given fragment,
     * ignoring case.
     *
     * @param firstName the first name fragment to look for.
     * @param firstLastName the first last name fragment to look for.
     * @return the matching profiles, possibly empty.
     */
    List<UserProfile> findByFirstNameContainingIgnoreCaseOrFirstLastNameContainingIgnoreCase(String firstName, String firstLastName);
}
