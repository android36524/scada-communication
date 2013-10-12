package com.ht.scada.communication.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author 薄成文
 *
 */
@Singleton
public class RealtimeDataServiceImpl implements RealtimeDataService {
    private static final Logger log = LoggerFactory.getLogger(RealtimeDataServiceImpl.class);

    private static final int BATCH_UPDATE_SIZE = 20;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private ExecutorService dbExecutorService;

    private List<RedisHashMapData> redisDataList = new ArrayList<>();
    private List<RedisPublishData> redisPublishDataList = new ArrayList<>();
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

        //dbExecutorService = Executors.newFixedThreadPool(Config.INSTANCE.getRedisMaxActive());

        // 最慢3s更新一次实时数据
//        executorService.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                update();
//            }
//        }, 5, 5, TimeUnit.SECONDS);
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
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis != null) {
                Pipeline pipeline = jedis.pipelined();
                for (Map.Entry<String, String> entry : kvMap.entrySet()) {
                    pipeline.set(entry.getKey(), entry.getValue());
                }
                pipeline.sync();
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (pool != null) {
                pool.returnResource(jedis);
            }
        }
    }

    @Override
    public void putValue(String k, String v) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis != null) {
                jedis.set(k, v);
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (pool != null) {
                pool.returnResource(jedis);
            }
        }
    }

    @Override
    public void setEndModelGroupVar(String code, Map<String, String> groupVarMap) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis != null) {
                jedis.hmset(code+":GROUP", groupVarMap);
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (pool != null) {
                pool.returnResource(jedis);
            }
        }
    }

    @Override
    public void updateEndModel(String code, Map<String, String> kvMap) {
        final RedisHashMapData data = new RedisHashMapData(code, kvMap);

        update(data);
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                redisDataList.add(data);
//                if (redisDataList.size() + redisPublishDataList.size() > BATCH_UPDATE_SIZE) {
//                    update();
//                }
//            }
//        });
    }

    private void update(final RedisHashMapData data) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis != null) {
                jedis.hmset(data.code, data.kvMap);
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (pool != null) {
                pool.returnResource(jedis);
            }
        }
    }

    private void publish(final RedisPublishData data) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis != null) {
                jedis.publish(data.channel, data.message);
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (pool != null) {
                pool.returnResource(jedis);
            }
        }
    }

    private void update() {

        final List<RedisHashMapData> dataList = Lists.newArrayList(redisDataList);
        final List<RedisPublishData> publishDataList = Lists.newArrayList(redisPublishDataList);
        redisDataList.clear();
        redisPublishDataList.clear();

        dbExecutorService.execute(new Runnable() {
            @Override
            public void run() {

                Jedis jedis = null;
                try {
                    jedis = pool.getResource();
                    if(jedis != null) {

                        Pipeline pipeline = jedis.pipelined();
                        for (RedisHashMapData data : dataList) {
                            pipeline.hmset(data.code, data.kvMap);
                        }
                        for (RedisPublishData data : publishDataList) {
                            pipeline.publish(data.channel, data.message);
                        }
                        pipeline.sync();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    if (pool != null) {
                        pool.returnResource(jedis);
                    }
                }
            }
        });
    }

    @Override
    public void updateEndModelYcArray(String code, Map<String, String> kvMap) {

        final RedisHashMapData data = new RedisHashMapData(code+":ARRAY", kvMap);
        update(data);
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                redisDataList.add(data);
//                if (redisDataList.size() + redisPublishDataList.size() > BATCH_UPDATE_SIZE) {
//                    update();
//                }
//            }
//        });
    }

    @Override
    public void faultOccured(final FaultRecord record) {
        log.info("故障报警:{} {}", record.getCode(), record.getName());
        final RedisPublishData data = new RedisPublishData(FAULT_CHANNEL, JSON.toJSONString(record));
        publish(data);
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                redisPublishDataList.add(data);
//                if (redisPublishDataList.size() > BATCH_UPDATE_SIZE) {
//                    update();
//                }
//            }
//        });
    }

    @Override
    public void faultResumed(final FaultRecord record) {
        final RedisPublishData data = new RedisPublishData(FAULT_CHANNEL, JSON.toJSONString(record));
        publish(data);
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                redisPublishDataList.add(data);
//                if (redisPublishDataList.size() > BATCH_UPDATE_SIZE) {
//                    update();
//                }
//            }
//        });
    }

    @Override
    public void offLimitsOccured(final OffLimitsRecord record) {
        final RedisPublishData data = new RedisPublishData(OFF_LIMITS_CHANNEL, JSON.toJSONString(record));
        publish(data);
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                redisPublishDataList.add(data);
//                if (redisPublishDataList.size() > BATCH_UPDATE_SIZE) {
//                    update();
//                }
//            }
//        });
    }

    @Override
    public void offLimitsResumed(final OffLimitsRecord record) {
        final RedisPublishData data = new RedisPublishData(OFF_LIMITS_CHANNEL, JSON.toJSONString(record));
        publish(data);
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                redisPublishDataList.add(data);
//                if (redisPublishDataList.size() > BATCH_UPDATE_SIZE) {
//                    update();
//                }
//            }
//        });
    }

    @Override
    public void yxChanged(final YxRecord record) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                redisPublishDataList.add(new RedisPublishData(YX_CHANGE_CHANNEL, JSON.toJSONString(record)));
                if (redisPublishDataList.size() > BATCH_UPDATE_SIZE) {
                    update();
                }
            }
        });
    }

/*    private void publish(String channel, String message) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            if(jedis != null) {
                jedis.publish(channel, message);
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (pool != null) {
                pool.returnResource(jedis);
            }
        }
    }*/

    @PreDestroy
	public void destroy() {
		pool.destroy();
	}

    private static class RedisHashMapData {
        private String code;
        //private Map<String, String> kvMap = new HashMap<>();
        private Map<String, String> kvMap;

        private RedisHashMapData(String code, Map<String, String> kvMap) {
            this.code = code;
            this.kvMap = kvMap;
            //this.kvMap.putAll(kvMap);
        }
    }

    private static class RedisPublishData {
        private String channel;
        private String message;

        private RedisPublishData(String channel, String message) {
            this.channel = channel;
            this.message = message;
        }
    }
}
