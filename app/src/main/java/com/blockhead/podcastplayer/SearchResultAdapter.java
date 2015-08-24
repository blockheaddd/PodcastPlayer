package com.blockhead.podcastplayer;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by GusSilva on 8/23/15.
 */
public class SearchResultAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Podcast> podcastList;
    private LayoutInflater podcastInflater;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private final String TAG = "pkrss", TAG2 = "bhca";

    public SearchResultAdapter(Context c, ArrayList<Podcast> theResults)
    {
        context = c;
        podcastInflater = LayoutInflater.from(context);
        podcastList = theResults;
        Log.d(TAG2, "SIZE: " + podcastList.size());
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions
                .Builder()
                .cacheOnDisk(true)
                .displayer(new RoundedBitmapDisplayer(200))
                .build();   //TODO: add show on fail image, etc...
    }
    @Override
    public int getCount() {
        return (podcastList == null? 0 : podcastList.size());
    }

    @Override
    public Podcast getItem(int position) {
        return (podcastList == null? null : podcastList.get(position));
    }

    @Override
    public long getItemId(int position) {
        try
        {
            return podcastList.get(position).hashCode();
        }
        catch(NullPointerException e)
        {
            Log.d("pkrss", "NullPointerException on index: " + position);
            return -1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout podcastLayout;
        if(convertView == null)
            podcastLayout = (LinearLayout) podcastInflater.inflate(R.layout.podcast_layout, parent, false);
        else        //Else recycle view
            podcastLayout = (LinearLayout)convertView;

        Podcast tempPodcast = getItem(position);
        Log.d(TAG2, "Creating: " + tempPodcast.getTitle() + "'s View!!!");
        ImageView podcastImg = (ImageView)podcastLayout.findViewById(R.id.podcast_image);
        TextView title = (TextView)podcastLayout.findViewById(R.id.podcast_title);
        TextView date = (TextView)podcastLayout.findViewById(R.id.podcast_date);
        TextView duration = (TextView)podcastLayout.findViewById(R.id.podcast_duration);
        TextView description = (TextView)podcastLayout.findViewById(R.id.podcast_description);

        imageLoader.displayImage(tempPodcast.getArtworkLargeUrl(), podcastImg, options);
        title.setText(tempPodcast.getTitle());
        duration.setText("Length: " + tempPodcast.getDuration());
        date.setText(tempPodcast.getDatePublished());
        description.setText(tempPodcast.getDescription());

        return podcastLayout;
    }
}
