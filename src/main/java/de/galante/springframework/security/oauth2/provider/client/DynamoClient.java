package de.galante.springframework.security.oauth2.provider.client;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.galante.springframework.security.core.DynamoAuthority;
import de.galante.springframework.security.core.GrantedAuthorityConverter;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * DynamoDB OAuth2 Client mapping
 * @author victor.galante
 */
@Data
@DynamoDBTable(tableName = "authorization_client")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DynamoClient implements ClientDetails {

    @DynamoDBHashKey
    private String clientId;
    @DynamoDBAttribute
    private String clientSecret;
    @DynamoDBAttribute
    private Set<String> resourceIds;
    @DynamoDBAttribute
    private Set<String> scope;
    @DynamoDBAttribute
    private Set<String> authorizedGrantTypes;
    @DynamoDBAttribute
    private Set<String> registeredRedirectUri;
    @DynamoDBTypeConverted(converter = GrantedAuthorityConverter.class)
    private Collection<GrantedAuthority> authorities;
    @DynamoDBAttribute
    private boolean isSecretRequired;
    @DynamoDBAttribute
    private boolean isScoped;
    @DynamoDBAttribute
    private boolean isAutoApprove;
    @DynamoDBAttribute
    private boolean scoped;
    @DynamoDBAttribute
    private Integer refreshTokenValiditySeconds;
    @DynamoDBAttribute
    private Integer accessTokenValiditySeconds;
    @DynamoDBAttribute
    private Map<String, Object> additionalInformation;

    public boolean isAutoApprove(String scope) {
        return scope.contains(scope);
    }
}
