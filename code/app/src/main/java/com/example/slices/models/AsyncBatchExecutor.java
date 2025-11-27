package com.example.slices.models;

import com.example.slices.interfaces.DBWriteCallback;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Utility class for running batches of asynchronous Firestore-style operations.
 * Each operation is expressed as a Runnable that triggers a DBWriteCallback.
 * A single final callback is invoked once all operations have completed:
 *  - onSuccess() when every operation succeeds
 *  - onFailure() when any operation fails
 */
public class AsyncBatchExecutor {

    /**
     * Runs a batch of asynchronous operations and waits for all of them to complete.
     *
     * @param operations
     *      List of operations, each accepting a DBWriteCallback
     * @param finalCallback
     *      Callback invoked once the batch completes
     */
    public static void runBatch(List<Consumer<DBWriteCallback>> operations, DBWriteCallback finalCallback) {
        if (operations == null || operations.isEmpty()) {
            if (finalCallback != null) finalCallback.onSuccess();
            return;
        }

        CountDownLatch latch = new CountDownLatch(operations.size());
        AtomicBoolean failed = new AtomicBoolean(false);

        for (Consumer<DBWriteCallback> op : operations) {
            op.accept(new DBWriteCallback() {
                @Override
                public void onSuccess() {
                    latch.countDown();
                }

                @Override
                public void onFailure(Exception e) {
                    failed.set(true);
                    latch.countDown();
                }
            });
        }

        new Thread(() -> {
            try {
                latch.await();

                if (finalCallback == null) return;

                if (failed.get()) {
                    finalCallback.onFailure(new Exception("Batch operation failed"));
                } else {
                    finalCallback.onSuccess();
                }

            } catch (InterruptedException e) {
                if (finalCallback != null) {
                    finalCallback.onFailure(e);
                }
            }
        }).start();
    }

}
