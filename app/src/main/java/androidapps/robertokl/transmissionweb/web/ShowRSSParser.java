package androidapps.robertokl.transmissionweb.web;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidapps.robertokl.transmissionweb.db.Entry;

/**
 * Created by klein on 3/1/15.
 */
public class ShowRSSParser {
    private static final String ns = null;
    private Context context;

    public List<Entry> parse(InputStream in, Context context) throws XmlPullParserException, IOException {
        this.context = context;

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }
    private List readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "rss");
        parser.next();
        parser.require(XmlPullParser.START_TAG, ns, "channel");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("item")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }
    private Entry readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "item");
        String title = null;
        String link = null;
        String description = null;
        String guid = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                title = readProperty(parser, "title");
            } else if (name.equals("link")) {
                link = readProperty(parser, "link");
            } else if (name.equals("description")) {
                description = readProperty(parser, "description");
            } else if (name.equals("guid")) {
                guid = readProperty(parser, "guid");
            } else {
                skip(parser);
            }
        }

        Entry entry = new Entry(this.context);
        entry.title = title;
        entry.link = link;
        entry.description = description;
        entry.guid = guid;
        return entry;
    }
    private String readProperty(XmlPullParser parser, String property) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, property);
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, property);
        return title;
    }
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}