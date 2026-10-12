package com.peakperformers.smartloadshedding.network;

import com.google.gson.annotations.SerializedName;
import com.peakperformers.smartloadshedding.models.GrantResponse;
import com.peakperformers.smartloadshedding.models.LossEntry;
import com.peakperformers.smartloadshedding.models.OutageScheduleResponse;
import com.peakperformers.smartloadshedding.models.Tip;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @GET("api/schedule")
    Call<List<OutageScheduleResponse>> getSchedule(@Query("area") String areaCode);

    @POST("api/losses")
    Call<LossResponse> logLoss(@Body LossEntry entry);

    @GET("api/losses")
    Call<List<LossEntry>> getLosses(@Query("businessId") int businessId);

    @POST("api/tips")
    Call<TipResponse> postTip(@Body Tip tip);

    @GET("api/tips")
    Call<List<Tip>> getTips();

    @GET("api/grants")
    Call<List<GrantResponse>> getGrants();

    class LossResponse {
        @SerializedName("lossId")
        public int lossId;
    }

    class TipResponse {
        @SerializedName("tipId")
        public int tipId;
    }
}