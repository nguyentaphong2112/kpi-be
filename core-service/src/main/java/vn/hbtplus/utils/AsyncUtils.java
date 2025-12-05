package vn.hbtplus.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
public class AsyncUtils {
    private static final ThreadLocal<Boolean> inAsyncContext = ThreadLocal.withInitial(() -> false);

    /**
     * Chạy các task song song, nếu 1 task lỗi -> cancel toàn bộ ngay lập tức (fail-fast)
     */
    public static void runParallel(ExecutorService executor, Runnable... tasks) {
        if (inAsyncContext.get()) {
            log.debug("Detected nested async context -> running tasks synchronously to prevent deadlock.");
            Arrays.stream(tasks).forEach(Runnable::run);
            return;
        }

        inAsyncContext.set(true);
        List<Future<?>> futures = null;

        try {
            futures = Arrays.stream(tasks)
                    .map(task -> executor.submit(() -> {
                        try {
                            task.run();
                        } catch (Exception ex) {
                            log.error("Task failed: {}", ex.getMessage(), ex);
                            throw ex; // ném exception ra để trigger cancel
                        }
                    }))
                    .collect(Collectors.toList());

            for (Future<?> future : futures) {
                try {
                    future.get(); // chờ từng task
                } catch (ExecutionException e) {
                    log.error("Fail-fast triggered: cancelling remaining tasks...");
                    cancelAll(futures);
                    throw new RuntimeException(e.getCause());
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Async execution interrupted: {}", e.getMessage(), e);
        } finally {
            inAsyncContext.remove();
        }
    }

    private static void cancelAll(List<Future<?>> futures) {
        futures.forEach(f -> {
            if (!f.isDone()) {
                f.cancel(true);
            }
        });
    }
}
