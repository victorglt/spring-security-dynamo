package de.galante.springframework.security.oauth2.provider.client.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import de.galante.springframework.security.oauth2.provider.client.DynamoClient;
import de.galante.springframework.security.oauth2.provider.client.DynamoClientDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Manages Clients in DynamoDB
 * @author victor.galante
 */
@Component
public class DynamoClientDetailsServiceImpl implements DynamoClientDetailsService {

    final DynamoDBMapper mapper;

    @Autowired
    public DynamoClientDetailsServiceImpl(DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Cacheable("clients")
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        return mapper.load(DynamoClient.class, clientId);
    }

    @Override
    public void save(ClientDetails details) {
        mapper.save(details);
    }

    @Override
    public void delete(String id) {
        mapper.delete(loadClientByClientId(id));
    }

    @Override
    public Collection<ClientDetails> findAll() {
        return new ArrayList<>(mapper.scan(DynamoClient.class, new DynamoDBScanExpression()));
    }
}
