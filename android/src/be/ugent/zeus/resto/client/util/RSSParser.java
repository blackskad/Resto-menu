package be.ugent.zeus.resto.client.util;

import android.util.Log;
import be.ugent.zeus.resto.client.data.rss.Category;
import be.ugent.zeus.resto.client.data.rss.Channel;
import be.ugent.zeus.resto.client.data.rss.Item;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An absolutely incomplete RSS parser. Only implemented the tags i need to 
 * parse the schamper rss feed...
 * 
 * FYI I HATE PARSING XML LIKE THIS
 * 
 * @author Thomas Meire
 */
public class RSSParser {

  private Item createItem(Node node) {
    Item item = new Item();

    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);

      if ("title".equals(child.getNodeName())) {
        item.title = child.getTextContent();
      } else if ("comments".equals(child.getNodeName())) {
        item.comments = child.getTextContent();
      } else if ("description".equals(child.getNodeName())) {
        item.description = child.getTextContent();
      } else if ("dc:creator".equals(child.getNodeName())) {
        item.creator = child.getTextContent();
      } else if ("link".equals(child.getNodeName())) {
        item.link = child.getTextContent();
      } else if ("pubDate".equals(child.getNodeName())) {
        // TODO: parse this to a proper Date object
        item.pubDate = child.getTextContent();
      } else if ("category".equals(child.getNodeName())) {
        Category category = new Category();
        category.domain = child.getAttributes().getNamedItem("domain").getTextContent();
        category.value = child.getTextContent();
        item.categories.add(category);
      } else {
        // ignore guid & #text tags
      }
    }
    return item;
  }

  public Channel parse(String feedXML) {
    Channel channel = new Channel();

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(feedXML)));

      Node rss = doc.getFirstChild();
      if (rss == null || !"rss".equals(rss.getNodeName().toLowerCase())) {
        Log.w("[RSSParser]", "Not an rss feed!");
        return channel;
      }

      NodeList children = rss.getFirstChild().getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        Node child = children.item(i);

        if ("item".equals(child.getNodeName())) {
          channel.items.add(createItem(child));
        } else if ("title".equals(child.getNodeName())) {
          channel.title = child.getTextContent();
        } else if ("description".equals(child.getNodeName())) {
          channel.description = child.getTextContent();
        } else if ("language".equals(child.getNodeName())) {
          channel.language = child.getTextContent();
        } else if ("link".equals(child.getNodeName())) {
          channel.link = child.getTextContent();
        } else {
          // ignore #text tags
        }
      }

    } catch (ParserConfigurationException e) {
      Log.e("[RSSParser]", "XML parse error: " + e.getMessage());
    } catch (SAXException e) {
      Log.e("[RSSParser]", "Wrong XML file structure: " + e.getMessage());
    } catch (IOException e) {
      Log.e("[RSSParser]", "I/O exeption: " + e.getMessage());
    }
    return channel;
  }

  /*
  private static String load() {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(new File("/home/blackskad/Downloads/schamper-dailies.rss")));

      String xml = "";

      String line;
      while ((line = reader.readLine()) != null) {
        xml += line;
      }
      return xml;
    } catch (Exception e) {
      return "";
    }
  }

  public static void main(String[] args) {
    String xml = load();

    RSSParser parser = new RSSParser();
    parser.parse(xml);
  }
   * 
   */
}