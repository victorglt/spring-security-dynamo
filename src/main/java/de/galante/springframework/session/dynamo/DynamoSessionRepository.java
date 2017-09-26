package de.galante.springframework.session.dynamo;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.session.ExpiringSession;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * A {@link org.springframework.session.SessionRepository} implementation that uses
 * Spring's {@link DynamoDBMapper} to store sessions in Amazon's Dynamo database. This
 * implementation does not support publishing of session events.
 *
 * @author Victor Galante
 */
public class DynamoSessionRepository implements FindByIndexNameSessionRepository<DynamoSessionRepository.DynamoSession> {

    private static final Log logger = LogFactory.getLog(DynamoSessionRepository.class);
    private static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";
    private static final DynamoSessionRepository.PrincipalNameResolver PRINCIPAL_NAME_RESOLVER = new DynamoSessionRepository.PrincipalNameResolver();
    private final DynamoDBMapper mapper;
    private Integer defaultMaxInactiveInterval;

    @Autowired
    public DynamoSessionRepository(DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Set the maximum inactive interval in seconds between requests before newly created
     * sessions will be invalidated. A negative time indicates that the session will never
     * timeout. The default is 1800 (30 minutes).
     *
     * @param defaultMaxInactiveInterval the maximum inactive interval in seconds
     */
    public void setDefaultMaxInactiveInterval(Integer defaultMaxInactiveInterval) {
        this.defaultMaxInactiveInterval = defaultMaxInactiveInterval;
    }

    @Override
    public Map<String, DynamoSession> findByIndexNameAndIndexValue(String indexName, String indexValue) {

        if (!PRINCIPAL_NAME_INDEX_NAME.equals(indexName)) {
            return Collections.emptyMap();
        }

        DynamoDBQueryExpression<DynamoSession> listSessionsByPrincipalNameQuery = new DynamoDBQueryExpression<DynamoSession>()
                .withConsistentRead(true)
                .withKeyConditionExpression(":principalName = " + indexValue);

        PaginatedQueryList<DynamoSession> sessions = mapper.query(DynamoSession.class, listSessionsByPrincipalNameQuery);

        Map<String, DynamoSession> sessionMap = new HashMap<String, DynamoSession>(
                sessions.size());

        for (DynamoSession session : sessions) {
            sessionMap.put(session.getId(), session);
        }

        return sessionMap;
    }

    @Override
    public DynamoSession createSession() {
        DynamoSession session = new DynamoSession();
        if (this.defaultMaxInactiveInterval != null) {
            session.setMaxInactiveIntervalInSeconds(this.defaultMaxInactiveInterval);
        }
        return session;
    }

    @Override
    public void save(final DynamoSession session) {
        if (session.isNew()) {
            mapper.save(session);
        } else {
            delete(session.getId());
            mapper.save(session);
        }
    }

    @Override
    public DynamoSession getSession(String id) {
        DynamoSession session = mapper.load(DynamoSession.class, id);
        if (session != null) {
            if (session.isExpired()) {
                delete(id);
            } else {
                return session;
            }
        }

        return null;
    }

    @Override
    public void delete(String s) {
        mapper.delete(new DynamoSession(s));
    }

    @Scheduled(cron = "${spring.session.cleanup.cron.expression:0 * * * * *}")
    public void cleanUpExpiredSessions() {

        DynamoDBQueryExpression<DynamoSession> deleteSessionsByLastAccessTimeQuery = new DynamoDBQueryExpression<DynamoSession>()
                .withConsistentRead(true)
                .withKeyConditionExpression(":maxInactiveInterval < (" + System.currentTimeMillis() + ") / 1000");

        PaginatedQueryList<DynamoSession> found = mapper.query(DynamoSession.class, deleteSessionsByLastAccessTimeQuery);

        found.parallelStream()
                .forEach(s -> delete(s.getId()));

        if (logger.isDebugEnabled()) {
            logger.debug("Cleaned up " + found.size() + " expired sessions");
        }
    }

    /**
     * Resolves the Spring Security principal name.
     *
     * @author Vedran Pavic
     */
    static class PrincipalNameResolver {

        private SpelExpressionParser parser = new SpelExpressionParser();

        public String resolvePrincipal(Session session) {
            String principalName = session.getAttribute(PRINCIPAL_NAME_INDEX_NAME);
            if (principalName != null) {
                return principalName;
            }
            Object authentication = session.getAttribute(SPRING_SECURITY_CONTEXT);
            if (authentication != null) {
                Expression expression = this.parser
                        .parseExpression("authentication?.name");
                return expression.getValue(authentication, String.class);
            }
            return null;
        }

    }

    /**
     * The {@link ExpiringSession} to use for {@link DynamoSessionRepository}.
     *
     * @author Victor Galante
     */
    @DynamoDBTable(tableName = "authentication_session")
    final class DynamoSession implements ExpiringSession, Serializable {

        /**
         * Default {@link #setMaxInactiveIntervalInSeconds(int)} (30 minutes).
         */
        public static final int DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS = 1800;

        @DynamoDBAttribute
        private boolean isNew;
        @DynamoDBAttribute
        private boolean changed;
        @DynamoDBHashKey
        private String id;
        @DynamoDBAttribute
        private Map<String, Object> sessionAttrs = new HashMap<String, Object>();
        @DynamoDBAttribute
        private long creationTime = System.currentTimeMillis();
        @DynamoDBAttribute
        private long lastAccessedTime = this.creationTime;

        /**
         * Defaults to 30 minutes.
         */
        @DynamoDBAttribute
        private int maxInactiveInterval = DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS;

        public DynamoSession() {
            this(UUID.randomUUID().toString());
        }

        public DynamoSession(String id) {
            this.id = id;
            this.isNew = true;
        }

        boolean isNew() {
            return this.isNew;
        }

        boolean isChanged() {
            return this.changed;
        }

        public long getCreationTime() {
            return this.creationTime;
        }

        /**
         * Sets the time that this {@link Session} was created in milliseconds since midnight
         * of 1/1/1970 GMT. The default is when the {@link Session} was instantiated.
         *
         * @param creationTime the time that this {@link Session} was created in milliseconds
         *                     since midnight of 1/1/1970 GMT.
         */
        public void setCreationTime(long creationTime) {
            this.creationTime = creationTime;
        }

        public String getId() {
            return this.id;
        }

        /**
         * Sets the identifier for this {@link Session}. The id should be a secure random
         * generated value to prevent malicious users from guessing this value. The default is
         * a secure random generated identifier.
         *
         * @param id the identifier for this session.
         */
        public void setId(String id) {
            this.id = id;
        }

        public long getLastAccessedTime() {
            return this.lastAccessedTime;
        }

        public void setLastAccessedTime(long lastAccessedTime) {
            this.lastAccessedTime = lastAccessedTime;
            this.changed = true;
        }

        public int getMaxInactiveIntervalInSeconds() {
            return this.maxInactiveInterval;
        }

        public void setMaxInactiveIntervalInSeconds(int interval) {
            this.maxInactiveInterval = interval;
            this.changed = true;
        }

        public boolean isExpired() {
            return isExpired(System.currentTimeMillis());
        }

        boolean isExpired(long now) {
            if (this.maxInactiveInterval < 0) {
                return false;
            }
            return now - TimeUnit.SECONDS
                    .toMillis(this.maxInactiveInterval) >= this.lastAccessedTime;
        }

        @SuppressWarnings("unchecked")
        public <T> T getAttribute(String attributeName) {
            return (T) this.sessionAttrs.get(attributeName);
        }

        public Set<String> getAttributeNames() {
            return this.sessionAttrs.keySet();
        }

        public void setAttribute(String attributeName, Object attributeValue) {
            if (attributeValue == null) {
                removeAttribute(attributeName);
            } else {
                this.sessionAttrs.put(attributeName, attributeValue);
            }

            this.changed = true;
        }

        public void removeAttribute(String attributeName) {
            this.sessionAttrs.remove(attributeName);
        }

        public boolean equals(Object obj) {
            return obj instanceof Session && this.id.equals(((Session) obj).getId());
        }

        public int hashCode() {
            return this.id.hashCode();
        }

    }
}
