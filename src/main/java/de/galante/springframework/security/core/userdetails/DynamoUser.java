package de.galante.springframework.security.core.userdetails;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.galante.springframework.security.core.DynamoAuthority;
import de.galante.springframework.security.core.GrantedAuthorityConverter;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * DynamoDB user mapping
 * @author victor.galante
 */
@Data
@DynamoDBTable(tableName = "authorization_user")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DynamoUser implements UserDetails {

    @DynamoDBAttribute
    private boolean isEnabled;

    @DynamoDBAttribute
    private boolean isCredentialsNonExpired;

    @DynamoDBAttribute
    private boolean isAccountNonLocked;

    @DynamoDBAttribute
    private boolean isAccountNonExpired;

    @DynamoDBHashKey
    private String username;

    @DynamoDBAttribute
    private String password;

    @DynamoDBTypeConverted(converter = GrantedAuthorityConverter.class)
    private Collection<? extends GrantedAuthority> authorities;
}
