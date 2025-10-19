package dogapi;

import java.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This BreedFetcher caches fetch request results to improve performance and
 * lessen the load on the underlying data source. An implementation of BreedFetcher
 * must be provided. The number of calls to the underlying fetcher are recorded.
 *
 * If a call to getSubBreeds produces a BreedNotFoundException, then it is NOT cached
 * in this implementation. The provided tests check for this behaviour.
 *
 * The cache maps the name of a breed to its list of sub breed names.
 */
public class CachingBreedFetcher implements BreedFetcher {
    private final BreedFetcher delegate;
    private final Map<String, List<String>> cache = new HashMap<>();

    public CachingBreedFetcher(BreedFetcher delegate) {
        if (delegate == null) throw new IllegalArgumentException("delegate cannot be null");
        this.delegate = delegate;
    }
    private int misses = 0;

    public int getCallsMade() { return misses; } // counts delegate calls

    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        String key = breed.toLowerCase(Locale.ROOT);
        if (cache.containsKey(key)) return cache.get(key);

        misses++; // only increments on cache miss before calling delegate
        List<String> subs = delegate.getSubBreeds(key);
        cache.put(key, List.copyOf(subs));
        return subs;
    }
}
