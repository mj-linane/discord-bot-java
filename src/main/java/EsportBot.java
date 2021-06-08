import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class EsportBot {
    public static void main(String[] args){
        try{
            JDA jda = JDABuilder.createDefault("asda0")
                    .addEventListeners(new TextHandler(), new VoiceHandler())
                    .build();

        }
        catch(LoginException e){
            e.printStackTrace();
        }
    }
}
