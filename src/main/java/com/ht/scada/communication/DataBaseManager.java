package com.ht.scada.communication;

import com.alibaba.druid.pool.DruidDataSource;
import com.ht.db.Database;
import com.ht.db.util.DbUtilsTemplate;
import com.ht.scada.communication.dao.*;
import com.ht.scada.communication.dao.impl.*;
import com.ht.scada.communication.service.HistoryDataService;
import com.ht.scada.communication.service.RealtimeDataService;
import com.ht.scada.communication.service.impl.HistoryDataServiceImpl;
import com.ht.scada.communication.service.impl.HistoryDataServiceImpl2;
import com.ht.scada.communication.service.impl.RealtimeDataServiceImpl;
import oracle.kv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: "薄成文" 13-5-21 上午10:55
 * To change this template use File | Settings | File Templates.
 */
public class DataBaseManager {
    private static final Logger log = LoggerFactory.getLogger(DataBaseManager.class);
    private static DataBaseManager instance = new DataBaseManager();
    private KVStoreConfig kvConfig;

    public static DataBaseManager getInstance() {
        return instance;
    }

    private JedisPool jedisPool;
    private KVStore kvStore;

    private DruidDataSource dataSource;
    private DbUtilsTemplate dbTemplate;

    private ChannelInfoDao channelInfoDao;
    private EndTagDao endTagDao;
    private TagVarTplDao tagVarTplDao;
    private VarGroupInfoDao varGroupInfoDao;
    private VarIOInfoDao varIOInfoDao;

    private FaultRecordDao faultRecordDao;
    private OffLimitsRecordDao offLimitsRecordDao;
    private YxRecordDao yxRecordDao;
    private VarGroupDataDao varGroupDataDao;

    private RealtimeDataService realtimeDataService;
    private HistoryDataService historyDataService;

    private DataBaseManager() {
        dataSource = new DruidDataSource();
        dataSource.setMaxWait(60000);
        dataSource.setMaxActive(20);
        dataSource.setMinIdle(1);
        dataSource.setInitialSize(1);
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

        dbTemplate = new DbUtilsTemplate(dataSource);

        initJedisPool();
        if (Config.INSTANCE.getDatabase() == Database.ORACLE_KV) {
            initKVStore();
        }
    }

    public void init() {
        channelInfoDao = new ChannelInfoDaoImpl();
        channelInfoDao.setDbUtilsTemplate(dbTemplate);

        endTagDao = new EndTagDaoImpl();
        endTagDao.setDbUtilsTemplate(dbTemplate);

        tagVarTplDao = new TagVarTplDaoImpl();
        tagVarTplDao.setDbUtilsTemplate(dbTemplate);

        varGroupInfoDao = new VarGroupInfoDaoImpl();
        varGroupInfoDao.setDbUtilsTemplate(dbTemplate);

        varIOInfoDao = new VarIOInfoDaoImpl();
        varIOInfoDao.setDbUtilsTemplate(dbTemplate);

        faultRecordDao = new FaultRecordDaoImpl(dbTemplate);
        offLimitsRecordDao = new OffLimitsRecordDaoImpl(dbTemplate);
        yxRecordDao = new YxRecordDaoImpl();
        yxRecordDao.setDbUtilsTemplate(dbTemplate);

        varGroupDataDao = new VarGroupDataDaoImpl(dbTemplate);

        realtimeDataService = new RealtimeDataServiceImpl(jedisPool);

        if (Config.INSTANCE.getDatabase() == Database.ORACLE_KV) {
            historyDataService = new HistoryDataServiceImpl(kvStore, kvConfig.getRequestTimeout(TimeUnit.MILLISECONDS));
        } else {
            historyDataService = new HistoryDataServiceImpl2();
        }
    }

    private void initKVStore() {
        kvConfig = new KVStoreConfig(Config.INSTANCE.getKvStoreName(), Config.INSTANCE.getKvHostPort());
        kvConfig.setRequestLimit(RequestLimitConfig.getDefault());

        try {
            kvStore = KVStoreFactory.getStore(kvConfig);
        } catch (FaultException e) {
            log.error("无法连接到时任何一个节点", e);
        }
    }

    private void initJedisPool() {
        JedisPoolConfig jedisPoolConfig =new JedisPoolConfig();//Jedis池配置
        jedisPoolConfig.setMaxActive(Config.INSTANCE.getRedisMaxActive());// 最大活动的对象个数
        jedisPoolConfig.setMaxIdle(Config.INSTANCE.getRedisMaxIdle());// 对象最大空闲时间
        jedisPoolConfig.setMaxWait(Config.INSTANCE.getRedisMaxWait());// 获取对象时最大等待时间
        //config.setTestOnBorrow(true);
        if (Config.INSTANCE.getRedisPassword() != null) {
            jedisPool = new JedisPool(jedisPoolConfig, Config.INSTANCE.getRedisHost(), Config.INSTANCE.getRedisPort(),
                    Config.INSTANCE.getRedisTimeout(), Config.INSTANCE.getRedisPassword());
        } else {
            jedisPool = new JedisPool(jedisPoolConfig, Config.INSTANCE.getRedisHost(), Config.INSTANCE.getRedisPort());
        }
    }

    public void destroy() {
        if (dataSource != null) {
            dataSource.close();
        }
        if (kvStore != null) {
            kvStore.close();
        }
        if (jedisPool != null) {
            jedisPool.destroy();
        }
    }

    public HistoryDataService getHistoryDataService() {
        return historyDataService;
    }

    public RealtimeDataService getRealtimeDataService() {
        return realtimeDataService;
    }

    public DbUtilsTemplate getDbTemplate() {
        return dbTemplate;
    }

    public OffLimitsRecordDao getOffLimitsRecordDao() {
        return offLimitsRecordDao;
    }

    public ChannelInfoDao getChannelInfoDao() {
        return channelInfoDao;
    }

    public EndTagDao getEndTagDao() {
        return endTagDao;
    }

    public TagVarTplDao getTagVarTplDao() {
        return tagVarTplDao;
    }

    public VarGroupInfoDao getVarGroupInfoDao() {
        return varGroupInfoDao;
    }

    public VarIOInfoDao getVarIOInfoDao() {
        return varIOInfoDao;
    }

    public FaultRecordDao getFaultRecordDao() {
        return faultRecordDao;
    }

    public YxRecordDao getYxRecordDao() {
        return yxRecordDao;
    }

    public VarGroupDataDao getVarGroupDataDao() {
        return varGroupDataDao;
    }
}
