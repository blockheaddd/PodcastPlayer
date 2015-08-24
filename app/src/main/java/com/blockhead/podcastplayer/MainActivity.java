package com.blockhead.podcastplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "pkrss", TAG2 = "bhca";
    private ArrayList<Podcast> searchResultPodcastList = new ArrayList<>();
    private static ListView searchResultListView;
    private static SearchResultAdapter searchResultAdapter;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this)
                .build());


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        searchResultAdapter = new SearchResultAdapter(getApplicationContext()
        , searchResultPodcastList);

        MySync s = new MySync();
        s.execute();

    }

    public class MySync extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... params) {
            //String feedUrl = getFeedUrl("https://itunes.apple.com/search?term=Better+Rivals&entity=podcast");
            String feedUrl = getFeedUrl("https://itunes.apple.com/search?term=Around+the+NFL&entity=podcast");


            if(feedUrl != null)
                searchResultPodcastList = getPodcastListFromFeed(feedUrl);

            //getPodcastListFromFeed("http://aroundtheleague.libsyn.com/rss");
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            if(searchResultListView != null)
            {
                searchResultAdapter = new SearchResultAdapter(getApplicationContext(), searchResultPodcastList);
                searchResultListView.setAdapter(searchResultAdapter);
                searchResultAdapter.notifyDataSetChanged();
            }
        }
    }

    private ArrayList<Podcast> getPodcastListFromFeed(String url)
    {
        try
        {
            ArrayList<Podcast> podcastList = new ArrayList<>();
            XMLParser parser = new XMLParser();
            String xml = parser.getXmlFromUrl(url); // getting XML from URL
            Document doc = parser.getDomElement(xml);
            NodeList items = doc.getElementsByTagName("item");
            Log.d(TAG, "item length: " + items.getLength());

            for (int k = 0; k < items.getLength() && k < 5; k++) //Cycle through each podcast in feed
            {
                Podcast tempPodcast = new Podcast();
                NodeList nl = items.item(k).getChildNodes();
                Log.d(TAG, "===============" + k + "================");
                for (int i = 0; i < nl.getLength(); i++) //Cycle through each podcast's attributes
                {
                    String name = nl.item(i).getNodeName();
                    String value = "";
                    if (!name.equals("#text"))
                    {
                        Element e = (Element) nl.item(i);
                        if (!e.hasAttributes())
                        {
                            value = nl.item(i).getTextContent();
                            //Log.d(TAG, "SENDING -> " + name + ": " + value);
                            tempPodcast = populatePodcastFromXml(tempPodcast, name, value);
                        }
                        else if (name.equals("guid"))    //SPECIAL CASE FOR GUID TODO: Make more universal
                        {
                            //Log.d(TAG, "+++++GUID: " + nl.item(i).getTextContent());
                            value = nl.item(i).getTextContent();
                            tempPodcast = populatePodcastFromXml(tempPodcast, name, value);
                        }
                        else {
                            NamedNodeMap map = e.getAttributes();
                            for (int j = 0; j < map.getLength(); j++)
                            {
                                String attrName = map.item(j).getNodeName();
                                String attrVal = map.item(j).getTextContent();
                                //Log.d(TAG, "(ATTRS) SENDING -> " + name + ": " + attrName + " = " + attrVal);
                                tempPodcast = populatePodcastFromXml(tempPodcast, name, attrName, attrVal);
                            }
                        }
                    }
                }
                podcastList.add(tempPodcast);
            }
            Log.d(TAG2, "Final Size after populating: " + podcastList.size());
            return podcastList;
        }
        catch (Exception e)
        {
            Log.d(TAG, "Error executing getPodcastListFromFeed() " + e.getMessage());
            return null;
        }
    }
    private String getFeedUrl(String url) {
        try
        {
            String str = "";
            URL oracle = new URL(url);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(oracle.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null)
                str += inputLine;
            in.close();

            JSONObject j = new JSONObject(str);
            JSONArray jArr = new JSONArray(j.get("results").toString());
            JSONObject j2 = jArr.optJSONObject(0);
            String feedUrl = j2.getString("feedUrl");
            Log.d(TAG, "feed: " + feedUrl);
            return feedUrl;
        }
        catch (JSONException e)
        {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
        catch (IOException e)
        {
            Log.d(TAG, "IOException: " + e.getMessage());
        }
        return null;
    }

    private Podcast populatePodcastFromXml(Podcast podcast, String elemName, String elemVal)
    {
        switch(elemName)
        {
            case "title":
                Log.d(TAG, "Title: " + elemVal);
                podcast.setTitle(elemVal);
                break;
            case "itunes:author":
                Log.d(TAG, "Author: " + elemVal);
                podcast.setAuthor(elemVal);
            case "pubDate":
                Log.d(TAG, "Date Published: " + elemVal);
                podcast.setDatePublished(elemVal);
                break;
            case "guid":
                Log.d(TAG, "ID: " + elemVal);
                podcast.setId(elemVal);
                break;
            case "link":
                Log.d(TAG, "Link: " + elemVal);
                podcast.setAudioUrl(elemVal);
                break;
            case "description":
                Log.d(TAG, "Description: " + elemVal);
                podcast.setDescription(elemVal);
                break;
            case "itunes:duration":
                Log.d(TAG, "Duration: " + elemVal);
                podcast.setDuration(elemVal);
                break;
            case "itunes:explicit":
                Log.d(TAG, "Explicit: " + elemVal); //TODO: add
                break;
            case "itunes:keywords":
                Log.d(TAG, "Keywords: " + elemVal); //TODO: add
                break;
            case "itunes:subtitle":
                Log.d(TAG, "Subtitle: " + elemVal); //TODO: add
                break;
            default:
                Log.d(TAG, "Invalid");
                break;
        }
        return podcast;
    }

    private Podcast populatePodcastFromXml(Podcast podcast, String elemName, String attrName, String attrValue)
    {
        switch(elemName)
        {
            case "enclosure":   //TODO: add
                if(attrName.equals("length"))
                    Log.d(TAG, "Length: " + attrValue);
                else if(attrName.equals("type"))
                    Log.d(TAG, "Type: " + attrValue);
                else if(attrName.equals("url"))
                    Log.d(TAG, "Audio Url: " + attrValue);
                break;
            case "itunes:image":
                Log.d(TAG, "Image Link: " + attrValue);
                podcast.setArtworkLargeUrl(attrValue);
                break;
        }

        return podcast;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Bundle args = getArguments();
            int page = args.getInt(ARG_SECTION_NUMBER);

            if(page == 1)
            {
                View rootView = inflater.inflate(R.layout.fragment_main, container, false);
                searchResultListView = (ListView)rootView.findViewById(R.id.search_result_list_view);
                if(searchResultListView != null)
                    searchResultListView.setAdapter(searchResultAdapter);
                return rootView;
            }
            else
                return null;
        }
    }

}
