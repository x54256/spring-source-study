package cn.x5456.rs.pre.cleanstrategy.redis.v1;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.redis.connection.RedisConnection;

import java.util.Properties;

@Deprecated
//@Component
public class ConfigureNotifyKeyspaceEventsAction implements ConfigureRedisAction {

	static final String CONFIG_NOTIFY_KEYSPACE_EVENTS = "notify-keyspace-events";

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.session.data.redis.config.ConfigureRedisAction#configure(org.
	 * springframework.data.redis.connection.RedisConnection)
	 */
	@Override
	public void configure(RedisConnection connection) {
		String notifyOptions = getNotifyOptions(connection);
		String customizedNotifyOptions = notifyOptions;
		if (!customizedNotifyOptions.contains("E")) {
			customizedNotifyOptions += "E";
		}
		boolean A = customizedNotifyOptions.contains("A");
		if (!(A || customizedNotifyOptions.contains("g"))) {
			customizedNotifyOptions += "g";
		}
		if (!(A || customizedNotifyOptions.contains("x"))) {
			customizedNotifyOptions += "x";
		}
		if (!notifyOptions.equals(customizedNotifyOptions)) {
			connection.setConfig(CONFIG_NOTIFY_KEYSPACE_EVENTS, customizedNotifyOptions);
		}
	}

	private String getNotifyOptions(RedisConnection connection) {
		try {
			Properties config = connection.getConfig(CONFIG_NOTIFY_KEYSPACE_EVENTS);
			if (config.isEmpty()) {
				return "";
			}
			return config.getProperty(config.stringPropertyNames().iterator().next());
		}
		catch (InvalidDataAccessApiUsageException ex) {
			throw new IllegalStateException(
					"Unable to configure Redis to keyspace notifications. See https://docs.spring.io/spring-session/docs/current/reference/html5/#api-redisindexedsessionrepository-sessiondestroyedevent",
					ex);
		}
	}

}
