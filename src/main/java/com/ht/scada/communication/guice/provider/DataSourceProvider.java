package com.ht.scada.communication.guice.provider;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.inject.Provider;
import com.ht.scada.communication.Config;

import javax.sql.DataSource;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: 薄成文 13-8-15 下午11:57
 * To change this template use File | Settings | File Templates.
 */
public class DataSourceProvider implements Provider<DataSource> {

    private DruidDataSource dataSource;

    @Override
    public DataSource get() {

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

        return dataSource;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
