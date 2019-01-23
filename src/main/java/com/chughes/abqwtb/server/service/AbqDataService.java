package com.chughes.abqwtb.server.service;

import com.chughes.abqwtb.server.model.AbqDataPayload;
import retrofit2.Call;
import retrofit2.http.GET;

public interface AbqDataService {

  @GET("transit/realtime/route/allroutes.json")
  Call<AbqDataPayload> listVehicles();


}
