/*
 * Copyright © 2013-2014 The Hyve B.V.
 *
 * This file is part of transmart-core-db.
 *
 * Transmart-core-db is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * transmart-core-db.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmartproject.db.querytool

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.querytool.*
import org.transmartproject.core.users.User
import org.transmartproject.db.ontology.AcrossTrialsTestData
import org.transmartproject.db.TransmartSpecification

import static org.hamcrest.Matchers.*
import static org.transmartproject.core.querytool.ConstraintByValue.Operator.BETWEEN
import static org.transmartproject.core.querytool.ConstraintByValue.ValueType.NUMBER
import static spock.util.matcher.HamcrestSupport.that

@Integration
@Rollback
class QueriesResourceAcrossTrialsSpec extends TransmartSpecification {
    private static final String AGE_AT_DIAGNOSIS_NODE =
            '\\\\xtrials\\Across Trials\\Demographics\\Age at Diagnosis\\'

    AcrossTrialsTestData testData

    QueriesResource queriesResourceService
    def sessionFactory

    void setupData() {
        testData = AcrossTrialsTestData.createDefault()
        testData.saveAll()
    }

    void testUserAdmin() {
        setupData()
        def user = users[0].username // first user is admin
        def definition = definitionForItem(
                new Item(conceptKey: AGE_AT_DIAGNOSIS_NODE))

        def result = queriesResourceService.runQuery definition, user
        // has all patients
        expect:
        that(result.patients, containsInAnyOrder(
                testData.patients.collect { is it }))
    }

    void testUserAdminWithConstraint() {
        setupData()
        def user = users[0].username // first user is admin
        def definition = definitionForItem(
                new Item(conceptKey: AGE_AT_DIAGNOSIS_NODE,
                        constraint: new ConstraintByValue(
                                valueType: NUMBER,
                                operator: BETWEEN,
                                constraint: '2101 and 2201'
                        )))

        def result = queriesResourceService.runQuery definition, user

        def patients = testData.facts.findAll {
            it.numberValue == 2101.0 || it.numberValue == 2201.0
        }*.patient
        expect:
        patients hasSize(2)

        result.patients
        containsInAnyOrder(patients.collect { is it })
    }

    void testUserWithNoAccessToStudy2() {
        setupData()
        // 4th user has no access to study 2
        def user = users[3].username
        def definition = definitionForItem(
                new Item(conceptKey: AGE_AT_DIAGNOSIS_NODE))

        def result = queriesResourceService.runQuery definition, user

        def patients = testData.patients.findAll {
            Patient it -> it.trial == 'STUDY_ID_1'
        }
        expect:
        patients hasSize(2)

        result.patients
        containsInAnyOrder(patients.collect { is it })
    }

    void testAcrossTrialsQueryNeedsUser() {
        setupData()
        def definition = definitionForItem(
                new Item(conceptKey: AGE_AT_DIAGNOSIS_NODE,))

        def result = queriesResourceService.runQuery(definition)

        expect:
        result allOf(
                hasProperty('status', equalTo(QueryStatus.ERROR)))
    }

    QueryDefinition definitionForItem(Item item) {
        new QueryDefinition([
                new Panel(
                        items: [
                                item
                        ]
                )
        ])
    }

    List<User> getUsers() {
        testData.accessLevelTestData.users
    }
}
