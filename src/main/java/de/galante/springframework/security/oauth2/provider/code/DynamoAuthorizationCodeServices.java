package de.galante.springframework.security.oauth2.provider.code;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;
import org.springframework.stereotype.Component;


/**
 * DynamoDB based authentication code storage
 */
@Component
public class DynamoAuthorizationCodeServices extends RandomValueAuthorizationCodeServices {

    final DynamoDBMapper mapper;

    @Autowired
    public DynamoAuthorizationCodeServices(DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected void store(String code, OAuth2Authentication authentication) {
        OAuthCode oauthCode = new OAuthCode(code, SerializationUtils.serialize(authentication));
        mapper.save(oauthCode);
    }

    @Override
    protected OAuth2Authentication remove(String code) {
        OAuthCode key = new OAuthCode();
        key.setCode(code);

        DynamoDBQueryExpression<OAuthCode> query = new DynamoDBQueryExpression<OAuthCode>()
                .withConsistentRead(true)
                .withHashKeyValues(key);

         PaginatedList<OAuthCode> results = mapper.query(OAuthCode.class, query);
         if(results.size()  == 0) {
             return null;
         }

         if(results.size() > 1) {
             throw new RuntimeException(String.format("More than one authorization_code with value %s was found", code));
         }

         OAuthCode oAuthCode = results.get(0);
         OAuth2Authentication authentication = SerializationUtils.deserialize(oAuthCode.getAuthentication());

         mapper.delete(oAuthCode);
         return authentication;
    }
}
