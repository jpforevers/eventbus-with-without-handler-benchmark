package com.example.eventbus_with_without_handler_benchmark;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput) // 测试吞吐量
@OutputTimeUnit(TimeUnit.MILLISECONDS) // 输出单位
@State(Scope.Thread) // 每个线程独立状态
public class EventBusBenchmark {

  private Vertx vertx;
  private EventBus eventBus;
  private static final String ADDRESS_WITH_HANDLER = "address.with.handler";
  private static final String ADDRESS_WITHOUT_HANDLER = "address.without.handler";

  @Setup(Level.Trial) // 在所有测试开始前运行一次
  public void setup() {
    vertx = Vertx.vertx();
    eventBus = vertx.eventBus();

    // 注册一个Handler到指定地址
    eventBus.consumer(ADDRESS_WITH_HANDLER, message -> {});
  }

  @TearDown(Level.Trial) // 在所有测试结束后运行一次
  public void tearDown() {
    vertx.close();
  }

  @Benchmark
  public void sendToAddressWithHandler() {
    eventBus.publisher(ADDRESS_WITH_HANDLER).write("Hello");
  }

  @Benchmark
  public void sendToAddressWithoutHandler() {
    eventBus.publisher(ADDRESS_WITHOUT_HANDLER).write("Hello");
  }

}
