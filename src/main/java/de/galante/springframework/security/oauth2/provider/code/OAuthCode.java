package de.galante.springframework.security.oauth2.provider.code;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@DynamoDBTable(tableName = "authorization_code")
public class OAuthCode {

    @DynamoDBHashKey
    private String code;

    private byte[] authentication;

    public OAuthCode(String code, byte[] authentication) {
        this.code = code;
        this.authentication = authentication;
    }
}
