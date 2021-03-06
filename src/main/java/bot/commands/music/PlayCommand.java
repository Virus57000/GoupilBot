/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bot.commands.music;

import bot.Constant;
import bot.wrk.music.NowPlaying;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.ChannelType;

/**
 *
 * @author renardn
 */
public class PlayCommand extends Command {

    public PlayCommand() {
        this.name = "play";
        this.help = "play a song with the specified url given in argument";
        this.arguments = "[url]";
        this.guildOnly = true;
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        String id = null;
        if (event.isFromType(ChannelType.TEXT)) {
            id = Constant.getTextChannelConf().getProperty(event.getGuild().getId());
            if (id != null) {
                if (!event.getChannel().getId().equals(id)) {
                    return;
                }
            }
        }
        System.out.println("1");
        if (event.getGuild().getAudioManager().isConnected()) {
            if (!event.getArgs().isEmpty()) {
                System.out.println("2");
                Constant.music.loadAndPlay(event);
                if (id != null) {
                    System.out.println("3");
                    if (!Constant.nowPlayingList.containsKey(event.getGuild().getId())) {
                        System.out.println("4");
                        new NowPlaying(event.getGuild(), Constant.music);
                    }
                }
            } else {
                event.replyWarning(event.getMember().getAsMention() + " You need to specify an url");
            }
        } else {
            event.replyWarning(event.getMember().getAsMention() + " I'm not even connected :joy:");
        }
    }

}
