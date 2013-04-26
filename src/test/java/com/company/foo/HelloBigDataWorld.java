/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2010, 2013 Oracle and/or its affiliates.  All rights reserved.
 *
 */

package com.company.foo;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import oracle.kv.Depth;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.KeyRange;
import oracle.kv.Value;
import oracle.kv.ValueVersion;

/**
 * An extremely simple KVStore client application that writes and reads a
 * single record.  It can be used to validate an installation.
 *
 * Before running this example program, start a KVStore instance.  The simplest
 * way to do that is to run KV Lite as described in the INSTALL document.  Use
 * the KVStore instance name, host and port for running this program, as
 * follows:
 *
 * <pre>
 * java schema.HelloBigDataWorld -store &lt;instance name&gt; \
 *                               -host  &lt;host name&gt;     \
 *                               -port  &lt;port number&gt;
 * </pre>
 *
 * For all examples the default instance name is kvstore, the default host name
 * is localhost and the default port number is 5000.  These defaults match the
 * defaults for running kvlite, so the simplest way to run the examples along
 * with kvlite is to omit all parameters.
 */
public class HelloBigDataWorld {

    private final KVStore store;

    /**
     * Runs the HelloBigDataWorld command line program.
     */
    public static void main(String args[]) {
        try {
            HelloBigDataWorld example = new HelloBigDataWorld(args);
            example.runExample();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parses command line args and opens the KVStore.
     */
    HelloBigDataWorld(String[] argv) {

        String storeName = "kvstore";
        String hostName = "localhost";
        String hostPort = "5000";

        final int nArgs = argv.length;
        int argc = 0;

        while (argc < nArgs) {
            final String thisArg = argv[argc++];

            if (thisArg.equals("-store")) {
                if (argc < nArgs) {
                    storeName = argv[argc++];
                } else {
                    usage("-store requires an argument");
                }
            } else if (thisArg.equals("-host")) {
                if (argc < nArgs) {
                    hostName = argv[argc++];
                } else {
                    usage("-host requires an argument");
                }
            } else if (thisArg.equals("-port")) {
                if (argc < nArgs) {
                    hostPort = argv[argc++];
                } else {
                    usage("-port requires an argument");
                }
            } else {
                usage("Unknown argument: " + thisArg);
            }
        }

        store = KVStoreFactory.getStore
            (new KVStoreConfig(storeName, hostName + ":" + hostPort));
    }

    private void usage(String message) {
        System.out.println("\n" + message + "\n");
        System.out.println("usage: " + getClass().getName());
        System.out.println("\t-store <instance name> (default: kvstore) " +
                           "-host <host name> (default: localhost) " +
                           "-port <port number> (default: 5000)");
        System.exit(1);
    }

    /**
     * Performs example operations and closes the KVStore.
     */
    void runExample() {
    	
        final String keyString = "Hello";
        final String valueString = "Big Data World2!";
        
//        Key key = Key.createKey(Arrays.asList("Hello", "World"), Arrays.asList("login", "2011-01-01"));
//        System.out.println(key.toString());
//
//        store.put(key, Value.createValue(valueString.getBytes()));
//
//        ValueVersion valueVersion = store.get(key);
//
//        System.out.println(valueVersion);
//        System.out.println(new String(valueVersion.getValue().getValue()));
//        
//        valueVersion = store.get(Key.createKey(Arrays.asList("Hello")));
//        System.out.println(valueVersion);
        
        SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
        KeyRange keyRange = new KeyRange("2011-01-01" /*start*/, true /*startInclusive*/,
                         "2013-01-10" /*end*/, false /*endInclusive*/);
        Key parentKey = Key.createKey(Arrays.asList("Hello", "World"), Arrays.asList("login") );
        final Map<Key, ValueVersion> results = store.multiGet
                        (parentKey, keyRange, Depth.CHILDREN_ONLY);

        System.out.println(results.size());
//                    for (Map.Entry<Key, ValueVersion> entry :
//                         results.entrySet()) {
//                        final Object o = KeyDefinition.deserializeAny
//                            (bindings, entry.getKey(),
//                             entry.getValue().getValue());
//                        System.out.println(o.toString());
//                    }

        store.close();
    }
}
