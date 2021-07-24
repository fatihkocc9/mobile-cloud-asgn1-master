/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup.model;


import org.magnum.dataup.VideoFileManager;
import org.magnum.dataup.model.VideoStatus.VideoState;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;


@Controller
public class VideoController{

	private Collection<Video> videos = new CopyOnWriteArrayList<Video>();
	public static final String DATA_PARAMETER = "data";
	public static final String ID_PARAMETER = "id";
	public static final String VIDEO_SVC_PATH = "/video";	
	public static final String VIDEO_DATA_PATH = VIDEO_SVC_PATH + "/{id}/data";
	private VideoId newId = new VideoId();
	private VideoFileManager newVideoFileManager;


	private String getDataUrl(long videoId){
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }
    
     private String getUrlBaseForLocalServer() {
       HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
       String base = 
          "http://"+request.getServerName() 
          + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
       return base;
    }


	@RequestMapping(value= VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList(){
		return videos;
	}
	
	@RequestMapping(value = VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v){
		v = newId.save(v);
		v.setDataUrl(getDataUrl(v.getId()));
		videos.add(v);
		return v;
	}


	public void saveSomeVideo(Video v, MultipartFile videoData) throws IOException {
		newVideoFileManager.saveVideoData(v.getId(), videoData.getInputStream());
	}

	@RequestMapping(value = VIDEO_DATA_PATH, method = RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable Long id, @RequestParam("data") MultipartFile videoData, HttpServletResponse response) throws IOException{
		
		if(newId.isExists(id)){
			try {
				newVideoFileManager = VideoFileManager.get();
				newVideoFileManager.saveVideoData(id, videoData.getInputStream());
				response.setHeader("Content-Type", "application/json");
				response.setStatus(200);
			} catch (Exception e) {
				response.setStatus(500);
				e.printStackTrace();
			}
		}
		else{
			response.setStatus(404);
		}
		return new VideoStatus(VideoState.READY);
	}


	@RequestMapping(value = VIDEO_DATA_PATH, method = RequestMethod.GET)
	public void getData(@PathVariable("id") Long id, HttpServletResponse response) throws IOException{
		
		if( newId.isExists(id)){
			newVideoFileManager.copyVideoData(id, response.getOutputStream());
			response.flushBuffer();
			response.setHeader("Content-Type", "application/json");
			response.setStatus(200);
		}
		else{
			response.sendError(404);
		}
	}

}
