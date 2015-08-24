package com.blockhead.podcastplayer;

import android.text.Html;

/**
 * Created by GusSilva on 8/20/15.
 */
public class Podcast {

    private String title, description, author, audioUrl, datePublished, duration;
    private String artworkSmallUrl, artworkMediumUrl, artworkLargeUrl;
    private Author authorObj;
    private String id;

    Podcast()
    {
        //Constructor
    }

    public void setTitle( String thisTitle){ title = thisTitle; }
    public String getTitle(){ return title; }

    public void setDescription(String thisDescription)
    {
        description = Html.fromHtml(thisDescription).toString();
    }
    public String getDescription(){ return description; }

    public void setAuthor(String thisAuthor){ author = thisAuthor; }
    public String getAuthor(){ return author; }

    public void setAudioUrl(String thisUrl){ audioUrl = thisUrl; }
    public String getAudioUrl(){ return audioUrl; }

    public void setDatePublished(String thisDatePublished)
    {
        datePublished = reformatPubDate(thisDatePublished);

    }
    public String getDatePublished(){return datePublished;}

    public void setDuration(String thisDuration){ duration = thisDuration;}
    public String getDuration(){return duration;}

    public void setArtworkLargeUrl(String thisArtworkLargeUrl)
    { artworkLargeUrl = thisArtworkLargeUrl;}
    public String getArtworkLargeUrl(){return artworkLargeUrl;}

    //TODO: Maybe make a reference to the index of a master list of authors from main activity that
    //TODO: (cont) way they can still be changed later
    public void setAuthorObj(Author thisAuthorObj){authorObj = thisAuthorObj;}
    public Author getAuthorObj(){ return authorObj; }

    public void setId(String thisId){id = thisId;}
    public String getId(){ return id; }

    private String reformatPubDate(String uglyDate)
    {
        char[] chars = uglyDate.toCharArray();
        String result = "";
        int place = 0;
        for(int i = 0; i < chars.length; i++)
        {
            if(chars[i] == ':')
                place = i - 5;
        }
        for(int i = 0; i < place; i++)
            result += chars[i];
        return result;
    }
}
