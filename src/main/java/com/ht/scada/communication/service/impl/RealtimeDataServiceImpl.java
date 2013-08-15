package com.ht.scada.communication.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.Singleton;
import com.ht.scada.communication.entity.FaultRecord;
import com.ht.scada.communication.entity.OffLimitsRecord;
import com.ht.scada.communication.entity.YxRecord;
import com.ht.scada.communication.service.RealtimeDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.Map;

/**
 * @author 薄成文
 *
 */
@Singleton
public class RealtimeDataServiceImpl implements RealtimeDataService {
    private static final Logger log = LoggerFactory.getLogger(RealtimeDataServiceImpl.class);

    /**
     * 遥信变位广播通道名称
     */
    public static final String YX_CHANGE_CHANNEL = "YxChangeChannel";

    /**
     * "遥测越限"广播通道名称
     */
    public static final String OFF_LIMITS_CHANNEL = "OffLimitsChannel";

    /**
     * "故障报警"广播通道名称
     */
    public static final String FAULT_CHANNEL = "FaultChannel";

    //private ShardedJedisPool pool;
    private final JedisPool pool;

    @Inject
    public RealtimeDataServiceImpl(JedisPool pool) {
        this.pool = pool;

/*		List<JedisShardInfo> jdsInfoList = new ArrayList<JedisShardInfo>(2);

		String hostA = "127.0.0.1";
		int portA = 6379;
		JedisShardInfo infoA = new JedisShardInfo(hostA, portA);
		// infoA.setPassword("redis.360buy");
		jdsInfoList.add(infoA);

		String hostB = "192.168.1.80";
		int portB = 6380;
		JedisShardInfo infoB = new JedisShardInfo(hostB, portB);
		// infoB.setPassword("redis.360buy");
		//jdsInfoList.add(infoB);

		//pool = new ShardedJedisPool(config, jdsInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);*/
	}

    @Override
    public void putValus(Map<String, String> kvMap) {
        Jedis jedis = pool.getResource();
        try {
            Pipeline pipeline = jedis.pipelined();
            for (Map.Entry<String, String> entry : kvMap.entrySet()) {
                pipeline.set(entry.getKey(), entry.getValue());
            }
            pipeline.sync();
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public void putValue(String k, String v) {
        Jedis jedis = pool.getResource();
        try {
            jedis.set(k, v);
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public void setEndModelGroupVar(String code, Map<String, String> groupVarMap) {
        Jedis jedis = pool.getResource();
        try {
            jedis.hmset(code+":GROUP", groupVarMap);
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public void updateEndModel(String code, Map<String, String> kvMap) {
        Jedis jedis = pool.getResource();
        try {
            jedis.hmset(code, kvMap);
        } finally {
            pool.returnResource(jedis);
        }
    }

    @Override
    public void updateEndModelYcArray(String code, Map<String, String> kvMap) {
        updateEndModel(code+":ARRAY", kvMap);
    }

    @Override
    public void faultOccured(FaultRecord record) {
        log.info("故障报警:{} {}", record.getCode(), record.getName());
        publish(FAULT_CHANNEL, JSON.toJSONString(record));
    }

    @Override
    public void faultResumed(FaultRecord record) {
        publish(FAULT_CHANNEL, JSONObject.toJSONString(record));
    }

    @Override
    public void offLimitsOccured(OffLimitsRecord record) {
        publish(OFF_LIMITS_CHANNEL, JSONObject.toJSONString(record));
    }

    @Override
    public void offLimitsResumed(OffLimitsRecord record) {
        publish(OFF_LIMITS_CHANNEL, JSONObject.toJSONString(record));
    }

    @Override
    public void yxChanged(YxRecord record) {
        publish(YX_CHANGE_CHANNEL, JSONObject.toJSONString(record));
    }

    private void publish(String channel, String message) {
        Jedis jedis = pool.getResource();
        try {
            jedis.publish(channel, message);
        } finally {
            pool.returnResource(jedis);
        }
    }

    @PreDestroy
	public void destroy() {
		pool.destroy();
	}
}
