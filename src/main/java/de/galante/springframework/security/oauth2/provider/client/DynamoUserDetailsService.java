package de.galante.springframework.security.oauth2.provider.client;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collection;

/**
 * @author victor.galante
 */
public interface DynamoUserDetailsService extends UserDetailsService {

    void save(UserDetails details);

    UserDetails load(String id);

    void delete(String id);

    Collection<UserDetails> findAll();
}
