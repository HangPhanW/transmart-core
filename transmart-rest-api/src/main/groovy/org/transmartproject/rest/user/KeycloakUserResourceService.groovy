package org.transmartproject.rest.user

import grails.transaction.Transactional
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.users.User
import org.transmartproject.core.users.UsersResource

import java.security.Principal

@Component
class KeycloakUserResourceService implements UsersResource {

    @Override
    User getUserFromUsername(String username) throws NoSuchResourceException {
        return null
    }

    @Override
    List<User> getUsers() {
        return null
    }

    @Override
    List<User> getUsersWithEmailSpecified() {
        return null
    }

    @Override
    User getUserFromPrincipal(Principal principal) {
        return null
    }
}
