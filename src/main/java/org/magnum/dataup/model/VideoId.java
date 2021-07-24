package org.magnum.dataup.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;



public class VideoId{
    
    private static final AtomicLong currentId = new AtomicLong(0L);
	
    private Map<Long,Video> videos1 = new HashMap<Long, Video>();
    
      public Video save(Video entity) {
        checkAndSetId(entity);
        videos1.put(entity.getId(), entity);
        return entity;
    }
    
    private void checkAndSetId(Video entity) {
        if(entity.getId() == 0){
            entity.setId(currentId.incrementAndGet());
        }
    }

    public boolean isExists(Long id){
        return videos1.containsKey(id);
    }

}