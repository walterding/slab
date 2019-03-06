package com.front.resin.slab;

/**
 * Created by hinotohui on 17/3/4.
 */

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PageTable {

	private final Map<Long, SlabPage> pageTable = new HashMap<Long, SlabPage>();
	private long addr;
	private ReadWriteLock lock = new ReentrantReadWriteLock();

	final long generatePageNo(SlabPage obj) {
		lock.writeLock().lock();
		final long start = addr;
		try {
			do {
				addr++;
				if (addr == -1)
					continue;
				if (!pageTable.containsKey(addr)) {
					pageTable.put(addr, obj);
					return addr;
				}
			} while (start != addr);
			return -1;
		} finally {
			lock.writeLock().unlock();
		}
	}

	final SlabPage removePage(long pageNo) {
		lock.writeLock().lock();
		try {
			return pageTable.remove(pageNo);
		} finally {
			lock.writeLock().unlock();
		}
	}

	final SlabPage getPage(long pageNo) {
		lock.readLock().lock();
		try {
			return pageTable.get(pageNo);
		} finally {
			lock.readLock().unlock();
		}
	}

}
