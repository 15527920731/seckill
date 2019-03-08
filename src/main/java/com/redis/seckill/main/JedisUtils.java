package com.redis.seckill.main;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisUtils {
    private static final String ADDR="127.0.0.1";
    private static final int PORT=6379;
    private static final boolean TEST_ON_BORROW=true;//在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；默认是false
    private static final int MAX_IDLE=200;//在容器中的最小的闲置连接数，仅仅在此值为正数且timeBetweenEvictionRunsMillis值大于0时有效
                                             //确保在对象逐出线程工作后确保线程池中有最小的实例数量，如果该值设定大于maxidle的值，此值不会生效，maxidle的值会生效

    private static JedisPool jedisPool=null;

    static {
        try {
            JedisPoolConfig config=new JedisPoolConfig();
            config.setMaxIdle(MAX_IDLE);
            config.setTestOnBorrow(TEST_ON_BORROW);
            jedisPool=new JedisPool(config,ADDR,PORT);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 获取Jedis实例
     * @return
     */
    public synchronized static Jedis getJedis(){
        try{
            if(jedisPool!=null){
                Jedis resource=jedisPool.getResource();
                return resource;
            }else{
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    public static void returnResource(final Jedis jedis){
        //JedisPool 的 returnResource 方法遭废弃，改用 close 替代
        if(jedis!=null){
            jedis.close();
        }

    }

}
