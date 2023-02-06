package palaiologos.kamilalisp.runtime.xml;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import palaiologos.kamilalisp.atom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class XMLParse extends PrimitiveFunction implements Lambda {
    @Override
    public Atom apply(Environment env, List<Atom> args) {
        assertArity(args, 1);
        Atom arg = args.get(0);
        String xmldata = null;

        if(arg.getType() == Type.LIST) {
            // Assume a buffer.
            byte[] data = new byte[arg.getList().size()];
            for (int i = 0; i < arg.getList().size(); i++)
                data[i] = arg.getList().get(i).getInteger().byteValueExact();
            xmldata = new String(data, StandardCharsets.UTF_8);

        } else if(arg.getType() == Type.STRING) {
            // Assume a string.
            xmldata = arg.getString();
        } else {
            throw new RuntimeException("xml:parse not defined for: " + arg.getType());
        }

        // Parse the XML.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = factory.newDocumentBuilder();
            Document dom = db.parse(xmldata);
            return new Atom(new XMLDocument(dom));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String name() {
        return "xml:parse";
    }
}