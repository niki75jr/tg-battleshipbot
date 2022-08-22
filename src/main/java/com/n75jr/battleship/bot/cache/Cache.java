package com.n75jr.battleship.bot.cache;

import java.util.Collection;
import java.util.Optional;

public interface Cache<K, V> {
	
    void add(V data);
    V remove(K key);
    boolean contains(K key);
    Optional<V> get(K id);
    Collection<V> getAll();
    int getCount();
}
