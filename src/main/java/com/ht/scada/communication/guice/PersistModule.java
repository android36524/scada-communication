package com.ht.scada.communication.guice;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.ht.db.util.DbUtilsTemplate;
import com.ht.scada.communication.Config;
import com.ht.scada.communication.dao.*;
import com.ht.scada.communication.dao.impl.*;
import com.ht.scada.communication.service.HistoryDataService;
import com.ht.scada.communication.service.RealtimeDataService;
import com.ht.scada.communication.service.impl.HistoryDataServiceImpl2;
import com.ht.scada.communication.service.impl.RealtimeDataServiceImpl;
import oracle.kv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.sql.DataSource;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: 薄成文 13-8-15 下午11:56
 * To change this template use File | Settings | File Templates.
 */
public class PersistModule extends AbstractModule {
    private static final Logger log = LoggerFactory.getLogger(PersistModule.class);

    protected void configure() {
        bind(DbUtilsTemplate.class);
        bind(ChannelInfoDao.class).to(ChannelInfoDaoImpl.class).in(Singleton.class);
        bind(EndTagDao.class).to(EndTagDaoImpl.class).in(Singleton.class);
        bind(FaultRecordDao.class).to(FaultRecordDaoImpl.class).in(Singleton.class);
        bind(OffLimitsRecordDao.class).to(OffLimitsRecordDaoImpl.class).in(Singleton.class);
        bind(TagVarTplDao.class).to(TagVarTplDaoImpl.class).in(Singleton.class);
        bind(VarGroupDataDao.class).to(VarGroupDataDaoImpl.class).in(Singleton.class);
        bind(VarGroupInfoDao.class).to(VarGroupInfoDaoImpl.class).in(Singleton.class);
        bind(VarIOInfoDao.class).to(VarIOInfoDaoImpl.class).in(Singleton.class);
        bind(YxRecordDao.class).to(YxRecordDaoImpl.class).in(Singleton.class);

        bind(RealtimeDataService.class).to(RealtimeDataServiceImpl.class).in(Singleton.class);
        bind(HistoryDataService.class).to(HistoryDataServiceImpl2.class).in(Singleton.class);
    }

    @Provides @Singleton
    JedisPool provideJedisPool() {

        JedisPoolConfig jedisPoolConfig =new JedisPoolConfig();//Jedis池配置
        jedisPoolConfig.setMaxActive(Config.INSTANCE.getRedisMaxActive());// 最大活动的对象个数
        jedisPoolConfig.setMaxIdle(Config.INSTANCE.getRedisMaxIdle());// 对象最大空闲
        jedisPoolConfig.setMaxWait(Config.INSTANCE.getRedisMaxWait());// 获取对象时最大等待时间
        jedisPoolConfig.setMinIdle(1);
        //config.setTestOnBorrow(true);
        if (Config.INSTANCE.getRedisPassword() != null) {
            return new JedisPool(jedisPoolConfig, Config.INSTANCE.getRedisHost(), Config.INSTANCE.getRedisPort(),
                    Config.INSTANCE.getRedisTimeout(), Config.INSTANCE.getRedisPassword());
        } else {
            return new JedisPool(jedisPoolConfig, Config.INSTANCE.getRedisHost(), Config.INSTANCE.getRedisPort(), Config.INSTANCE.getRedisTimeout());
        }
    }

    @Provides @Singleton
    DataSource provideDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setMaxWait(Config.INSTANCE.getConfig().getLong("jdbc.maxWait", 120000));
        dataSource.setMaxActive(Config.INSTANCE.getConfig().getInt("jdbc.maxActive", 200));
        dataSource.setMinIdle(Config.INSTANCE.getConfig().getInt("jdbc.minIdle", 1));
        dataSource.setInitialSize(Config.INSTANCE.getConfig().getInt("jdbc.initialSize", 10));
        dataSource.setUrl(Config.INSTANCE.getConfig().getString("jdbc.url"));
        dataSource.setUsername(Config.INSTANCE.getConfig().getString("jdbc.username"));
        dataSource.setPassword(Config.INSTANCE.getConfig().getString("jdbc.password"));
        dataSource.setTestWhileIdle(false);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);

        try {
            dataSource.setFilters("stat,slf4j");
            dataSource.getConnection();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return dataSource;
    }

    @Provides @Singleton
    KVStore provideKvStore() {

        KVStoreConfig kvConfig = new KVStoreConfig(Config.INSTANCE.getKvStoreName(), Config.INSTANCE.getKvHostPort());
        kvConfig.setRequestLimit(RequestLimitConfig.getDefault());

        KVStore kvStore = null;

        try {
            kvStore = KVStoreFactory.getStore(kvConfig);
        } catch (FaultException e) {
            log.error("无法连接到时任何一个节点", e);
        }
        return kvStore;
    }
}
