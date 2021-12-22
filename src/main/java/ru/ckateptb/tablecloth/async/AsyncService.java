package ru.ckateptb.tablecloth.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Service
@EnableScheduling
public class AsyncService {
    private final Map<CompletableFuture<Object>, Consumer<Object>> completableFutures = new HashMap<>();

    /**
     * Выполнить действие supplier асинхронно, после выполнения supplier выполнить handler,
     * который содержит результат supplier
     * @param supplier действие, которое нужно выполнить асинхронно
     * @param handler действие, которое выполняется по завершению handler
     * @return CompletableFuture#supplyAsync(supplier)
     */
    public CompletableFuture<Object> supplyAsync(Supplier<Object> supplier, Consumer<Object> handler) {
        CompletableFuture<Object> completableFuture = CompletableFuture.supplyAsync(supplier);
        completableFutures.put(completableFuture, handler);
        return completableFuture;
    }

    @Scheduled(fixedRate = 1)
    public void onTick() {
        List<CompletableFuture<Object>> toRemove = new ArrayList<>();
        completableFutures.forEach((completableFuture, handler) -> {
            if (completableFuture.isDone()) {
                toRemove.add(completableFuture);
                try {
                    handler.accept(completableFuture.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        toRemove.forEach(completableFutures::remove);
    }
}
