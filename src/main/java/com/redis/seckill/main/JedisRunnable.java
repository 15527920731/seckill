package com.redis.seckill.main;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

public class JedisRunnable implements Runnable {

    private Jedis jedis=JedisUtils.getJedis();
    private String userId;
    private int index;

    public JedisRunnable(String userId,int index) {
        this.userId = userId;
        this.index=index;
    }

    @Override
    public void run() {
        try{
            //jedis.watch一般是和事务一起使用，当对某个key进行watch后如果其他的客户端对这个key进行了更改，
            // 那么本次事务会被取消，事务的exec会返回null。jedis.watch(key)都会返回OK
            jedis.watch(RedisSecKiller.WATCH_KEY);
            int leftGoodsNum=Integer.valueOf(jedis.get(RedisSecKiller.WATCH_KEY));
            if(leftGoodsNum>0){
                //获取事物
                Transaction tx=jedis.multi();
                //值减1
                tx.decrBy(RedisSecKiller.WATCH_KEY,1);

                //拿到减后的值，拿不到就没抢到
                List<Object> results=tx.exec();

                if(results==null || results.isEmpty()){
                    String failUserInfo="fail";
                    String failMsg=index+"用户"+failUserInfo+"抢购失败，剩余"+leftGoodsNum+"----"+userId;
                    System.out.println(failMsg);
                    //SETNX ：SET if Not eXists （如果不存在，则 SET)的简写
                    jedis.setnx(failUserInfo,failMsg);


                }else{//此时tx.exec()事物执行成功会自动提交事物

                    for (Object succ:results){
                        //
                        String succUserInfo="succ"+(100-Integer.parseInt(results.get(0).toString()));
                        String succMsg=index+"用户"+succUserInfo+"抢购成功，剩余"+Integer.parseInt(results.get(0).toString())+"----"+userId;
                        System.out.println(succMsg);
                        jedis.setnx(succUserInfo,succMsg);
                    }

                }

            }else{//库存0，结束
                String overUserInfo="over……………………"+userId;
                String overMsg=index+"用户"+overUserInfo+"抢购成功，剩余"+leftGoodsNum;
                System.out.println(overUserInfo);
                jedis.setnx(overUserInfo,overMsg);
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //不能忘记
            JedisUtils.returnResource(jedis);
        }

    }
}
