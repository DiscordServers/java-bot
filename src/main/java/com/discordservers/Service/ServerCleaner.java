package com.discordservers.Service;

import com.discordservers.Bot;
import io.swagger.client.ApiException;
import io.swagger.client.api.ServerApi;
import net.dv8tion.jda.core.entities.Guild;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ServerCleaner {
    private final int timer = 60 * 60 * 1000;

    private final Bot bot;
    private final ServerApi api;

    public ServerCleaner(Bot bot) {
        this.bot = bot;
        this.api = bot.api.serverApi;

        new Timer().scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            process();
                        } catch (ApiException e) {
                            e.printStackTrace();
                        }
                    }
                },
                timer,
                timer
        );
    }

    public void process() throws ApiException {
        List<Long> activeServers = this.getActiveServers();
        int count = 0;
        for (Long id : activeServers) {
            Guild guild = this.bot.jda.getGuildById(id);
            if (guild == null) {
                this.api.delist(id);
                count++;
            }
        }

        System.out.format("Delisted %d servers\n", count);
    }

    private List<Long> getActiveServers() throws ApiException {
        List<Long> servers = new ArrayList<>();
        int from = 0;
        while (true) {
            List<Long> result = this.api.getActiveServers(from, 5000);
            servers.addAll(result);
            from += 5000;
            if (result.size() < 5000) {
                break;
            }
        }


        return servers;
    }
}
