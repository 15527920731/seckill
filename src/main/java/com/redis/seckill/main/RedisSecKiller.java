package com.redis.seckill.main;

import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisSecKiller {
    private static final int N_THREADS=5;//模拟用户抢购，最大并发数
    public static final String WATCH_KEY="Goods";//监控键
    private static final int GOODS_NUM=100;
    private static final int USER_NUM=5000;


    public static void main(String[] args) {
        //统资源进
        System.out.println(Runtime.getRuntime().availableProcessors());
        ExecutorService executorService=Executors.newFixedThreadPool(N_THREADS);
        Jedis jedis=JedisUtils.getJedis();
        jedis.set(WATCH_KEY,String.valueOf(GOODS_NUM));
        jedis.close();


        for (int i=1;i<=USER_NUM;i++) {
            executorService.execute(new JedisRunnable(UUID.randomUUID().toString(),i));
        }

        executorService.shutdown();
    }
}
