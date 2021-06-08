import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.audio.CombinedAudio;
import net.dv8tion.jda.api.audio.UserAudio;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

// example https://github.com/DV8FromTheWorld/JDA/blob/master/src/examples/java/AudioEchoExample.java

public class VoiceHandler extends ListenerAdapter {
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event)
    {
        Message message = event.getMessage();
        User author = message.getAuthor();
        String content = message.getContentRaw();
        Guild guild = event.getGuild();

        // Ignore message if bot
        if (author.isBot())
            return;

        if (content.startsWith("!silence "))
        {
            String arg = content.substring("!echo ".length());
            onSilenceCommand(event, guild, arg);
        }
        else if (content.equals("!silence"))
        {
            onSilenceCommand(event);
        }
    }


    /**
     * Handle command without arguments.
     *
     * @param event
     *        The event for this command
     */
    private void onSilenceCommand(GuildMessageReceivedEvent event)
    {
        // Note: None of these can be null due to our configuration with the JDABuilder!
        Member member = event.getMember();                              // Member is the context of the user for the specific guild, containing voice state and roles
        GuildVoiceState voiceState = member.getVoiceState();            // Check the current voice state of the user
        VoiceChannel channel = voiceState.getChannel();                 // Use the channel the user is currently connected to
        User[] users = channel.
        if (channel != null)
        {
            connectTo(channel);                                         // Join the channel of the user
            onConnecting(channel, event.getChannel());                  // Tell the user about our success
        }
        else
        {
            onUnknownChannel(event.getChannel(), "your voice channel"); // Tell the user about our failure
        }
    }

    /**
     * Handle command with arguments.
     *
     * @param event
     *        The event for this command
     * @param guild
     *        The guild where its happening
     * @param arg
     *        The input argument
     */
    private void onSilenceCommand(GuildMessageReceivedEvent event, Guild guild, String arg)
    {
        boolean isNumber = arg.matches("\\d+"); // This is a regular expression that ensures the input consists of digits
        VoiceChannel channel = null;
        if (isNumber)                           // The input is an id?
        {
            channel = guild.getVoiceChannelById(arg);
        }
        if (channel == null)                    // Then the input must be a name?
        {
            List<VoiceChannel> channels = guild.getVoiceChannelsByName(arg, true);
            if (!channels.isEmpty())            // Make sure we found at least one exact match
                channel = channels.get(0);      // We found a channel! This cannot be null.
        }

        TextChannel textChannel = event.getChannel();
        if (channel == null)                    // I have no idea what you want mr user
        {
            onUnknownChannel(textChannel, arg); // Let the user know about our failure
            return;
        }
        connectTo(channel);                     // We found a channel to connect to!
        onConnecting(channel, textChannel);     // Let the user know, we were successful!
    }

    /**
     * Inform user about successful connection.
     *
     * @param channel
     *        The voice channel we connected to
     * @param textChannel
     *        The text channel to send the message in
     */
    private void onConnecting(VoiceChannel channel, TextChannel textChannel)
    {
        textChannel.sendMessage("Connecting to " + channel.getName()).queue(); // never forget to queue()!
    }

    /**
     * The channel to connect to is not known to us.
     *
     * @param channel
     *        The message channel (text channel abstraction) to send failure information to
     * @param comment
     *        The information of this channel
     */
    private void onUnknownChannel(MessageChannel channel, String comment)
    {
        channel.sendMessage("Unable to connect to ``" + comment + "``, no such channel!").queue(); // never forget to queue()!
    }

    /**
     * Connect to requested channel and start echo handler
     *
     * @param channel
     *        The channel to connect to
     */
    private void connectTo(VoiceChannel channel)
    {
        Guild guild = channel.getGuild();
        // Get an audio manager for this guild, this will be created upon first use for each guild
        AudioManager audioManager = guild.getAudioManager();
        // Create our Receive handler for the audio connection
        SilenceHandler handler = new SilenceHandler();


        // The order of the following instructions does not matter!
        // Set the receiving handler to the same echo system, otherwise we can't echo anything
        audioManager.setReceivingHandler(handler);
        // Connect to the voice channel
        audioManager.openAudioConnection(channel);

        handler.handleUserAudio();
    }

    public static class SilenceHandler implements AudioReceiveHandler
    {
        /*
            All methods in this class are called by JDA threads when resources are available/ready for processing.
            The receiver will be provided with the latest 20ms of PCM stereo audio
            The sender will provide 20ms of PCM stereo audio (pass-through) once requested by JDA
         */
        /* Receive Handling */

        @Override // give audio separately for each user that is speaking
        public boolean canReceiveUser()
        {
            return true;
        }

        @Override
        public void handleUserAudio(UserAudio userAudio) {
            byte[] data = userAudio.getAudioData(1.0f); // volume at 100% = 1.0 (50% = 0.5 / 55% = 0.55)
            checkIfTooLoud(data);
        }

        private boolean checkIfTooLoud(byte[] audioData) throws LineUnavailableException
        {
            float sampleRate = 48000.0f;
            int numChannels = 2;
            int sampleSizeBits = 16;
            double maxDB = 0;
            InputStream stream = new InputStream();
            AudioFormat format = new AudioFormat(sampleRate, sampleSizeBits, true, true);
            TargetDataLine tdl = ;

            tdl.open(format);
            tdl.start();
            if (!tdl.isOpen()) {
                System.exit(1);
            }

            byte[] data = audioData;

            int read = tdl.read(data, 0, (int) sampleRate);
            if(read > 0){
                for (int i = 0; i < read-1; i= i+2){
                    float currentLevel = data[i].getLevel();
//                    long val = ((data[i] & 0xffL) << 8L) | (data[i + 1] & 0xffL);
//                    long valf = extendSign(val, 16);
//                    System.out.println(i + "\t" + valf);
                }
            }
            tdl.close();

            
        }
    }
}
