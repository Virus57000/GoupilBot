/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bot.commands.music;

import bot.Constant;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

/**
 *
 * @author renardn
 */
public class NowPlayingCommand extends Command {

    public NowPlayingCommand() {
        this.name = "nowPlaying";
        this.aliases = new String[]{"np"};
        this.help = "show the current playing track";
        this.guildOnly = true;
        this.ownerCommand = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        String id = Constant.getTextChannelConf().getProperty(event.getGuild().getId());
        if (id != null) {
            if (!event.getChannel().getId().equals(id)) {
                return;
            }
        }

        if (event.getGuild().getAudioManager().isConnected()) {
            if (Constant.music.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack() != null) {
                Constant.nowPlayingList.get(event.getGuild().getId()).resendNowPlaying();
            } else {
                event.replyWarning("I'm not playing anything");
            }
        }else{
            event.replyError(event.getMember().getAsMention() + " I'm not even connected :joy:");
        }
    }
}
