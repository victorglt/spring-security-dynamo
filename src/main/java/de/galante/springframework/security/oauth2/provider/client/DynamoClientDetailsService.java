package de.galante.springframework.security.oauth2.provider.client;

import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;

import java.util.Collection;

/**
 * @author victor.galante
 */
public interface DynamoClientDetailsService extends ClientDetailsService {

    void save(ClientDetails details);

    void delete(String id);

    Collection<ClientDetails> findAll();

}
