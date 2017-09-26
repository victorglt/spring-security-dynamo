package de.galante.springframework.security.oauth2.provider.client.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import de.galante.springframework.security.core.userdetails.DynamoUser;
import de.galante.springframework.security.oauth2.provider.client.DynamoUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.Collection;

/**
 * Manages Users in DynamoDB
 * @author victor.galante
 */
@Component
public class DynamoUserDetailsServiceImpl implements DynamoUserDetailsService {

    final DynamoDBMapper mapper;

    @Autowired
    public DynamoUserDetailsServiceImpl(DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return mapper.load(DynamoUser.class, username);
    }

    @Override
    public void save(UserDetails details) {
        mapper.save(details);
    }

    @Override
    public UserDetails load(String id) {
        return mapper.load(DynamoUser.class, id);
    }

    @Override
    public void delete(String id) {
        mapper.delete(load(id));
    }

    @Override
    public Collection<UserDetails> findAll() {
        return new ArrayList<>(mapper.scan(DynamoUser.class, new DynamoDBScanExpression()));
    }
}
