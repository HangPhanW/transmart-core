package org.transmartproject.core.users

import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.Study
import org.transmartproject.core.querytool.QueryDefinition
import org.transmartproject.core.querytool.QueryResult

/**
 * Contains access right checks for the pre 17.1 data model where study represented with a tree node.
 */
interface LegacyAuthorisationChecks {

    /**
     * Check whether user has access to the query result
     * @return true if user has access to the result
     */
    boolean hasAccess(User user, QueryResult result)

    /**
     * Check whether user has access to the term
     * @return true if user has access to the term
     */
    boolean hasAccess(User user, OntologyTerm term)

    /**
     * Check whether user has right to run the given query definition (cohort selection query).
     * Studies mentioned in the definition are checked whether user has access to them.
     * @param user - user we check read permission for
     * @param definition - contains query definition
     * @return true if user can run the query
     */
    boolean canRun(User user, QueryDefinition definition)

    /**
     * Check whether user has to the study patient data of the given level.
     * @param user - user we check read permission for
     * @param patientDataAccessLevel - minimal access level user has to have
     * @param study - study we check user permission for
     * @return true if user has access to the data
     */
    boolean canReadPatientData(User user, PatientDataAccessLevel patientDataAccessLevel, Study study)

    /**
     * Check whether user has any access to the study
     * @return true if user has access to the study
     */
    boolean hasAccess(User user, Study study)
}