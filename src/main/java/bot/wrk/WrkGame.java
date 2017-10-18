/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bot.wrk;

import bot.Constant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.EventListener;

/**
 *
 * @author virus
 */
public class WrkGame implements EventListener {

    public WrkGame() {
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof GuildVoiceJoinEvent) {
            if (((GuildVoiceJoinEvent) event).getChannelJoined().getId().equals(Constant.getVoiceChannelConf().getProperty(((GuildVoiceJoinEvent) event).getChannelJoined().getGuild().getId()))) {
                if (((GuildVoiceJoinEvent) event).getMember().getGame() != null) {
                    askGame(event);
                }
            }
        } else if (event instanceof GuildVoiceMoveEvent) {
            if (((GuildVoiceMoveEvent) event).getChannelJoined().getId().equals(Constant.getVoiceChannelConf().getProperty(((GuildVoiceMoveEvent) event).getChannelJoined().getGuild().getId()))) {
                if (((GuildVoiceMoveEvent) event).getMember().getGame() != null) {
                    askGame(event);
                }
            }
            if (Constant.gameList.containsKey(((GuildVoiceMoveEvent) event).getGuild().getId())) {
                for (Iterator<VoiceChannel> it = Constant.gameList.get(((GuildVoiceMoveEvent) event).getGuild().getId()).iterator(); it.hasNext();) {
                    VoiceChannel voiceChannel = it.next();
                    if (voiceChannel.equals(((GuildVoiceMoveEvent) event).getChannelLeft())) {
                        if (((GuildVoiceMoveEvent) event).getChannelLeft().getMembers().isEmpty()) {
                            ((GuildVoiceMoveEvent) event).getChannelLeft().delete().queue();
                            it.remove();
                        }
                    }
                }
            }
        } else if (event instanceof GuildVoiceLeaveEvent) {
            if (Constant.gameList.containsKey(((GuildVoiceLeaveEvent) event).getGuild().getId())) {
                for (Iterator<VoiceChannel> it = Constant.gameList.get(((GuildVoiceLeaveEvent) event).getGuild().getId()).iterator(); it.hasNext();) {
                    VoiceChannel voiceChannel = it.next();
                    if (voiceChannel.equals(((GuildVoiceLeaveEvent) event).getChannelLeft())) {
                        if (((GuildVoiceLeaveEvent) event).getChannelLeft().getMembers().isEmpty()) {
                            ((GuildVoiceLeaveEvent) event).getChannelLeft().delete().queue();
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    private void askGame(Event event) {
        GenericGuildVoiceEvent gEvent = (GenericGuildVoiceEvent) event;
        TextChannel textChannel;
        textChannel = event.getJDA().getTextChannelById(Constant.getTextChannelConf().getProperty(gEvent.getGuild().getId()));
        Message msg = textChannel.sendMessage(" Hi " + gEvent.getMember().getAsMention() + ", wanna be moved in your game's Channel?\nReact:").complete();
        msg.addReaction("🆗").queue();
        msg.addReaction("❌").queue();
        Constant.waiter.waitForEvent(MessageReactionAddEvent.class,
                e -> e.getMessageId().equals(msg.getId()) && e.getChannel().equals(textChannel) && e.getMember().equals(gEvent.getMember()),
                e -> {
                    if (e.getReaction().getEmote().getName().equals("🆗")) {
                        msg.delete().queue();
                        gameYes(event);
                    } else if (e.getReaction().getEmote().getName().equals("❌")) {
                        msg.delete().queue();
                    }
                }, 10, TimeUnit.SECONDS, () -> msg.delete().queue());
    }

    private void gameYes(Event event) {
        GenericGuildVoiceEvent gEvent = (GenericGuildVoiceEvent) event;
        VoiceChannel gameVC = event.getJDA().getVoiceChannelById(Constant.getVoiceChannelConf().getProperty(gEvent.getGuild().getId()));
        if (Constant.gameList.containsKey(gEvent.getGuild().getId())) {
            if (Constant.gameList.get(gEvent.getGuild().getId()).isEmpty()) {
                List<VoiceChannel> list = Collections.synchronizedList(new ArrayList<>());
                VoiceChannel voiceChannel = (VoiceChannel) gEvent.getGuild().getController().createVoiceChannel(gEvent.getMember().getGame().getName()).complete();
                gEvent.getGuild().getController().moveVoiceMember(gEvent.getMember(), voiceChannel).complete();
                if (gameVC.getParent() != null) {
                    voiceChannel.getManager().setParent(gameVC.getParent()).complete();
                }
                int oui = gEvent.getGuild().getVoiceChannels().indexOf(event.getJDA().getVoiceChannelById(Constant.getVoiceChannelConf().getProperty(gEvent.getGuild().getId())));
                gEvent.getGuild().getController().modifyVoiceChannelPositions().selectPosition(voiceChannel).moveTo(oui + 1).complete();
                list.add(voiceChannel);
                Constant.gameList.get(gEvent.getGuild().getId()).add(voiceChannel);
            } else {
                for (VoiceChannel voiceChannel : Constant.gameList.get(gEvent.getGuild().getId())) {
                    if (!gEvent.getGuild().getVoiceChannels().contains(voiceChannel)) {
                        voiceChannel = (VoiceChannel) gEvent.getGuild().getController().createVoiceChannel(gEvent.getMember().getGame().getName()).complete();
                        gEvent.getGuild().getController().moveVoiceMember(gEvent.getMember(), voiceChannel).complete();
                        if (gameVC.getParent() != null) {
                            voiceChannel.getManager().setParent(gameVC.getParent()).complete();
                        }
                        int oui = gEvent.getGuild().getVoiceChannels().indexOf(event.getJDA().getVoiceChannelById(Constant.getVoiceChannelConf().getProperty(gEvent.getGuild().getId())));
                        gEvent.getGuild().getController().modifyVoiceChannelPositions().selectPosition(voiceChannel).moveTo(oui + 1).complete();
                        Constant.gameList.get(gEvent.getGuild().getId()).add(voiceChannel);
                    } else {
                        gEvent.getGuild().getController().moveVoiceMember(gEvent.getMember(), voiceChannel).complete();
                    }
                }
            }
        } else {
            List<VoiceChannel> list = Collections.synchronizedList(new ArrayList<>());
            VoiceChannel voiceChannel = (VoiceChannel) gEvent.getGuild().getController().createVoiceChannel(gEvent.getMember().getGame().getName()).complete();
            gEvent.getGuild().getController().moveVoiceMember(gEvent.getMember(), voiceChannel).complete();
            if (gameVC.getParent() != null) {
                voiceChannel.getManager().setParent(gameVC.getParent()).complete();
            }
            int oui = gEvent.getGuild().getVoiceChannels().indexOf(event.getJDA().getVoiceChannelById(Constant.getVoiceChannelConf().getProperty(gEvent.getGuild().getId())));
            gEvent.getGuild().getController().modifyVoiceChannelPositions().selectPosition(voiceChannel).moveTo(oui + 1).complete();
            list.add(voiceChannel);
            Constant.gameList.put(gEvent.getGuild().getId(), list);
        }
    }
}
