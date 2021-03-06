/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bot.commands.music;

import bot.Constant;
import bot.wrk.music.GuildMusicManager;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.util.ArrayList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;

/**
 *
 * @author renardn
 */
public class QueueCommand extends Command {

    public QueueCommand() {
        this.name = "queue";
        this.help = "display the current queue";
        this.guildOnly = true;
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.isFromType(ChannelType.TEXT)) {
            String id = Constant.getTextChannelConf().getProperty(event.getGuild().getId());
            if (id != null) {
                if (!event.getChannel().getId().equals(id)) {
                    return;
                }
            }
        }
        if (event.getGuild().getAudioManager().isConnected()) {
            GuildMusicManager musicManager = Constant.music.getGuildAudioPlayer(event.getGuild());
            if (musicManager.player.getPlayingTrack() != null) {
                ArrayList<AudioTrack> queue = new ArrayList<>(musicManager.scheduler.getQueue());
                String message = "";
                EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Current queue:");
                builder.setColor(event.getGuild().getSelfMember().getColor());
                if (queue.size() > 4) {
                    message += "1.`" + musicManager.player.getPlayingTrack().getInfo().title + "`";
                    for (int i = 0; i < 4; i++) {
                        message += "\n" + (i + 2) + ".`" + queue.get(i).getInfo().title + "`";
                    }
                    message += "\n\nAnd `" + (queue.size() - 4) + "` more...";
                    builder.setDescription(message);
                    event.reply(builder.build());
                } else if (queue.isEmpty()) {
                    event.replyWarning("The queue is empty");
                } else if (queue.size() <= 4) {
                    message += "1.`" + musicManager.player.getPlayingTrack().getInfo().title + "`";
                    for (int i = 0; i < queue.size(); i++) {
                        message += "\n" + (i + 2) + ".`" + queue.get(i).getInfo().title + "`";
                    }
                    builder.setDescription(message);
                    builder.setFooter(event.getSelfMember().getNickname() != null ? event.getSelfMember().getNickname() : event.getSelfMember().getEffectiveName(), event.getGuild().getIconUrl());
                    event.reply(builder.build());
                }
            }
        } else {
            event.replyError(event.getMember().getAsMention() + " I'm not even connected :joy:");
        }
    }
}
