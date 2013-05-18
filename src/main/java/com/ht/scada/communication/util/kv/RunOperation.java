/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2010, 2013 Oracle and/or its affiliates.  All rights reserved.
 *
 */

package com.ht.scada.communication.util.kv;

import oracle.kv.ConsistencyException;
import oracle.kv.DurabilityException;
import oracle.kv.FaultException;
import oracle.kv.RequestTimeoutException;

/**
 * Utility class to run a KVStore operation and handle {@code
 * FaultExceptions} according to this application's policies.  The
 * {@code doOperation} method is abstract and must be implemented by
 * callers.  {@code doOperation} is called by the {@code run()} method
 * of this class.
 *
 * <p>This class is a placeholder for implementing
 * application-specific exception handling policies.  This
 * implementation simply prints the exception details to {@code
 * System.err}.  In addition to performing other actions when an
 * exception occurs, applications may wish to extend this mechanism to
 * perform retries under certain conditions.</p>
 *
 * <p>Note that {@code OperationExecutionException} is not handled by
 * this class because it is a contingency exception that is expected
 * to be handled by the operation itself (the {@code doOperation}
 * method).</p>
 */
public abstract class RunOperation {

    /**
     * Must be implemented to perform the operation.
     */
    public abstract void doOperation();

    /**
     * Calls the doOperation method and handles exceptions as needed.
     */
    public void run() throws FaultException {
        try {
            doOperation();
        } catch (ConsistencyException e) {
            handleConsistencyException(e);
        } catch (DurabilityException e) {
            handleDurabilityException(e);
        } catch (RequestTimeoutException e) {
            handleTimeoutException(e);
        } catch (FaultException e) {
            handleFaultException(e);
        } catch (RuntimeException e) {
            handleOtherException(e);
        }
    }

    /**
     * Placeholder for application-specific handling of DurabilityException.
     *
     * With the default durability configuration setting (see
     * KVStoreConfig.getDurability) this exception should rarely occur, but it
     * may need to be handled in certain circumstances. See the
     * DurabilityException javadoc for more information.
     */
    private void handleDurabilityException(DurabilityException e) {
        handleFaultException(e);
    }

    /**
     * Placeholder for application-specific handling of ConsistencyException.
     *
     * With the default consistency configuration setting (see
     * KVStoreConfig.getConsistency) this exception should rarely occur, but it
     * may need to be handled in certain circumstances. See the
     * ConsistencyException javadoc for more information.
     */
    private void handleConsistencyException(ConsistencyException e) {
        handleFaultException(e);
    }

    /**
     * Placeholder for application-specific handling of
     * RequestTimeoutException.
     *
     * With the default request timeout configuration setting (see
     * KVStoreConfig.getRequestTimeout) this exception should rarely occur, but
     * it may need to be handled in certain circumstances.  See the
     * RequestTimeoutException javadoc for more information.
     */
    private void handleTimeoutException(RequestTimeoutException e) {
        handleFaultException(e);
    }

    /**
     * Placeholder for application-specific handling of FaultException.  This
     * is the base class for unchecked (runtime) exceptions thrown by KVStore.
     *
     * When the exception WAS logged remotely, the client may wish to log the
     * exception for correlating it with KVStore service logs.  When the
     * exception was NOT logged remotely, the client may wish to log and
     * additionally perform necessary notifications.  See the FaultException
     * javadoc for more information.
     */
    private void handleFaultException(FaultException e) {
        /* Can be correlated with server logs using time and fault name. */
        System.err.println("Time: " + System.currentTimeMillis() +
                           " Fault: " + e.getFaultClassName());
        e.printStackTrace();
        if (!e.wasLoggedRemotely()) {
            /* Do any necessary notifications here. */
        }
    }

    /**
     * Placeholder for application-specific handling of unexpected runtime
     * exceptions.
     */
    private void handleOtherException(RuntimeException e) {
        e.printStackTrace();
    }
}
