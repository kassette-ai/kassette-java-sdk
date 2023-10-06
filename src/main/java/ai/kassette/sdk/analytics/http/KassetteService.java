package ai.kassette.sdk.analytics.http;

import ai.kassette.sdk.analytics.messages.Batch;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface KassetteService {
    @POST
    @Headers(value = {"Content-Type:application/json"})
    Call<ResponseBody> upload(@Url HttpUrl uploadUrl, @Body Batch batch);
}
