package com.sovon9.RRMS_Portal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.sovon9.RRMS_Portal.dto.RoomDto;

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class RoomsService
{
	Logger LOGGER = LoggerFactory.getLogger(RoomsService.class);
	@Autowired
	private RestTemplate restTemplate;
	
	private List<RoomDto> roomDtos;
	
	@Value("${ROOM_MGMT_SERVICE_URL}")
	private String ROOM_MGMT_SERVICE_URL;
	
	public List<Integer> getRoomsByRatePlan(String ratePlan)
	{
		return roomDtos.stream().filter(rp->rp.getRatePlan().equals(ratePlan)).map(rp->rp.getRoomNum()).collect(Collectors.toList());
	}
	
	@Retry(name="retry-Rateplan", fallbackMethod = "fallBackRateplanRoomData")
	public List<RoomDto> getAllAvlRateplanRoomData(String jwtToken)
	{
		roomDtos = new ArrayList<>();
		try
		{
			 HttpHeaders headers = new HttpHeaders();
		        headers.set("Authorization", "Bearer " + jwtToken);
		    HttpEntity<?> httpEntity = new HttpEntity<>(headers);
		    
		    ResponseEntity<RoomDto[]> responseEntity = restTemplate
	                .exchange(ROOM_MGMT_SERVICE_URL + "getAllAvailableRooms/roomStatus/VC", HttpMethod.GET, httpEntity, RoomDto[].class);
			if (responseEntity.getStatusCode() == HttpStatus.OK)
			{
				roomDtos = List.of(responseEntity.getBody());
				LOGGER.error("Successfully fetched room data: " + roomDtos);
			}
		}
		catch (ResourceAccessException e) {
			LOGGER.error("Error fetching RatePlan RoomData : "+e.getMessage());
		}
		return roomDtos;
	}
	
	public List<RoomDto> fallBackRateplanRoomData(Exception exception)
	{
		return List.of();
	}

	@Retry(name="retry-Rateplan", fallbackMethod = "fallBackRateplanRoomData")
	public List<RoomDto> getAllBlkdRateplanRoomData(String jwtToken)
	{
		List<RoomDto> blkdRoomList = new ArrayList<>();
		try
		{
			 HttpHeaders headers = new HttpHeaders();
		        headers.set("Authorization", "Bearer " + jwtToken);
		    HttpEntity<?> httpEntity = new HttpEntity<>(headers);
		    
		    ResponseEntity<RoomDto[]> responseEntity = restTemplate
	                .exchange(ROOM_MGMT_SERVICE_URL + "getAllAvailableRooms/roomStatus/VB", HttpMethod.GET, httpEntity, RoomDto[].class);
			if (responseEntity.getStatusCode() == HttpStatus.OK)
			{
				blkdRoomList = List.of(responseEntity.getBody());
				LOGGER.error("Successfully fetched room data: " + roomDtos);
			}
		}
		catch (ResourceAccessException e) {
			LOGGER.error("Error fetching RatePlan RoomData : "+e.getMessage());
		}
		return blkdRoomList;
	}
	
	@Retry(name = "avl-rateplanRoom", fallbackMethod = "fallBackAvlRatePlanRoom")
	public Set<String> getAllAvlRateplanData(String jwtToken)
	{
		List<RoomDto> roomData = getAllAvlRateplanRoomData(jwtToken);
		return roomData.stream().map(r->r.getRatePlan()).collect(Collectors.toSet());
	}
	
	public Set<String> fallBackAvlRatePlanRoom(String jwtToken, Throwable throwable)
	{
		return Set.of();
	}
}
