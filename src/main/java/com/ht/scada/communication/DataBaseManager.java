package com.ht.scada.communication;

import com.alibaba.druid.pool.DruidDataSource;
import com.ht.scada.communication.service.HistoryDataService;
import com.ht.scada.communication.service.RealtimeDataService;
import com.ht.scada.communication.web.MyWebAppContextListener;
import oracle.kv.KVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import javax.sql.DataSource;


/**
 * Created with IntelliJ IDEA.
 *
 * @author: "薄成文" 13-5-21 上午10:55
 * To change this template use File | Settings | File Templates.
 */
public class DataBaseManager {
    private static final Logger log = LoggerFactory.getLogger(DataBaseManager.class);
    private static DataBaseManager instance = new DataBaseManager();

    public static DataBaseManager getInstance() {
        return instance;
    }


    private RealtimeDataService realtimeDataService;
    private HistoryDataService historyDataService;

    private DataBaseManager() {
        realtimeDataService = MyWebAppContextListener.injector.getInstance(RealtimeDataService.class);
        historyDataService = MyWebAppContextListener.injector.getInstance(HistoryDataService.class);
    }

    public void destroy() {
        DataSource dataSource = MyWebAppContextListener.injector.getInstance(DataSource.class);
        if (dataSource != null && dataSource instanceof  DruidDataSource) {
            ((DruidDataSource) dataSource).close();
        }
        KVStore kvStore = MyWebAppContextListener.injector.getInstance(KVStore.class);
        if (kvStore != null) {
            kvStore.close();
        }
        JedisPool jedisPool = MyWebAppContextListener.injector.getInstance(JedisPool.class);
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

}
