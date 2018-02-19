package com.discordservers.Api;

import com.discordservers.Bot;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Response;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SecurityApi;
import io.swagger.client.api.ServerApi;
import io.swagger.client.model.InlineResponse200;

import java.lang.reflect.Type;
import java.util.Timer;
import java.util.TimerTask;

public class ReauthedApiClient extends ApiClient {
    public final SecurityApi securityApi;
    public final ServerApi serverApi;
    private String refreshToken;

    public ReauthedApiClient(Bot bot) throws ApiException {
        this.securityApi = new SecurityApi(this);
        this.serverApi = new ServerApi(this);

        InlineResponse200 response = this.securityApi.authenticate(System.getenv("API_USERNAME"), System.getenv("API_PASSWORD"));
        this.refreshToken = response.getRefreshToken();

        this.setApiKey(response.getToken());
        this.setApiKeyPrefix("Bearer");

        new Timer().scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        refresh();
                    }
                },
                30 * 30 * 1000,
                30 * 30 * 1000
        );
    }

    @Override
    public <T> ApiResponse<T> execute(Call call, Type returnType) throws ApiException {
        try {
            return super.execute(call, returnType);
        } catch (ApiException e) {
            if (e.getMessage().equals("retry")) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }

                return this.execute(call, returnType);
            }

            throw e;
        }
    }

    @Override
    public <T> T handleResponse(Response response, Type returnType) throws ApiException {
        if (response.code() == 401) {
            this.refresh();

            throw new ApiException("retry");
        }

        return super.handleResponse(response, returnType);
    }


    private void refresh() {
        try {
            InlineResponse200 response = this.securityApi.refresh(refreshToken);

            this.refreshToken = response.getRefreshToken();
            this.setApiKey(response.getToken());
        } catch (ApiException e) {
            try {
                System.out.println("Failed to refresh token");
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }

            refresh();
        }
    }
}
