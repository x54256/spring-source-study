package cn.x5456.rs.pre.cleanstrategy.redis.v1;

import org.springframework.data.redis.connection.RedisConnection;

/**
 * Allows specifying a strategy for configuring and validating Redis.
 *
 * @author Rob Winch
 * @since 1.0.1
 */
@Deprecated
public interface ConfigureRedisAction {

	void configure(RedisConnection connection);

	/**
	 * A do nothing implementation of {@link ConfigureRedisAction}.
	 */
	ConfigureRedisAction NO_OP = (connection) -> {
	};

}
