package de.galante.springframework.security.core;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;

import static java.util.Arrays.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

/**
 * Saves authorities as list of comma separated values in DynamoDB
 * @author victor.galante
 */
public class GrantedAuthorityConverter implements DynamoDBTypeConverter<String, Collection<GrantedAuthority>> {

    private static final String SEPARATOR = ";";

    @Override
    public String convert(Collection<GrantedAuthority> toConvert) {
        return toConvert.stream().map(GrantedAuthority::getAuthority).collect(joining(";"));

    }

    @Override
    public Collection<GrantedAuthority> unconvert(String toUnconvert) {
        return stream(toUnconvert.split(SEPARATOR))
                .map(DynamoAuthority::new)
                .collect(toCollection(HashSet::new));
    }
}
