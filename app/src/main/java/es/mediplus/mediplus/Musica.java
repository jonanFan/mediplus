package es.mediplus.mediplus;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by jon on 3/11/15.
 */
public class Musica {
    private static MediaPlayer player;

    public static void start(Context ctx, int id){
        player=MediaPlayer.create(ctx, id);
        player.setLooping(true);
        player.start();
    }

    public static void stop() {
        if(player != null){
            player.stop();
            player.release();
            player=null;
        }
    }

    public static void play() {
        if(player!=null){
            player.start();
        }
    }
    
    public static void pause() {
        if(player!=null){
            player.pause();
        }
    }
}
