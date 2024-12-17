package com.example.eventbus_with_without_handler_benchmark;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput) // 测试吞吐量
@OutputTimeUnit(TimeUnit.SECONDS) // 输出单位
@State(Scope.Thread) // 每个线程独立状态
public class EventBusBenchmark {

  private static final String ADDRESS_WITH_HANDLER = "address.with.handler";
  private static final String ADDRESS_WITHOUT_HANDLER = "address.without.handler";

  private static final String MODE_NON_CLUSTERED = "non-clustered";
  private static final String MODE_CLUSTERED = "clustered";

  @Param({MODE_NON_CLUSTERED, MODE_CLUSTERED})
  private String mode; // 测试模式：非集群或集群

  private Vertx vertx;
  private EventBus eventBus;

  @Setup(Level.Trial) // 在所有测试开始前运行一次
  public void setup() {
    if (MODE_NON_CLUSTERED.equals(mode)) {
      vertx = Vertx.vertx();
    } else {
      CountDownLatch countDownLatch = new CountDownLatch(1);
      Vertx.clusteredVertx(new VertxOptions()).onComplete(ar -> {
        countDownLatch.countDown();
        if (ar.succeeded()) {
          vertx = ar.result();
        } else {
          throw new RuntimeException("Failed to start clustered Vert.x");
        }
      });
      try {
        countDownLatch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    eventBus = vertx.eventBus();
    // 注册一个Handler到指定地址
    eventBus.consumer(ADDRESS_WITH_HANDLER, message -> {});
  }

  @TearDown(Level.Trial) // 在所有测试结束后运行一次
  public void tearDown() {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    vertx.close().onComplete(ar -> countDownLatch.countDown());
    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Benchmark
  public void sendToAddressWithHandler() {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    eventBus.publisher(ADDRESS_WITH_HANDLER).write("Hello")
      .onComplete(ar -> countDownLatch.countDown());
    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Benchmark
  public void sendToAddressWithoutHandler() {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    eventBus.publisher(ADDRESS_WITHOUT_HANDLER).write("Hello")
      .onComplete(ar -> countDownLatch.countDown());
    try {
      countDownLatch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
