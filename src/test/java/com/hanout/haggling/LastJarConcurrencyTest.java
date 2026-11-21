package com.hanout.haggling;

import com.hanout.catalog.Sku;
import com.hanout.catalog.SkuRepository;
import com.hanout.common.Money;
import com.hanout.inventory.InventoryService;
import com.hanout.inventory.OutOfStockException;
import com.hanout.inventory.ReservationReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LastJarConcurrencyTest {

    @Autowired
    private SkuRepository skuRepository;

    @Autowired
    private InventoryService inventoryService;

    @Test
    @DisplayName("Last-Jar Test: 50 concurrent threads competing for 1 unit in stock -> exactly 1 succeeds")
    void testFiftyThreadsOnLastJar() throws InterruptedException {
        // Find seeded SKU and reset to exactly 1 unit in stock
        Sku lastJarSku = skuRepository.findById(4L).orElseThrow();
        lastJarSku.setStockOnHand(1);
        lastJarSku.setReserved(0);
        lastJarSku = skuRepository.saveAndFlush(lastJarSku);
        final Long skuId = lastJarSku.getId();

        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startGate.await(); // ensure all 50 threads fire at the exact same instant
                    inventoryService.reserveStock(skuId, 1, ReservationReason.OFFER, "OFFER-CONCURRENT-" + index, LocalDateTime.now().plusHours(1));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    endGate.countDown();
                }
            });
        }

        // Fire all threads simultaneously
        startGate.countDown();
        boolean completed = endGate.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(49);

        // Verify SKU inventory
        Sku finalSku = skuRepository.findById(skuId).orElseThrow();
        assertThat(finalSku.getReserved()).isEqualTo(1);
        assertThat(finalSku.getAvailableStock()).isEqualTo(0);
    }
}
