package de.galante.springframework.security.core;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

/**
 * Authority DynamoDB mapping based on the SimpleGrantedAuthority.
 * @author victor.galante
 */
@Data
@NoArgsConstructor
@DynamoDBDocument
public class DynamoAuthority implements GrantedAuthority {

    @DynamoDBHashKey
    private String authority;

    public DynamoAuthority(String authority) {
        this.authority = authority;
    }
}
