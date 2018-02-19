package com.discordservers.Queue;

import com.discordservers.Bot;
import io.swagger.client.ApiException;
import io.swagger.client.api.ServerApi;
import io.swagger.client.model.ServerForm;
import net.dv8tion.jda.core.entities.Guild;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static com.discordservers.Builder.ServerFormBuilder.getServerForm;

public class UpdateMemberCountQueue {
    private final int timer = 15 * 60 * 1000;

    private final Bot bot;
    private final ServerApi api;

    private final HashMap<Long, Integer> items = new HashMap<>();

    public UpdateMemberCountQueue(Bot bot) {
        this.bot = bot;
        this.api = bot.api.serverApi;
        new Timer().scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        process();
                    }
                },
                timer,
                timer
        );
    }

    public void push(long server, int members) {
        this.items.put(server, members);
    }

    private void process() {
        HashMap<Long, Integer> items = new HashMap<>(this.items);
        this.items.clear();

        if (items.size() == 0) {
            return;
        }

        items.forEach(this::sendToApi);
    }

    private void sendToApi(Long server, Integer members) {
        this.bot.statsd.increment("event.updateMemberCount");
        try {
            ServerForm form = getServerForm(this.bot.jda.getGuildById(server));
            form.members(members);

            this.api.upsert(server, form);
            this.bot.statsd.increment("event.success");
        } catch (ApiException e) {
            System.out.println(e.getResponseBody());
            e.printStackTrace();
            this.bot.statsd.increment("event.failure");
        }
    }
}
