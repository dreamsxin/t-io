package org.tio.http.server.session.impl;

import java.util.concurrent.TimeUnit;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.LocalCachedMapOptions.EvictionPolicy;
import org.redisson.api.LocalCachedMapOptions.InvalidationPolicy;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.tio.http.server.session.HttpSession;
import org.tio.http.server.session.IHttpSessionStore;

/**
 * 
 * @author tanyaowu 
 * 2017年8月5日 上午10:16:26
 */
public class RedisHttpSessionStore implements IHttpSessionStore {

	private RedissonClient redisson;
	
	private Long sessionTimeout = null;

	private RLocalCachedMap<String, HttpSession> cachedMap = null;
	
	@SuppressWarnings("rawtypes")
	private LocalCachedMapOptions options;
	
	private static RedisHttpSessionStore instance;

	public static RedisHttpSessionStore getInstance(RedissonClient redisson, Long sessionTimeout) {
		if (instance == null) {
			synchronized (RedisHttpSessionStore.class) {
				if (instance == null) {
					instance = new RedisHttpSessionStore(redisson, sessionTimeout);
				}
			}
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	private RedisHttpSessionStore(RedissonClient redisson, Long sessionTimeout) {
		this.redisson = redisson;
		this.sessionTimeout = sessionTimeout;
		
		options = LocalCachedMapOptions.defaults()
			      // 淘汰机制有LFU, LRU和NONE这几种算法策略可供选择
			     .evictionPolicy(EvictionPolicy.LFU)
			     .cacheSize(100000)
			      // 如果该值是`真(true)`时，在该实例执行更新和删除操作的同时，将向其他所有的相同实例发
			      // 送针对该元素的淘汰消息。其他相同实例在收到该消息以后，会同时删除自身的缓存。下次读取
			      // 该元素时会从Redis服务器获取。
//			     .invalidateEntryOnChange(false)
//			     .invalidationPolicy(InvalidationPolicy.ON_CHANGE)  //true
			     .invalidationPolicy(InvalidationPolicy.NONE)  //false
			      // 每个Map本地缓存里元素的有效时间，默认毫秒为单位
//			     .timeToLive(10000)
			      // 或者
			     .timeToLive(0, TimeUnit.SECONDS)
			      // 每个Map本地缓存里元素的最长闲置时间，默认毫秒为单位
//			     .maxIdle(10000)
			      // 或者
			     .maxIdle(this.sessionTimeout, TimeUnit.SECONDS);
		
		cachedMap = this.redisson.getLocalCachedMap("tio-session-store", options);

	}
	
	@Override
	public void save(String sessionId, HttpSession session) {
//		redisson.getLocalCachedMap(name, options)
		cachedMap.put(sessionId, session);
	}

	@Override
	public void remove(String sessionId) {
		cachedMap.remove(sessionId);
	}

	@Override
	public HttpSession get(String sessionId) {
		return cachedMap.get(sessionId);
	}
	
}
